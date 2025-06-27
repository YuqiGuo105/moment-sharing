package com.example.datalake.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials-file:}")
    private String credentialsFile;

    @PostConstruct
    public void init() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }
        try {
            FirebaseOptions.Builder builder = FirebaseOptions.builder();
            if (credentialsFile != null && !credentialsFile.isBlank()) {
                try (InputStream in = new FileInputStream(credentialsFile)) {
                    builder.setCredentials(GoogleCredentials.fromStream(in));
                }
                // Ensure Spring Cloud GCP uses the same credentials if not already set
                String gcpCredsProp = System.getProperty("spring.cloud.gcp.credentials.location");
                if (gcpCredsProp == null || gcpCredsProp.isBlank()) {
                    System.setProperty("spring.cloud.gcp.credentials.location", "file:" + credentialsFile);
                }
            } else {
                builder.setCredentials(GoogleCredentials.getApplicationDefault());
            }
            FirebaseApp.initializeApp(builder.build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Firebase", e);
        }
    }
}
