package main.blog.domain.service;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import main.blog.domain.dto.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioService {

    @Value("${minio.access.name}")
    private String accessKey;

    @Value("${minio.access.secret}")
    private String accessSecret;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.bucket.name}")
    private String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, accessSecret)
                .build();
    }

    public String uploadFile(MultipartFile file, String filename) throws MinioException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        String uploadFilename = getFileName(filename);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(uploadFilename).stream(
                                    file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return "File uploaded successfully. File name: " + filename;
        } catch (MinioException e) {
            throw new MinioException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException(e);
        } catch (InvalidKeyException e) {
            throw new InvalidKeyException(e);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public String uploadTusFile(InputStream fileIo, String filename, long fileSize, String contentType) throws MinioException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        String uploadFilename = getFileName(filename);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(uploadFilename).stream(
                                    fileIo, fileSize, -1)
                            .contentType(contentType)
                            .build());
            return "File uploaded successfully. File name: " + filename;
        } catch (MinioException e) {
            throw new MinioException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException(e);
        } catch (InvalidKeyException e) {
            throw new InvalidKeyException(e);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public boolean deleteTusFile( String filename) throws MinioException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        String uploadFilename = getFileName(filename);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket(bucketName).object(uploadFilename)
                            .build());
            return true;
        } catch (MinioException e) {
            throw new MinioException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException(e);
        } catch (InvalidKeyException e) {
            throw new InvalidKeyException(e);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public boolean findFile(String filename) {
        try {
            String uploadFilename = getFileName(filename);
            StatObjectResponse objectStat = minioClient.statObject(
                            StatObjectArgs.builder().bucket(bucketName).object(uploadFilename)
                                    .build());
            return true;
        } catch (ServerException e) {
            throw new RuntimeException(e);
        } catch (InsufficientDataException e) {
            throw new RuntimeException(e);
        } catch (ErrorResponseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidResponseException e) {
            throw new RuntimeException(e);
        } catch (XmlParserException e) {
            throw new RuntimeException(e);
        } catch (InternalException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<InputStreamResource> downloadFile(String filename) {
        String uploadFilename = getFileName(filename);
        try {
            InputStreamResource resource = new InputStreamResource(minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketName).object(uploadFilename).build()));

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", filename));

            return ResponseEntity.ok()
                    .headers(headers)
                    //.contentType(MediaType.IMAGE_PNG)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (ServerException e) {
            throw new RuntimeException(e);
        } catch (InsufficientDataException e) {
            throw new RuntimeException(e);
        } catch (ErrorResponseException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (InvalidResponseException e) {
            throw new RuntimeException(e);
        } catch (XmlParserException e) {
            throw new RuntimeException(e);
        } catch (InternalException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileName(String filename) {
        CustomUserDetails customUserDetails = getAuthenticatedUserDetail();
        String[] username = customUserDetails.getUsername().split("@");

        return username[0] + "/" + filename;
    }

    private static CustomUserDetails getAuthenticatedUserDetail() {
        Authentication authentication
                = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new BadCredentialsException("로그인을 해주세요.");
        }
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails;
    }
}
