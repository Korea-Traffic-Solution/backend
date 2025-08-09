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

    private static final String COLLECTION_CONCLUSION = "Conclusion";
    private static final String COLLECTION_REPORTS = "Conclusion";

    public Map<String, Object> getConclusionByDocId(String docId) {
        Firestore fs = FirestoreClient.getFirestore();
        DocumentReference docRef = fs.collection(COLLECTION_CONCLUSION).document(docId);
        try {
            DocumentSnapshot document = docRef.get().get();
            if (document.exists()) {
                return document.getData();
            } else {
                throw new RuntimeException("Conclusion 문서가 존재하지 않음: " + docId);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Conclusion 조회 중 오류", e);
        }
    }

    public void saveReportToReports(Long reportId, Map<String, Object> data) {
        Firestore fs = FirestoreClient.getFirestore();
        DocumentReference docRef = fs.collection(COLLECTION_REPORTS).document(String.valueOf(reportId));
        try {
            docRef.set(data).get();
        } catch (Exception e) {
            throw new RuntimeException("Firestore 저장 실패", e);
        }
    }

    public String getManagerRegion(String region) {
        Firestore fs = FirestoreClient.getFirestore();
        CollectionReference managers = fs.collection("Manager");
        ApiFuture<QuerySnapshot> query = managers.whereEqualTo("region", region).get();
        try {
            List<QueryDocumentSnapshot> docs = query.get().getDocuments();
            if (!docs.isEmpty()) {
                return docs.get(0).getString("region");
            } else {
                throw new RuntimeException("해당 관리자 region 정보 없음");
            }
        } catch (Exception e) {
            throw new RuntimeException("Firestore에서 region 정보 조회 실패", e);
        }
    }

    public List<QueryDocumentSnapshot> getAllConclusions() {
        Firestore fs = FirestoreClient.getFirestore();
        CollectionReference conclusions = fs.collection(COLLECTION_CONCLUSION);
        try {
            return conclusions.get().get().getDocuments();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Conclusion 전체 조회 중 오류", e);
        }
    }
}