package com.blueoptima.apirate;

import java.io.FileInputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@SpringBootApplication
public class TestapiApplication {

    public static void main(String[] args) throws IOException {   

        SpringApplication.run(TestapiApplication.class, args);
    }

    /**
     * Initializes the Firebase Database using Credentials
     */
    @PostConstruct
    private static void initFirebaseDb() throws IOException {
        // this file contains the Firebase Credentials
        FileInputStream serviceAccount = new FileInputStream("serviceAccountKey.json");

        if(FirebaseApp.getApps().isEmpty()) {
        	FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
 
            FirebaseApp.initializeApp(options);
        }
    }

}
