package com.whatsyouretf.userservice.common.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 저장 서비스 인터페이스
 * <p>
 * Strategy 패턴으로 구현하여 저장소 변경 용이
 * - LocalFileStorageService: 로컬 서버 저장
 * - S3FileStorageService: AWS S3 저장 (추후 구현)
 */
public interface FileStorageService {

    /**
     * 파일 업로드
     *
     * @param file      업로드할 파일
     * @param directory 저장 디렉토리 (예: "profiles", "posts")
     * @return 접근 가능한 파일 URL
     */
    String upload(MultipartFile file, String directory);

    /**
     * 파일 삭제
     *
     * @param fileUrl 삭제할 파일의 URL
     * @return 삭제 성공 여부
     */
    boolean delete(String fileUrl);

    /**
     * 파일 존재 여부 확인
     *
     * @param fileUrl 확인할 파일의 URL
     * @return 존재 여부
     */
    boolean exists(String fileUrl);

    /**
     * 저장소 타입 반환
     *
     * @return 저장소 타입 (LOCAL, S3 등)
     */
    String getStorageType();
}
