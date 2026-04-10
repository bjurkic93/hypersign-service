package com.reddiax.rdxvideo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Firebase configuration for FCM push notifications.
 * 
 * Supports loading credentials from:
 * 1. FIREBASE_CREDENTIALS environment variable (JSON string) - for Heroku/cloud
 * 2. Classpath file (firebase-service-account.json)
 * 3. File path
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials-file:firebase-service-account.json}")
    private String credentialsFile;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;
    
    @Value("${FIREBASE_CREDENTIALS:}")
    private String firebaseCredentialsJson;

    @PostConstruct
    public void initialize() {
        if (!firebaseEnabled) {
            log.info("Firebase is disabled. Push notifications will not work.");
            return;
        }

        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getCredentialsStream();
                
                if (serviceAccount != null) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase initialized successfully for FCM push notifications");
                } else {
                    log.warn("Firebase credentials not found. Push notifications disabled.");
                    log.warn("Set FIREBASE_CREDENTIALS env var or provide {} file", credentialsFile);
                }
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }

    private InputStream getCredentialsStream() {
        // 1. First try FIREBASE_CREDENTIALS environment variable (JSON string)
        if (firebaseCredentialsJson != null && !firebaseCredentialsJson.isBlank()) {
            log.info("Loading Firebase credentials from FIREBASE_CREDENTIALS environment variable");
            return new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));
        }
        
        try {
            // 2. Try classpath
            ClassPathResource resource = new ClassPathResource(credentialsFile);
            if (resource.exists()) {
                log.info("Loading Firebase credentials from classpath: {}", credentialsFile);
                return resource.getInputStream();
            }
            
            // 3. Try file path
            log.info("Loading Firebase credentials from file: {}", credentialsFile);
            return new FileInputStream(credentialsFile);
        } catch (IOException e) {
            log.debug("Could not load Firebase credentials from file: {}", e.getMessage());
            return null;
        }
    }
}
