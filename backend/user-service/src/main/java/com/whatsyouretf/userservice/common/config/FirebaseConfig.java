package com.whatsyouretf.userservice.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase Admin SDK 설정
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    private final ResourceLoader resourceLoader;

    @Value("${firebase.credentials-path}")
    private String credentialsPath;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                Resource resource = resourceLoader.getResource(credentialsPath);
                InputStream serviceAccount = resource.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK 초기화 완료");
            }
        } catch (IOException e) {
            log.error("Firebase 초기화 실패: {}", e.getMessage());
        }
    }
}
