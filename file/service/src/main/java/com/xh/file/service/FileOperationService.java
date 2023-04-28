package com.xh.file.service;

import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import com.xh.file.client.entity.SysFile;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件操作service
 * sunxh 2023/4/22
 */
@Service
@Slf4j
public class FileOperationService extends BaseServiceImpl {

    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;
    @Value("${minio.bucket}")
    private String bucket;

    /**
     * 上传单个文件
     */
    @Transactional
    public SysFile uploadFile(MultipartFile multipartFile) {
        try {
            // Create a minioClient with the MinIO server playground, its access key and secret key.
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(endpoint)
                            .credentials(accessKey, secretKey)
                            .build();

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            String filename = multipartFile.getOriginalFilename();
            DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
            multipartFile.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String date = yyyyMMdd.format(LocalDateTime.now());
            //文件后缀名
            String object = "%s/%s".formatted(date, uuid);
            String suffix = CommonUtil.getFileSuffix(filename);
            if(CommonUtil.isNotEmpty(suffix)) object += "." + suffix;
            SysFile sysFile = new SysFile();
            sysFile.setObject(object);
            sysFile.setSize(multipartFile.getSize());
            sysFile.setSuffix(suffix);
            sysFile.setContentType(multipartFile.getContentType());
            sysFile.setName(filename);
            try {
                // 图片对象
                BufferedImage image = ImageIO.read(multipartFile.getInputStream());
                sysFile.setImgWidth(image.getWidth());
                sysFile.setImgHeight(image.getHeight());
                sysFile.setImgRatio(image.getWidth() / new BigDecimal(image.getHeight()).doubleValue());
            } catch (Exception e) {
                log.info("非图片文件");
            }
            baseJdbcDao.insert(sysFile);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(object)
                            .contentType(multipartFile.getContentType())
                            .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                            .build()
            );
            return sysFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
