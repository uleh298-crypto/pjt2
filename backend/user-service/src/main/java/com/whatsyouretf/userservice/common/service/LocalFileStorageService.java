package com.whatsyouretf.userservice.common.service;

import com.whatsyouretf.userservice.common.exception.BusinessException;
import com.whatsyouretf.userservice.common.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * 로컬 파일 시스템 저장소 구현체
 * <p>
 * 개발 환경 또는 단일 서버 환경에서 사용
 * 운영 환경에서는 S3FileStorageService로 교체 권장
 */
@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    private static final String STORAGE_TYPE = "LOCAL";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:http://localhost:8080}")
    private String baseUrl;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            log.info("파일 업로드 디렉토리 생성: {}", uploadPath);
        } catch (IOException e) {
            log.error("업로드 디렉토리 생성 실패: {}", uploadPath, e);
            throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다.", e);
        }
    }

    @Override
    public String upload(MultipartFile file, String directory) {
        validateFile(file);

        try {
            // 디렉토리 생성
            Path dirPath = uploadPath.resolve(directory);
            Files.createDirectories(dirPath);

            // 고유 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + extension;

            // 파일 저장
            Path targetPath = dirPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("파일 업로드 완료: {}", targetPath);

            // 접근 URL 반환
            return baseUrl + "/uploads/" + directory + "/" + newFilename;

        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Override
    public boolean delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return false;
        }

        try {
            // URL에서 파일 경로 추출
            String relativePath = extractRelativePath(fileUrl);
            if (relativePath == null) {
                log.warn("유효하지 않은 파일 URL: {}", fileUrl);
                return false;
            }

            Path filePath = uploadPath.resolve(relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("파일 삭제 완료: {}", filePath);
                return true;
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", filePath);
                return false;
            }

        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", fileUrl, e);
            return false;
        }
    }

    @Override
    public boolean exists(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return false;
        }

        String relativePath = extractRelativePath(fileUrl);
        if (relativePath == null) {
            return false;
        }

        Path filePath = uploadPath.resolve(relativePath);
        return Files.exists(filePath);
    }

    @Override
    public String getStorageType() {
        return STORAGE_TYPE;
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * URL에서 상대 경로 추출
     * 예: http://localhost:8080/uploads/profiles/abc.jpg -> profiles/abc.jpg
     */
    private String extractRelativePath(String fileUrl) {
        String marker = "/uploads/";
        int index = fileUrl.indexOf(marker);
        if (index == -1) {
            return null;
        }
        return fileUrl.substring(index + marker.length());
    }
}
