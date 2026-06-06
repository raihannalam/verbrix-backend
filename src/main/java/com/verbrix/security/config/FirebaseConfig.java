package com.verbrix.security.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {

        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        if (credentialsPath != null && !credentialsPath.isBlank()) {
            // PROD (Render)
            try (InputStream serviceAccount = new FileInputStream(credentialsPath)) {

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                return FirebaseApp.initializeApp(options);
            }
        }

        // DEV (Local)
        ClassPathResource resource = new ClassPathResource("firebase-service-account.json");

        if (!resource.exists()) {
            throw new IllegalStateException("Local firebase-service-account.json not found in resources.");
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                .build();

        return FirebaseApp.initializeApp(options);
    }
}
