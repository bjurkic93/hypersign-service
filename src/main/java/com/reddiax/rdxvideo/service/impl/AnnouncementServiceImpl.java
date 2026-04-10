package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.constant.AnnouncementChannelEnum;
import com.reddiax.rdxvideo.constant.AnnouncementStatusEnum;
import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.AnnouncementDTO;
import org.springframework.http.HttpStatus;
import com.reddiax.rdxvideo.model.dto.CreateAnnouncementRequestDTO;
import com.reddiax.rdxvideo.model.entity.AnnouncementEntity;
import com.reddiax.rdxvideo.model.entity.UserEntity;
import com.reddiax.rdxvideo.model.mapper.AnnouncementMapper;
import com.reddiax.rdxvideo.repository.AnnouncementRepository;
import com.reddiax.rdxvideo.repository.DeviceRegistrationRepository;
import com.reddiax.rdxvideo.repository.UserRepository;
import com.reddiax.rdxvideo.service.AnnouncementService;
import com.reddiax.rdxvideo.service.EmailService;
import com.reddiax.rdxvideo.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementMapper announcementMapper;
    private final UserRepository userRepository;
    private final DeviceRegistrationRepository deviceRegistrationRepository;
    private final PushNotificationService pushNotificationService;
    private final EmailService emailService;

    @Override
    @Transactional
    public AnnouncementDTO createAnnouncement(CreateAnnouncementRequestDTO request, String creatorExternalId) {
        log.info("Creating announcement: {} by user {}", request.getTitle(), creatorExternalId);

        UserEntity creator = userRepository.findByExternalId(creatorExternalId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "Creator not found", "CREATOR_NOT_FOUND"));

        AnnouncementEntity announcement = AnnouncementEntity.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .htmlContent(request.getHtmlContent())
                .channel(request.getChannel())
                .scheduledAt(request.getScheduledAt())
                .status(request.getScheduledAt() != null ? AnnouncementStatusEnum.SCHEDULED : AnnouncementStatusEnum.DRAFT)
                .createdBy(creator)
                .active(true)
                .build();

        announcement = announcementRepository.save(announcement);

        // If no scheduled time, send immediately
        if (request.getScheduledAt() == null) {
            sendAnnouncementAsync(announcement.getId());
        }

        return announcementMapper.toDTO(announcement);
    }

    @Override
    public Page<AnnouncementDTO> getAllAnnouncements(Pageable pageable) {
        return announcementRepository.findByActiveTrue(pageable)
                .map(announcementMapper::toDTO);
    }

    @Override
    public AnnouncementDTO getAnnouncementById(Long id) {
        AnnouncementEntity announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "Announcement not found", "ANNOUNCEMENT_NOT_FOUND"));
        return announcementMapper.toDTO(announcement);
    }

    @Override
    @Transactional
    public AnnouncementDTO cancelAnnouncement(Long id) {
        AnnouncementEntity announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "Announcement not found", "ANNOUNCEMENT_NOT_FOUND"));

        if (announcement.getStatus() == AnnouncementStatusEnum.SENT ||
            announcement.getStatus() == AnnouncementStatusEnum.SENDING) {
            throw new RdXException(HttpStatus.BAD_REQUEST, "Cannot cancel an announcement that is being sent or already sent", "CANNOT_CANCEL");
        }

        announcement.setStatus(AnnouncementStatusEnum.CANCELLED);
        announcement = announcementRepository.save(announcement);

        log.info("Announcement {} cancelled", id);
        return announcementMapper.toDTO(announcement);
    }

    @Override
    @Transactional
    public AnnouncementDTO sendAnnouncementNow(Long id) {
        AnnouncementEntity announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "Announcement not found", "ANNOUNCEMENT_NOT_FOUND"));

        if (announcement.getStatus() == AnnouncementStatusEnum.SENT ||
            announcement.getStatus() == AnnouncementStatusEnum.SENDING) {
            throw new RdXException(HttpStatus.BAD_REQUEST, "Announcement has already been sent or is being sent", "ALREADY_SENT");
        }

        sendAnnouncementAsync(id);
        return announcementMapper.toDTO(announcement);
    }

    @Override
    @Transactional
    public void processScheduledAnnouncements() {
        List<AnnouncementEntity> scheduledAnnouncements = announcementRepository
                .findScheduledAnnouncementsReadyToSend(AnnouncementStatusEnum.SCHEDULED, LocalDateTime.now());

        log.info("Found {} scheduled announcements ready to send", scheduledAnnouncements.size());

        for (AnnouncementEntity announcement : scheduledAnnouncements) {
            sendAnnouncementAsync(announcement.getId());
        }
    }

    @Override
    public AnnouncementStatsDTO getStats() {
        long total = announcementRepository.count();
        long draft = announcementRepository.countByStatus(AnnouncementStatusEnum.DRAFT);
        long scheduled = announcementRepository.countByStatus(AnnouncementStatusEnum.SCHEDULED);
        long sent = announcementRepository.countByStatus(AnnouncementStatusEnum.SENT);
        long totalUsers = userRepository.countActiveUsers();
        long totalDevices = deviceRegistrationRepository.countActiveFcmTokens();

        return new AnnouncementStatsDTO(total, draft, scheduled, sent, totalUsers, totalDevices);
    }

    /**
     * Send announcement asynchronously.
     */
    @Async
    @Transactional
    public void sendAnnouncementAsync(Long announcementId) {
        AnnouncementEntity announcement = announcementRepository.findById(announcementId)
                .orElse(null);

        if (announcement == null) {
            log.error("Announcement {} not found for sending", announcementId);
            return;
        }

        log.info("Starting to send announcement: {} (channel: {})", 
                announcement.getTitle(), announcement.getChannel());

        announcement.setStatus(AnnouncementStatusEnum.SENDING);
        announcement = announcementRepository.save(announcement);

        int totalRecipients = 0;
        int successfulDeliveries = 0;
        int failedDeliveries = 0;

        try {
            AnnouncementChannelEnum channel = announcement.getChannel();

            // Send push notifications
            if (channel == AnnouncementChannelEnum.PUSH || channel == AnnouncementChannelEnum.BOTH) {
                List<String> fcmTokens = deviceRegistrationRepository.findAllActiveFcmTokens();
                totalRecipients += fcmTokens.size();

                if (!fcmTokens.isEmpty()) {
                    int pushSuccess = pushNotificationService.sendNotificationToDevices(
                            fcmTokens, 
                            announcement.getTitle(), 
                            announcement.getMessage()
                    );
                    successfulDeliveries += pushSuccess;
                    failedDeliveries += (fcmTokens.size() - pushSuccess);
                    log.info("Push notifications sent: {}/{}", pushSuccess, fcmTokens.size());
                }
            }

            // Send emails
            if (channel == AnnouncementChannelEnum.EMAIL || channel == AnnouncementChannelEnum.BOTH) {
                List<UserEntity> usersWithEmail = userRepository.findAllActiveUsersWithEmail();
                totalRecipients += usersWithEmail.size();

                String emailContent = announcement.getHtmlContent() != null 
                        ? announcement.getHtmlContent() 
                        : announcement.getMessage();
                boolean isHtml = announcement.getHtmlContent() != null;

                for (UserEntity user : usersWithEmail) {
                    try {
                        if (isHtml) {
                            emailService.sendHtmlEmail(user.getEmail(), announcement.getTitle(), emailContent);
                        } else {
                            emailService.sendEmail(user.getEmail(), announcement.getTitle(), emailContent);
                        }
                        successfulDeliveries++;
                    } catch (Exception e) {
                        log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage());
                        failedDeliveries++;
                    }
                }
                log.info("Emails sent to {} users", usersWithEmail.size());
            }

            announcement.setStatus(AnnouncementStatusEnum.SENT);
            announcement.setSentAt(LocalDateTime.now());
            log.info("Announcement {} sent successfully", announcementId);

        } catch (Exception e) {
            log.error("Failed to send announcement {}: {}", announcementId, e.getMessage());
            announcement.setStatus(AnnouncementStatusEnum.FAILED);
        }

        announcement.setTotalRecipients(totalRecipients);
        announcement.setSuccessfulDeliveries(successfulDeliveries);
        announcement.setFailedDeliveries(failedDeliveries);
        announcementRepository.save(announcement);
    }
}
