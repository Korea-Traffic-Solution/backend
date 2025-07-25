package com.kgu.traffic.domain.auth.service;


public interface FirestoreService {
    void saveManagerToFirestore(String name, String email, String region, String classname);
}