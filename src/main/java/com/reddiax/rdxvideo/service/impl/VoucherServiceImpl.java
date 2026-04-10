package com.reddiax.rdxvideo.service.impl;

import com.reddiax.rdxvideo.constant.EmailTemplateConstant;
import com.reddiax.rdxvideo.constant.VoucherStatusEnum;
import com.reddiax.rdxvideo.exception.RdXException;
import com.reddiax.rdxvideo.model.dto.VoucherDTO;
import com.reddiax.rdxvideo.model.entity.OrganizationEntity;
import com.reddiax.rdxvideo.model.entity.UserAdvertisementEntity;
import com.reddiax.rdxvideo.model.entity.UserEntity;
import com.reddiax.rdxvideo.model.entity.VoucherEntity;
import com.reddiax.rdxvideo.model.mapper.VoucherMapper;
import com.reddiax.rdxvideo.repository.OrganizationRepository;
import com.reddiax.rdxvideo.repository.UserAdvertisementRepository;
import com.reddiax.rdxvideo.repository.UserRepository;
import com.reddiax.rdxvideo.repository.VoucherRepository;
import com.reddiax.rdxvideo.security.SecurityUtils;
import com.reddiax.rdxvideo.service.EmailService;
import com.reddiax.rdxvideo.service.PushNotificationService;
import com.reddiax.rdxvideo.service.VoucherService;
import freemarker.template.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final UserAdvertisementRepository userAdvertisementRepository;
    private final OrganizationRepository organizationRepository;
    private final VoucherMapper voucherMapper;
    private final EmailService emailService;
    private final PushNotificationService pushNotificationService;
    private final Configuration freemarkerConfig;

    public VoucherServiceImpl(
            VoucherRepository voucherRepository,
            UserRepository userRepository,
            UserAdvertisementRepository userAdvertisementRepository,
            OrganizationRepository organizationRepository,
            VoucherMapper voucherMapper,
            EmailService emailService,
            PushNotificationService pushNotificationService,
            @Qualifier("freeMarkerConfiguration") Configuration freemarkerConfig) {
        this.voucherRepository = voucherRepository;
        this.userRepository = userRepository;
        this.userAdvertisementRepository = userAdvertisementRepository;
        this.organizationRepository = organizationRepository;
        this.voucherMapper = voucherMapper;
        this.emailService = emailService;
        this.pushNotificationService = pushNotificationService;
        this.freemarkerConfig = freemarkerConfig;
    }

    private static final String VOUCHER_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public VoucherDTO createRewardVoucher(Long userId, Long userAdvertisementId, BigDecimal discountPercentage,
                                          String description, int validDays) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "User not found", "USER_NOT_FOUND"));

        UserAdvertisementEntity userAd = null;
        OrganizationEntity organization = null;
        
        if (userAdvertisementId != null) {
            userAd = userAdvertisementRepository.findById(userAdvertisementId).orElse(null);
            if (userAd != null && userAd.getAdvertisement() != null) {
                Long orgId = userAd.getAdvertisement().getOrganizationId();
                if (orgId != null) {
                    organization = organizationRepository.findById(orgId).orElse(null);
                }
            }
        }

        String code = generateUniqueCode();
        
        VoucherEntity voucher = VoucherEntity.builder()
                .user(user)
                .userAdvertisement(userAd)
                .organization(organization)
                .code(code)
                .discountPercentage(discountPercentage)
                .description(description)
                .status(VoucherStatusEnum.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(validDays))
                .build();

        voucher = voucherRepository.save(voucher);
        log.info("Created reward voucher {} for user {} with {}% discount", code, userId, discountPercentage);

        // Send email notification
        sendVoucherEmail(user, voucher);
        
        // Send push notification
        sendVoucherPushNotification(user, voucher);

        return voucherMapper.toDTO(voucher);
    }

    @Override
    @Transactional
    public VoucherDTO createOrganizationVoucher(Long userId, Long userAdvertisementId, Long organizationId,
                                                BigDecimal discountPercentage, String description, int validDays) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "User not found", "USER_NOT_FOUND"));

        OrganizationEntity organization = null;
        if (organizationId != null) {
            organization = organizationRepository.findById(organizationId).orElse(null);
        }

        UserAdvertisementEntity userAd = null;
        if (userAdvertisementId != null) {
            userAd = userAdvertisementRepository.findById(userAdvertisementId).orElse(null);
        }

        String code = generateUniqueCode();
        
        VoucherEntity voucher = VoucherEntity.builder()
                .user(user)
                .userAdvertisement(userAd)
                .organization(organization)
                .code(code)
                .discountPercentage(discountPercentage)
                .description(description)
                .status(VoucherStatusEnum.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(validDays))
                .build();

        voucher = voucherRepository.save(voucher);
        log.info("Created organization voucher {} for user {} from org {}", code, userId, organizationId);

        // Send email notification
        sendVoucherEmail(user, voucher);
        
        // Send push notification
        sendVoucherPushNotification(user, voucher);

        return voucherMapper.toDTO(voucher);
    }

    @Override
    public VoucherDTO getVoucherById(Long voucherId) {
        VoucherEntity voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "Voucher not found", "VOUCHER_NOT_FOUND"));
        return voucherMapper.toDTO(voucher);
    }

    @Override
    public VoucherDTO getVoucherByCode(String code) {
        VoucherEntity voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "Voucher not found", "VOUCHER_NOT_FOUND"));
        return voucherMapper.toDTO(voucher);
    }

    @Override
    public Page<VoucherDTO> getMyVouchers(Pageable pageable) {
        Long userId = getCurrentUserId();
        return voucherRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(voucherMapper::toDTO);
    }

    @Override
    public List<VoucherDTO> getMyActiveVouchers() {
        Long userId = getCurrentUserId();
        return voucherRepository.findByUserIdAndStatusOrderByExpiresAtAsc(userId, VoucherStatusEnum.ACTIVE)
                .stream()
                .filter(v -> v.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(voucherMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<VoucherDTO> getVouchersByUserId(Long userId, Pageable pageable) {
        return voucherRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(voucherMapper::toDTO);
    }

    @Override
    @Transactional
    public VoucherDTO redeemVoucher(String code, String reference) {
        VoucherEntity voucher = voucherRepository.findActiveVoucherByCode(code.toUpperCase(), LocalDateTime.now())
                .orElseThrow(() -> new RdXException(HttpStatus.BAD_REQUEST, 
                        "Voucher not found, already used, or expired", "VOUCHER_INVALID"));

        voucher.markAsUsed(reference);
        voucher = voucherRepository.save(voucher);
        
        log.info("Voucher {} redeemed with reference: {}", code, reference);
        
        return voucherMapper.toDTO(voucher);
    }

    @Override
    public VoucherDTO validateVoucher(String code) {
        VoucherEntity voucher = voucherRepository.findActiveVoucherByCode(code.toUpperCase(), LocalDateTime.now())
                .orElseThrow(() -> new RdXException(HttpStatus.BAD_REQUEST, 
                        "Voucher not found, already used, or expired", "VOUCHER_INVALID"));
        
        return voucherMapper.toDTO(voucher);
    }

    @Override
    @Transactional
    public void cancelVoucher(Long voucherId) {
        VoucherEntity voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "Voucher not found", "VOUCHER_NOT_FOUND"));
        
        if (voucher.getStatus() == VoucherStatusEnum.USED) {
            throw new RdXException(HttpStatus.BAD_REQUEST, "Cannot cancel a used voucher", "VOUCHER_ALREADY_USED");
        }
        
        voucher.cancel();
        voucherRepository.save(voucher);
        
        log.info("Voucher {} cancelled", voucherId);
    }

    @Override
    @Transactional
    public int expireOverdueVouchers() {
        int expired = voucherRepository.expireVouchers(LocalDateTime.now());
        if (expired > 0) {
            log.info("Expired {} overdue vouchers", expired);
        }
        return expired;
    }

    @Override
    public Long countMyActiveVouchers() {
        Long userId = getCurrentUserId();
        return voucherRepository.countActiveVouchersByUserId(userId, LocalDateTime.now());
    }

    /**
     * Generate a unique voucher code in format: RDX-XXXX-XXXX-XXXX
     */
    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        do {
            code = String.format("RDX-%s-%s-%s",
                    randomSegment(4),
                    randomSegment(4),
                    randomSegment(4));
            attempts++;
            if (attempts > 100) {
                throw new RdXException(HttpStatus.INTERNAL_SERVER_ERROR, 
                        "Failed to generate unique voucher code", "CODE_GENERATION_FAILED");
            }
        } while (voucherRepository.existsByCode(code));
        
        return code;
    }

    private String randomSegment(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(VOUCHER_CODE_CHARS.charAt(RANDOM.nextInt(VOUCHER_CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private Long getCurrentUserId() {
        String externalId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RdXException(HttpStatus.UNAUTHORIZED, "User not authenticated", "UNAUTHORIZED"));
        
        UserEntity user = userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RdXException(HttpStatus.NOT_FOUND, "User not found", "USER_NOT_FOUND"));
        
        return user.getId();
    }

    /**
     * Send voucher notification email to user using FreeMarker template.
     */
    private void sendVoucherEmail(UserEntity user, VoucherEntity voucher) {
        try {
            String organizationName = voucher.getOrganization() != null ? 
                    voucher.getOrganization().getName() : "RdX";
            String advertisementName = voucher.getUserAdvertisement() != null && 
                    voucher.getUserAdvertisement().getAdvertisement() != null ?
                    voucher.getUserAdvertisement().getAdvertisement().getName() : "RdX Reklama";
            
            // Prepare template data
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", user.getDisplayName());
            templateData.put("voucherCode", voucher.getCode());
            templateData.put("discountPercentage", voucher.getDiscountPercentage().stripTrailingZeros().toPlainString());
            templateData.put("expiresAt", voucher.getExpiresAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")));
            templateData.put("description", voucher.getDescription());
            templateData.put("advertisementName", advertisementName);
            templateData.put("organizationName", organizationName);
            
            // Render templates
            String subject = FreeMarkerTemplateUtils.processTemplateIntoString(
                    freemarkerConfig.getTemplate(EmailTemplateConstant.VOUCHER_REWARD_TEMPLATE + 
                            EmailTemplateConstant.SUBJECT_TEMPLATE_EXTENSION),
                    templateData
            );
            
            String body = FreeMarkerTemplateUtils.processTemplateIntoString(
                    freemarkerConfig.getTemplate(EmailTemplateConstant.VOUCHER_REWARD_TEMPLATE + 
                            EmailTemplateConstant.BODY_TEMPLATE_EXTENSION),
                    templateData
            );
            
            emailService.sendEmail(user.getEmail(), subject, body);
            log.info("Voucher email sent to user {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send voucher email to user {}: {}", user.getEmail(), e.getMessage());
            // Don't fail the voucher creation if email fails
        }
    }

    /**
     * Send voucher push notification to user's mobile devices.
     */
    private void sendVoucherPushNotification(UserEntity user, VoucherEntity voucher) {
        try {
            String organizationName = voucher.getOrganization() != null ? 
                    voucher.getOrganization().getName() : "RdX";
            
            pushNotificationService.sendVoucherEarnedNotification(
                    user.getId(),
                    voucher.getCode(),
                    voucher.getDiscountPercentage(),
                    organizationName
            );
            log.debug("Voucher push notification sent to user {}", user.getId());
        } catch (Exception e) {
            log.warn("Failed to send voucher push notification to user {}: {}", user.getId(), e.getMessage());
            // Don't fail the voucher creation if push notification fails
        }
    }
}
