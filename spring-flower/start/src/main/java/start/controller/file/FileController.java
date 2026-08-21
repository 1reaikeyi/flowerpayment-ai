package start.controller.file;

import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StreamUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/local")
@Slf4j
public class FileController {

    //本地存储 private数据 ku
    private static final String PATH = "ku/image";

    @PostMapping
    public Result upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        File local = new File(PATH);
        File path = new File(local.getAbsolutePath());
        if (!path.exists()) {
            path.mkdirs();
        }
        log.info("文件路径::{}", path);
        String saveName = UUID.randomUUID().toString() +
                originalFilename.substring(originalFilename.lastIndexOf("."));
        try {
            file.transferTo(new File(path, saveName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("文件上传成功: {} {}", path, saveName);
        return Result.success(path+"::" + saveName);
    }
    /**
     * 下载文件
     */
    @GetMapping
    public void download(String fileName, HttpServletResponse response) {
        File local = new File(PATH);
        File path = new File(local.getAbsolutePath(), fileName);
        try {
            if (!path.exists()) {
                throw new IllegalArgumentException(path + "文件不存在");
            }
            response.setContentType("application/octet-stream");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);
            try (InputStream fileInputStream = new FileInputStream(path)) {
                StreamUtils.copy(fileInputStream, response.getOutputStream());
                response.flushBuffer();
                log.info("文件下载成功: {}", path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}