package com.sky.config;

import com.sky.properties.MinIOProperties;
import com.sky.utils.MinIOUtil;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinIOConfiguration {

    @Bean
    public MinIOUtil minIOUtil(MinIOProperties minIOProperties){
        log.info("minio start:{}", minIOProperties);
        return new MinIOUtil(minIOProperties.getEndpoint(),
                minIOProperties.getAccessKey(),
                minIOProperties.getSecretKey(),
                minIOProperties.getBucketName());
    }
}
