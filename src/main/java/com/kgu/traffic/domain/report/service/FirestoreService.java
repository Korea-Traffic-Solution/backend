package com.kgu.traffic.domain.report.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {

    private static final String COLLECTION_NAME = "reports";

    public Map<String, Object> getReportFirestoreData(Long reportId) {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(String.valueOf(reportId));
        ApiFuture<DocumentSnapshot> future = docRef.get();

        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                return document.getData();
            } else {
                throw new RuntimeException("해당 Firestore 문서가 존재하지 않음");
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Firestore 조회 중 오류 발생", e);
        }
    }

    public void saveReportToFirestore(Long reportId, Map<String, Object> data) {
        Firestore firestore = FirestoreClient.getFirestore();
        DocumentReference docRef = firestore.collection("reports").document(String.valueOf(reportId));
        ApiFuture<WriteResult> result = docRef.set(data);
        try {
            result.get();
        } catch (Exception e) {
            throw new RuntimeException("Firestore 저장 실패", e);
        }
    }

    public String getManagerRegion(String region) {
        Firestore firestore = FirestoreClient.getFirestore();
        CollectionReference managers = firestore.collection("Manager");

        ApiFuture<QuerySnapshot> query = managers.whereEqualTo("region", region).get();

        try {
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            if (!documents.isEmpty()) {
                return documents.get(0).getString("region");
            } else {
                throw new RuntimeException("해당 관리자 region 정보 없음");
            }
        } catch (Exception e) {
            throw new RuntimeException("Firestore에서 region 정보 조회 실패", e);
        }
    }
}