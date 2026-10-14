package com.example.serviciopagos.Config;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class SqsConfig {
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.credentials.session-token:}")
    private String sessionToken;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        software.amazon.awssdk.auth.credentials.AwsCredentials credentials;
        if (sessionToken != null && !sessionToken.isEmpty()) {
            // Credenciales temporales con session token (AWS Academy)
            credentials = AwsSessionCredentials.create(accessKey, secretKey, sessionToken);
        } else {
            // Credenciales permanentes
            credentials = AwsBasicCredentials.create(accessKey, secretKey);
        }

        return SqsAsyncClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build();
    }

    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.builder()
            .sqsAsyncClient(sqsAsyncClient)
            .build();
    }
}
