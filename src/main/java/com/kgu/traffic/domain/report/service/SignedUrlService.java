package com.kgu.traffic.domain.report.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SignedUrlService {

    private final Storage storage;

    /**
     * Firebase Storage / GCS URL → 일정 시간 유효한 V4 서명 URL로 변환
     */
    public String toSignedUrl(String anyUrl, Duration ttl) {
        if (anyUrl == null || anyUrl.isBlank()) return null;

        ParsedGcsPath p = parseGcsPath(anyUrl);
        if (p == null) {
            return anyUrl;
        }

        BlobInfo blob = BlobInfo.newBuilder(p.bucket(), p.object()).build();

        Map<String, String> headers = new HashMap<>();
        URL url = storage.signUrl(
                blob,
                Math.max(1, ttl.toMinutes()),
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.GET),
                Storage.SignUrlOption.withV4Signature(),
                Storage.SignUrlOption.withExtHeaders(headers)
        );
        return url.toString();
    }

    private ParsedGcsPath parseGcsPath(String anyUrl) {
        try {
            if (anyUrl.startsWith("gs://")) {
                String noScheme = anyUrl.substring("gs://".length());
                int idx = noScheme.indexOf('/');
                if (idx < 0) return null;
                String bucket = noScheme.substring(0, idx);
                String object = noScheme.substring(idx + 1);
                return new ParsedGcsPath(bucket, object);
            }
            URI uri = URI.create(anyUrl);
            String host = uri.getHost();
            if (host == null) return null;
            String path = uri.getPath() == null ? "" : uri.getPath();
            if ("storage.googleapis.com".equals(host)) {
                String trimmed = path.startsWith("/") ? path.substring(1) : path;
                int idx = trimmed.indexOf('/');
                if (idx < 0) return null;
                String bucket = trimmed.substring(0, idx);
                String object = trimmed.substring(idx + 1);
                return new ParsedGcsPath(bucket, object);
            }
            if ("firebasestorage.googleapis.com".equals(host)) {
                return parseFirebaseApiPath(path);
            }
            if (host.endsWith("firebasestorage.app")) {
                ParsedGcsPath p = parseFirebaseApiPath(path);
                if (p != null) return p;
                String trimmed = path.startsWith("/") ? path.substring(1) : path;
                int idx = trimmed.indexOf('/');
                if (idx > 0) {
                    String bucket = trimmed.substring(0, idx);
                    String object = trimmed.substring(idx + 1);
                    if (!bucket.isBlank() && !object.isBlank()) {
                        return new ParsedGcsPath(bucket, object);
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    private ParsedGcsPath parseFirebaseApiPath(String path) {
        String p = path;
        if (p.startsWith("/v0")) {
            p = p.substring(3);
        }
        String[] parts = p.split("/");
        if (parts.length >= 5 && "b".equals(parts[1]) && "o".equals(parts[3])) {
            String bucket = parts[2];
            String objectEncoded = p.substring(p.indexOf("/o/") + 3);
            int q = objectEncoded.indexOf('?');
            if (q >= 0) objectEncoded = objectEncoded.substring(0, q);
            String object = java.net.URLDecoder.decode(objectEncoded, java.nio.charset.StandardCharsets.UTF_8);
            return new ParsedGcsPath(bucket, object);
        }
        return null;
    }
    private record ParsedGcsPath(String bucket, String object) {}
}