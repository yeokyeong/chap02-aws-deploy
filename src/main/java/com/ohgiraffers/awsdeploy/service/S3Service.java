package com.ohgiraffers.awsdeploy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

/**
 * S3 파일 업로드 서비스
 * Amazon S3를 사용한 파일 업로드, 삭제, URL 생성 기능을 제공한다
 */
@Service
public class S3Service {

    // S3 클라이언트
    private final S3Client s3Client;
    // S3 버킷 이름
    private final String bucketName;
    // S3 리전
    private final String region;

	/* S3Service 생성자를 만들 때 아래 4가지 파라미터를 받는다.
	 * 1. AWS 액세스 키
	 * 2. AWS 시크릿 키
	 * 3. S3 버킷 이름
	 * 4. S3 리전
	 * 이 값들은 환경변수에 저장되어 있으며, 환경변수는 .env(또는 .env.example) 파일에 저장되어 있다.
	 * 간단한 테스트를 위해 IDE에서 Run Profile의 Edit Configurations에서 환경변수를 설정한다.
	 */
    public S3Service(@Value("${aws.credentials.access-key}") String accessKey,
                     @Value("${aws.credentials.secret-key}") String secretKey,
                     @Value("${aws.s3.bucket}") String bucketName,
                     @Value("${aws.region}") String region) {
        
        this.bucketName = bucketName;
        this.region = region;
        
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    /**
     * S3에 파일을 업로드한다
     * @param file 업로드할 파일
     * @return S3 객체 키 (파일명)
     */
    public String uploadFile(MultipartFile file) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    /**
     * S3에서 파일을 삭제한다
     * @param fileName 삭제할 파일명
     */
    public void deleteFile(String fileName) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            
            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    /**
     * 업로드된 파일의 전체 URL을 반환한다
     * @param fileName 파일명
     * @return S3 객체의 전체 URL
     */
    public String getFileUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
    }

    /**
     * 고유한 파일명을 생성한다
     * @param originalFilename 원본 파일명
     * @return 고유한 파일명
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
} 