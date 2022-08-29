package com.iabql.userprovider8000.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Component
public class UpLoadUtils {

    private static final Logger logger = LoggerFactory.getLogger(UpLoadUtils.class);

    // Endpoint以北京，其它Region请按实际情况填写。
    private static String endpoint = "oss-cn-beijing.aliyuncs.com";
    // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
    private static String accessKeyId = "LTAI5tLJDTEmVekc3Rq7UXzJ";
    private static String accessKeySecret = "boeDf4p3IaD4SLoEpM4oxRS6Yar9ZE";
    // Bucket名称
    private static String bucketName = "im-communication";



    /**
     *上传图片，返回图片访问路径
     * @param file 文件源
     * @param path  存储路径
     * @param fileName  存储文件名
     * @return
     * @throws IOException
     */
    public static String upLoadImg(MultipartFile file , String path, String fileName) throws IOException {

        // 填写Object完整路径，例如blog-cover/test.png。上传到exampledir目录下，exampleobject.txt保存文件名
        String objectName = path + fileName;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        //外面获取文件输入流，最后方便关闭
        InputStream in = file.getInputStream();

        String uploadUrl = null;//返回上传之后地址

        try {
            //将文件上传
            //获取文件信息，为了上传
            // meta设置请求头
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType("image/jpg");//设置为image/jpg就是在线预览，否则会下载
            //将文件上传至oss
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, objectName, in, meta);

            //返回上传之后地址
            uploadUrl = "https://"+bucketName+"."+endpoint+"/"+objectName;

        } catch (OSSException oe) {
            logger.error("图片上传失败："+oe.getMessage());//日志打印
        } catch (ClientException ce) {
            logger.error("图片上传失败："+ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();//释放oss连接
            }
            if(in != null){
                in.close();//关闭文件输入流连接
            }
        }
        return uploadUrl;
    }


    /**
     * 删除上传的图片
     * bucketName:库
     * filePath：文件路径
     */
    public static boolean deleteImg(String filePath) {
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try{
            // 删除文件或目录。如果要删除目录，目录必须为空。
            ossClient.deleteObject(bucketName,filePath);
        }catch (OSSException oe){
            logger.error("删除图片失败："+oe.getMessage());
            return false;
        }catch (ClientException ce){
            logger.error("删除图片失败："+ce.getMessage());
            return false;
        }finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }

        }
        return  true;
    }
}
