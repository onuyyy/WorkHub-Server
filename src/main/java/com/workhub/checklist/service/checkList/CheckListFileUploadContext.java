package com.workhub.checklist.service.checkList;

import com.workhub.file.dto.FileUploadResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Multipart 업로드 결과를 원본 파일명 기준으로 매핑하여
 * 체크리스트 항목/선택지 생성 및 수정 시 필요한 파일을 찾아주는 유틸 컨텍스트.
 */
@Slf4j
class CheckListFileUploadContext {

    private final Map<String, Deque<FileUploadResponse>> uploadsByOriginalName;
    private final List<String> uploadedFileNames;

    /**
     * uploadedFileNames : 업로드 결과 리스트를 받아서 S3에 실제 저장된 파일명들을 미리 저장,
     * uploadsByOriginalName : 원본 파일명 -> 업로드 결과들
     * @param uploads 업로드 결과 리스트
     */
    CheckListFileUploadContext(List<FileUploadResponse> uploads) {
        this.uploadedFileNames = (uploads == null)
                ? List.of()
                : uploads.stream().map(FileUploadResponse::fileName).toList();

        this.uploadsByOriginalName = new HashMap<>();
        if (uploads == null) {
            return;
        }

        for (FileUploadResponse upload : uploads) {
            String key = normalize(upload.originalFileName());
            if (key == null) {
                log.warn("원본 파일명이 null이거나 공백인 업로드 파일 무시: {}", upload.fileName());
                continue;
            }

            Deque<FileUploadResponse> existing = uploadsByOriginalName.get(key);
            if (existing != null && !existing.isEmpty()) {
                log.info("동일한 원본 파일명 '{}' 감지 (중복 업로드 {}개)", key, existing.size() + 1);
            }

            uploadsByOriginalName
                    .computeIfAbsent(key, ignore -> new ArrayDeque<>())
                    .add(upload);
        }

        if (!uploadsByOriginalName.isEmpty()) {
            log.debug("체크리스트 파일 업로드 컨텍스트 생성: {} 개 원본 파일명, 총 {} 개 파일",
                    uploadsByOriginalName.size(), uploadedFileNames.size());
        }
    }

    /**
     * DTO 에서 식별자를 넘기면 공백, null 정리
     * uploadsByOriginalName 에서 동일 키를 찾아 업로드 결과 꺼냄
     * 큐가 비면 제거하고, 없으면 빈 Optional로 반환
     *  -> 신규 업로드 파일이 아니라 존재 하지 않음을 전달
     */
    Optional<FileUploadResponse> consume(String identifier) {
        String key = normalize(identifier);
        if (key == null) {
            return Optional.empty();
        }

        Deque<FileUploadResponse> deque = uploadsByOriginalName.get(key);
        if (deque == null || deque.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("파일 소비 실패 - identifier: '{}', 사용 가능한 파일: {}",
                        key, uploadsByOriginalName.keySet());
            }
            return Optional.empty();
        }

        FileUploadResponse response = deque.pollFirst();
        if (deque.isEmpty()) {
            uploadsByOriginalName.remove(key);
        }

        if (log.isTraceEnabled()) {
            log.trace("파일 소비 성공 - identifier: '{}', S3 파일명: '{}'",
                    key, response.fileName());
        }

        return Optional.ofNullable(response);
    }

    /**
     * 누락된 선택지 찾음
     */
    boolean hasUnconsumedFiles() {
        return uploadsByOriginalName.values().stream().anyMatch(deque -> !deque.isEmpty());
    }

    /**
     * 미소비 파일 목록 반환 (디버깅용)
     */
    List<String> getUnconsumedFileNames() {
        return uploadsByOriginalName.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * 업로드된 S3 파일명 전체를 복사해서 돌려줌
     */
    List<String> getUploadedFileNames() {
        return new ArrayList<>(uploadedFileNames);
    }

    /**
     * 키 비교를 위한 null/공백 정리
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
