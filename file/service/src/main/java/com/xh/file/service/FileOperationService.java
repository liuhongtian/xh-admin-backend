package com.xh.file.service;

import com.xh.common.core.service.BaseServiceImpl;
import com.xh.common.core.utils.CommonUtil;
import com.xh.common.core.utils.WebLogs;
import com.xh.common.core.web.MyException;
import com.xh.common.core.web.PageQuery;
import com.xh.common.core.web.PageResult;
import com.xh.file.client.dto.DownloadFileDTO;
import com.xh.file.client.entity.SysFile;
import io.minio.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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

    @Resource
    private PlatformTransactionManager platformTransactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;

    /**
     * 上传单个文件
     */
    @Transactional
    public SysFile uploadFile(MultipartFile multipartFile) {
        try {
            //获取文件摘要sha1
            String sha1 = CommonUtil.getFileSha1(multipartFile.getInputStream());
            String sql2 = "delete from sys_file where sha1 = ? and status = 4";
            primaryJdbcTemplate.update(sql2, sha1);
            String sql = "select * from sys_file where sha1 = ?";
            //利用sha1判断文件是否一致，sha1查询文件是否已上传过，已上传则节省空间直接返回sysFile对象
            SysFile sysFile = baseJdbcDao.findBySql(SysFile.class, sql, sha1);
            if (sysFile != null) return sysFile;
            sysFile = new SysFile();
            MinioClient minioClient = getMinioClient();
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
            String filename = multipartFile.getOriginalFilename();
            DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
            multipartFile.getOriginalFilename();
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            String date = yyyyMMdd.format(LocalDateTime.now());
            //文件后缀名
            String object = "%s/%s".formatted(date, uuid);
            String suffix = CommonUtil.getFileSuffix(filename);
            if (CommonUtil.isNotEmpty(suffix)) object += "." + suffix;
            sysFile.setObject(object);
            sysFile.setSize(multipartFile.getSize());
            sysFile.setSuffix(suffix);
            sysFile.setContentType(multipartFile.getContentType());
            sysFile.setName(filename);
            try {
                // 判断文件是否为图片文件，图片文件获取宽高，横纵比
                BufferedImage image = ImageIO.read(multipartFile.getInputStream());
                sysFile.setImgWidth(image.getWidth());
                sysFile.setImgHeight(image.getHeight());
                sysFile.setImgRatio(image.getWidth() / new BigDecimal(image.getHeight()).doubleValue());
            } catch (Exception e) {
                log.info("非图片文件");
            }
            sysFile.setStatus(1);
            sysFile.setSha1(sha1);
            baseJdbcDao.insert(sysFile);

            //上传至文件存储服务器
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

    /**
     * 文件下载
     */
    @Transactional
    public void downloadFile(DownloadFileDTO downloadFileDTO, HttpServletResponse response) {
        MinioClient minioClient = getMinioClient();
        if(CommonUtil.isEmpty(downloadFileDTO.getObject())) {
            if(downloadFileDTO.getId() == null) throw new MyException("参数异常");
            SysFile sysFile = baseJdbcDao.findById(SysFile.class, downloadFileDTO.getId());
            downloadFileDTO.setObject(sysFile.getObject());
            downloadFileDTO.setContentType(sysFile.getContentType());
            if(CommonUtil.isEmpty(downloadFileDTO.getFileName())){
                downloadFileDTO.setFileName(sysFile.getName());
            }
        }
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(downloadFileDTO.getObject())
                        .build())) {
            response.setCharacterEncoding("UTF-8");
            if (CommonUtil.isNotEmpty(downloadFileDTO.getFileName())) {
                String fileName = URLEncoder.encode(downloadFileDTO.getFileName(), "UTF-8");
                //设置响应头中文件的下载方式为附件方式，以及设置文件名
                response.setHeader("Content-Disposition",
                        "%s; filename=%s".formatted(downloadFileDTO.getDisposition(), fileName));
            }
            if (CommonUtil.isNotEmpty(downloadFileDTO.getContentType())) {
                //设置Content-Type，这样可以使用浏览器原生的文件预览功能
                response.setContentType(downloadFileDTO.getContentType());
            }
            OutputStream outputStream = response.getOutputStream();
            //如果图片预览缩略图
            if (downloadFileDTO.getIsScale()) {
                try {
                    //缩略图长边大小
                    double scaleWidth = downloadFileDTO.getScaleWidth();
                    // 判断文件是否为图片文件，图片文件获取宽高，横纵比
                    BufferedImage image = ImageIO.read(inputStream);
                    int maxWidth = Math.max(image.getWidth(), image.getHeight());
                    if (maxWidth > scaleWidth) {
                        double scale = scaleWidth / maxWidth;
                        int newWidth = (int) (image.getWidth() * scale);
                        int newHeight = (int) (image.getHeight() * scale);
                        BufferedImage newBI = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                        newBI.getGraphics().drawImage(image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
                        ImageIO.write(newBI, "webp", outputStream);
                        return;
                    } else {
                        ImageIO.write(image, "webp", outputStream);
                    }
                } catch (Exception e) {
                    log.error("图片缩放失败，{}", e.getMessage());
                }
            }
            inputStream.transferTo(outputStream);
            outputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 系统文件查询
     */
    @Transactional(readOnly = true)
    public PageResult<SysFile> query(PageQuery<Map<String, Object>> pageQuery) {
        WebLogs.info("文件列表查询---");
        Map<String, Object> param = pageQuery.getParam();
        String sql = "select * from sys_file where status <> 4 ";
        if (CommonUtil.isNotEmpty(param.get("object"))) {
            sql += " and object like '%' ? '%'";
            pageQuery.addArg(param.get("object"));
        }
        if (CommonUtil.isNotEmpty(param.get("name"))) {
            sql += " and name like '%' ? '%'";
            pageQuery.addArg(param.get("name"));
        }
        if (CommonUtil.isNotEmpty(param.get("contentType"))) {
            sql += " and content_type like  '%' ? '%'";
            pageQuery.addArg(param.get("contentType"));
        }
        if (CommonUtil.isNotEmpty(param.get("suffix"))) {
            sql += " and suffix = ? ";
            pageQuery.addArg(param.get("suffix"));
        }
        if (CommonUtil.isNotEmpty(param.get("sha1"))) {
            sql += " and sha1 = ? ";
            pageQuery.addArg(param.get("sha1"));
        }
        if (CommonUtil.isNotEmpty(param.get("type"))) {
            sql += " and content_type like  ? '%' ";
            pageQuery.addArg(param.get("type"));
        }
        sql += " order by create_time desc";
        pageQuery.setBaseSql(sql);
        return baseJdbcDao.query(SysFile.class, pageQuery);
    }

    @Transactional
    public SysFile save(SysFile sysFile) {
        WebLogs.getLogger().info("文件保存---");
        if (sysFile.getId() == null) baseJdbcDao.insert(sysFile);
        else baseJdbcDao.update(sysFile);
        return sysFile;
    }

    /**
     * id获取文件详情
     */
    @Transactional(readOnly = true)
    public SysFile getById(Serializable id) {
        return baseJdbcDao.findById(SysFile.class, id);
    }

    /**
     * id删除文件
     */
    public void del(String ids) {
        String sql = "select * from sys_file where id in (%s)".formatted(ids);
        List<SysFile> list = baseJdbcDao.findList(SysFile.class, sql);
        MinioClient minioClient = getMinioClient();
        for (SysFile sysFile : list) {
            TransactionStatus transactionStatus = platformTransactionManager.getTransaction(transactionDefinition);
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(sysFile.getObject())
                        .build());
                sysFile.setStatus(4);//已删除
                baseJdbcDao.update(sysFile);
                platformTransactionManager.commit(transactionStatus); //提交事务
            } catch (Exception e) {
                log.error("删除文件出错：{}", e.getMessage());
                platformTransactionManager.rollback(transactionStatus);//回滚事务
                throw new MyException(e);
            }
        }
    }

    private MinioClient getMinioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
