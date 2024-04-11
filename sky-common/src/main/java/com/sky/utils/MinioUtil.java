package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Data
@AllArgsConstructor
@Slf4j
public class MinIOUtil {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    // ... 其他构造方法或设置字段的setter方法

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {
        try {
            // 创建MinioClient实例
            MinioClient minioClient = new MinioClient(endpoint, accessKey, secretKey);

            // 设置上传的参数
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType("application/octet-stream")
                    .build();

            // 上传文件
            minioClient.putObject(args);

            // 文件访问路径规则
            String fileUrl = endpoint +"/"+ bucketName + "/" +  URLEncoder.encode(objectName, StandardCharsets.UTF_8.toString());

            // 打印或记录日志
            System.out.println("文件上传到: " + fileUrl);

            return fileUrl;
        } catch (MinioException e) {
            System.out.println("上传文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("发生未知错误: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
