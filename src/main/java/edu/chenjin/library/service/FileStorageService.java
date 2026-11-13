package edu.chenjin.library.service;

import edu.chenjin.library.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg");

    private final Path root;

    public FileStorageService(@Value("${app.upload-dir:./uploads}") String uploadDir) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root.resolve("covers"));
            Files.createDirectories(root.resolve("gallery"));
        } catch (IOException e) {
            throw new IllegalStateException("无法创建图片上传目录：" + root, e);
        }
    }

    public StoredFile store(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的图片");
        }
        String original = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String extension = extensionOf(original);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BizException("仅支持 jpg、png、gif、webp、bmp、svg 格式的图片");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BizException("上传的文件不是图片");
        }
        if (file.getSize() > 8 * 1024 * 1024) {
            throw new BizException(413, "单张图片不能超过 8MB");
        }

        String safeCategory = "covers".equals(category) ? "covers" : "gallery";
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = root.resolve(safeCategory).resolve(fileName).normalize();
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BizException("图片保存失败，请稍后重试");
        }
        return new StoredFile(
                fileName,
                original,
                "/uploads/" + safeCategory + "/" + fileName,
                contentType,
                file.getSize()
        );
    }

    public void deleteByUrl(String url) {
        if (!StringUtils.hasText(url) || !url.startsWith("/uploads/")) {
            return;
        }
        String relative = url.substring("/uploads/".length());
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // 文件被占用时不影响数据库记录删除
        }
    }

    private String extensionOf(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index < 0 ? "" : fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    public record StoredFile(String fileName, String originalName, String url,
                             String contentType, long size) {
    }
}
