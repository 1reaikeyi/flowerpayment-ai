package common.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;

import common.properties.AliOssProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class AliOssUtil {
   @Autowired
   private AliOssProperties aliOssProperties;
   public String uploadFile(String objectName, InputStream inputStream) {
       String endpoint = aliOssProperties.getEndpoint();
       String accessKeyId = aliOssProperties.getAccessKeyId();
       String accessKeySecret = aliOssProperties.getAccessKeySecret();
       String bucketName = aliOssProperties.getBucketName();
       // 创建OSSClient实例。
       OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
       try {
           // 创建PutObject请求。
           ossClient.putObject(bucketName, objectName, inputStream);
       } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder.append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);
        return stringBuilder.toString();
       
    }

}
   
