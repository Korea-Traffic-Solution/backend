// src/main/java/com/kgu/traffic/domain/report/service/FirestoreService.java
package com.kgu.traffic.domain.report.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {

    private static final String COLLECTION_CONCLUSION = "Conclusion";
    private static final String COLLECTION_MANAGER = "Manager";

    // KST 포맷터 (콘솔 표시와 동일한 형태: "2025년 8월 1일 오후 4시 31분 12초")
    private static final DateTimeFormatter KST_FMT =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h시 m분 s초", Locale.KOREAN)
                    .withZone(ZoneId.of("Asia/Seoul"));

    /** Conclusion 단건 조회 + date를 KST 문자열로 변환해서 반환 */
    public Map<String, Object> getConclusionByDocId(String docId) {
        Firestore fs = FirestoreClient.getFirestore();
        DocumentReference docRef = fs.collection(COLLECTION_CONCLUSION).document(docId);
        try {
            DocumentSnapshot document = docRef.get().get();
            if (!document.exists()) {
                throw new RuntimeException("Conclusion 문서가 존재하지 않음: " + docId);
            }
            Map<String, Object> data = document.getData();

            // date가 Timestamp면 KST 문자열로 변환
            if (data != null) {
                Object dateObj = data.get("date");
                if (dateObj instanceof Timestamp ts) {
                    String kst = KST_FMT.format(ts.toDate().toInstant());
                    data.put("date", kst);
                }
            }
            return data;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Conclusion 조회 중 오류", e);
        }
    }

    public String getManagerRegion(String region) {
        Firestore fs = FirestoreClient.getFirestore();
        CollectionReference managers = fs.collection(COLLECTION_MANAGER);
        ApiFuture<QuerySnapshot> query = managers.whereEqualTo("region", region).get();
        try {
            List<QueryDocumentSnapshot> docs = query.get().getDocuments();
            if (!docs.isEmpty()) {
                return docs.get(0).getString("region");
            }
            throw new RuntimeException("해당 관리자 region 정보 없음");
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
            Thread.currentThread().interrupt();
            throw new RuntimeException("Conclusion 전체 조회 중 오류", e);
        }
    }
}