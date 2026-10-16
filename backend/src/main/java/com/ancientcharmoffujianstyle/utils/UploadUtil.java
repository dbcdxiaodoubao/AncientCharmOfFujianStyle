package com.ancientcharmoffujianstyle.utils;

import com.ancientcharmoffujianstyle.domain.query.CheckInQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class UploadUtil {
    @Value("${upload.path:./uploads}")
    private String uploadPath;
    @Value("${upload.access-path:images/}")
    private String accessPath;

    public String uploadImage(MultipartFile file, CheckInQuery query) throws IOException {
        return save(file, "");
    }

    public String uploadface(MultipartFile file, String studentName) throws IOException {
        return save(file, "face/");
    }

    private String save(MultipartFile file, String subdirectory) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("上传的图片不能为空");
        if (file.getSize() > 5 * 1024 * 1024) throw new IllegalArgumentException("图片不能超过5MB");
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase(java.util.Locale.ROOT).matches(".*\\.(jpg|jpeg|png)$")) {
            throw new IllegalArgumentException("仅支持jpg、png、jpeg格式的图片");
        }
        try (InputStream stream = file.getInputStream()) {
            if (ImageIO.read(stream) == null) throw new IllegalArgumentException("图片内容无效");
        }
        String suffix = name.substring(name.lastIndexOf('.')).toLowerCase(java.util.Locale.ROOT);
        Path directory = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(subdirectory);
        Files.createDirectories(directory);
        Path destination = directory.resolve(UUID.randomUUID() + suffix);
        try {
            file.transferTo(destination.toFile());
        } catch (IOException | RuntimeException exception) {
            Files.deleteIfExists(destination);
            throw exception;
        }
        return accessPath.replaceAll("/+$", "") + "/" + subdirectory + destination.getFileName();
    }
}
