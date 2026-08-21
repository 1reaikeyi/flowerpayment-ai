package start.controller.file;

import common.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 直接访问测试图片
 * https://java-web-ai-wangnian.oss-cn-beijing.aliyuncs.com/fcb59f82-5aac-4078-8667-0ef559af7af9.gif
 */
@RestController
@RequestMapping("/oss")
@Slf4j
public class OSSFileController {
    //存储 公共数据
    @Autowired
    private AliOssUtil aliOss;

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {

        Map<String, String> fileResult = new HashMap<>();
        if (file.isEmpty()) {
            fileResult.put("error", "请选择要上传的文件");
            return ResponseEntity.badRequest().body(fileResult);
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID().toString() + extension;

            String url =aliOss.uploadFile(objectName, file.getInputStream());
            fileResult.put("url", url);
            fileResult.put("filename", originalFilename);

            log.info("url:{}", url);
            log.info("filename:{}", originalFilename);

            return ResponseEntity.ok(fileResult);
        } catch (IOException e) {
            fileResult.put("error", "文件上传失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(fileResult);
        }
    }

    @GetMapping
    public ResponseEntity<byte[]> downloadFile(@RequestParam("url") String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            InputStream inputStream = connection.getInputStream();

            byte[] bytes = inputStream.readAllBytes();
            inputStream.close();

            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            String decodedFilename = java.net.URLDecoder.decode(filename, "UTF-8");

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
                    new String(decodedFilename.getBytes("UTF-8"), "ISO-8859-1") + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, connection.getContentType());

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
