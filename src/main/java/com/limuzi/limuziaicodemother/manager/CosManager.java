package com.limuzi.limuziaicodemother.manager;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.limuzi.limuziaicodemother.config.CosClientConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * COS对象存储管理器
 *
 * @author yupi
 */
@Component
@Slf4j
public class CosManager {

    private static final String BASE_DIR_NAME = "limuzi-ai-code-mother";

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private OSS ossClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResult putObjects(String key, File file) {
        try {
            // 创建PutObject请求。
            return ossClient.putObject(cosClientConfig.getBucket(), key, file);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            return null;
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
            return null;
        }
    }

    /**
     * 上传文件到 COS 并返回访问 URL
     *
     * @param key  COS对象键（完整路径）
     * @param file 要上传的文件
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFile(String key, File file) {
        log.info("开始上传文件到COS: {}", key);
        // 上传文件
        PutObjectResult result = putObjects(key, file);
        if (result != null) {
            // 构建访问URL
            String url = String.format("https://%s.%s/%s", cosClientConfig.getBucket(), cosClientConfig.getHost(), key);
            log.info("文件上传COS成功: {} -> {}", file.getName(), url);
            return url;
        } else {
            log.error("文件上传COS失败，返回结果为空");
            return null;
        }
    }

    public String uploadFile(String key, InputStream in, long contentLength, String contentType) {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(contentLength);
        meta.setContentType(contentType);
        PutObjectRequest req = new PutObjectRequest(cosClientConfig.getBucket(), key, in, meta);
        ossClient.putObject(req);
        return String.format("https://%s.%s/%s", cosClientConfig.getBucket(), cosClientConfig.getHost(), key);
    }


    /**
     * 删除桶中文件
     * @param url 文件访问URL
     */
    public void deleteFile(String url) {
        if (url.split("com/").length>=2){
            String key = url.split("com/")[1];
            ossClient.deleteObject(cosClientConfig.getBucket(), key);
            log.info("删除COS文件成功: {}", key);
        }
    }

    /**
     * 生成文件名
     * @param fileName 文件名
     * @return 文件名
     */
    public String generateKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format(BASE_DIR_NAME +"/avatar/%s/%s", datePath, fileName);
    }
}
