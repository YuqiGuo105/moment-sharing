package com.example.datalake.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class GcsConfig {
    @Bean
    public Storage storage(
            @Value("file:${firebase.credentials-file}") Resource keyFile,
            @Value("${spring.cloud.gcp.project-id}") String projectId) throws IOException {

        GoogleCredentials creds = GoogleCredentials
                .fromStream(keyFile.getInputStream())
                .createScoped("https://www.googleapis.com/auth/cloud-platform");

        return StorageOptions.newBuilder()
                .setCredentials(creds)
                .setProjectId(projectId)
                .build()
                .getService();
    }
}
