package com.br.infnet.userservice.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service implements BucketStorageService {
    private final S3Client s3Client;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @Override
    public List<String> listarImagensDisponiveis() {
        try {
            s3Client.headBucket(b -> b.bucket(bucketName));

            ListObjectsV2Response response = s3Client.listObjectsV2(r -> r.bucket(bucketName));

            return response.contents().stream()
                    .map(S3Object::key)
                    .map(this::generatePublicUrl)
                    .toList();

        } catch (S3Exception e) {
            log.error("S3 error: {}", e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Erro no S3/MinIO: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error", e);
            throw new RuntimeException("Erro ao listar avatares do bucket S3/MinIO", e);
        }
    }

    private String generatePublicUrl(String key) {
        return s3Client.utilities().getUrl(b -> b.bucket(bucketName).key(key)).toString();
    }
}