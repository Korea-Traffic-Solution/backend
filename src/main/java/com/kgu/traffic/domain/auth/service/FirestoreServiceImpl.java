package com.kgu.traffic.domain.auth.service;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class FirestoreServiceImpl implements FirestoreService {

    @Override
    public void saveManagerToFirestore(String name, String email, String region, String position) {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> managerData = new HashMap<>();
        managerData.put("name", name);
        managerData.put("email", email);
        managerData.put("region", region);
        managerData.put("class", position); // class → 직급

        db.collection("Manager").document(email).set(managerData);
    }
}