package com.ancientcharmoffujianstyle.utils;

import com.ancientcharmoffujianstyle.domain.query.CheckInQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class UploadUtil {

    // 从配置文件读取存储路径
    @Value("${upload.path}")
    private String uploadPath;

    // 从配置文件读取访问前缀
    @Value("${upload.access-path}")
    private String accessPath;

    /**
     * 图片上传（文件名：毫秒时间戳+后缀）
     * @param file 上传的图片文件
     * @return 图片访问URL（前端用于展示）
     */
    public String uploadImage(MultipartFile file, CheckInQuery checkInQuery) throws IOException {
        // 1. 校验文件是否为空
        if (file.isEmpty()) {
            throw new RuntimeException("上传的图片不能为空");
        }

        // 2. 校验文件类型（仅允许jpg、png、jpeg）
        String originalFilename = file.getOriginalFilename();
        // 防止文件名中没有后缀
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new RuntimeException("图片文件名格式不正确");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!".jpg".equalsIgnoreCase(suffix) && !".png".equalsIgnoreCase(suffix) && !".jpeg".equalsIgnoreCase(suffix)) {
            throw new RuntimeException("仅支持jpg、png、jpeg格式的图片");
        }

        // 3. 生成文件名：毫秒时间戳+后缀（核心修改：替换UUID为System.currentTimeMillis()）
        long timeStamp = System.currentTimeMillis(); // 13位毫秒级时间戳，确保唯一
        String fileName = timeStamp +checkInQuery.getUserId()+ suffix;

        // 4. 创建存储目录（若目录不存在则自动创建）
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 5. 保存文件到本地
        File destFile = new File(uploadPath + fileName);
        file.transferTo(destFile);

        // 6. 返回图片访问URL
        return accessPath + fileName;
    }

    /**
     * 图片上传（文件名：毫秒时间戳+后缀）
     * @param file 上传的图片文件
     * @return 图片访问URL（前端用于展示）
     */
    public String uploadface(MultipartFile file,String studentName) throws IOException {
        // 1. 校验文件是否为空
        if (file.isEmpty()) {
            throw new RuntimeException("上传的图片不能为空");
        }

        // 2. 校验文件类型（仅允许jpg、png、jpeg）
        String originalFilename = file.getOriginalFilename();
        // 防止文件名中没有后缀
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new RuntimeException("图片文件名格式不正确");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (!".jpg".equalsIgnoreCase(suffix) && !".png".equalsIgnoreCase(suffix) && !".jpeg".equalsIgnoreCase(suffix)) {
            throw new RuntimeException("仅支持jpg、png、jpeg格式的图片");
        }


        String fileName = studentName+ suffix;


        // 5. 保存文件到本地
        File destFile = new File(uploadPath+"face/"+ fileName);
        file.transferTo(destFile);

        // 6. 返回图片访问URL
        return accessPath +"face/"+ fileName;
    }
}