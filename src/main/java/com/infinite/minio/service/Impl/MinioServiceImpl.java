package com.infinite.minio.service.Impl;

import com.infinite.minio.common.util.SysParamUtil;
import com.infinite.minio.entity.Album;
import com.infinite.minio.service.MinioService;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.Result;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.MinioException;
import io.minio.messages.DeleteError;
import io.minio.messages.Item;
import io.minio.messages.Upload;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service("minioService")
public class MinioServiceImpl implements MinioService {
    private static MinioClient minioClient;

    static {
        // 使用Minio服务的URL，端口，Access key和Secret key创建一个MinioClient对象
        try {
            minioClient = new MinioClient(SysParamUtil.getProperties("endpoint"), SysParamUtil.getProperties("accessKey"), SysParamUtil.getProperties("secretKey"));
        } catch (InvalidEndpointException e) {
            e.printStackTrace();
        } catch (InvalidPortException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void putObject(String bucketName, MultipartFile file, String[] paths) throws XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException, IOException {
        try {
            // 检查存储桶是否已经存在
            boolean isExist = minioClient.bucketExists(bucketName);
            if (isExist) {
                System.out.println("Bucket already exists.");
            } else {
                // 创建一个名为asiatrip的存储桶，用于存储照片的zip文件。
                minioClient.makeBucket(bucketName);
            }

            // 使用putObject上传一个文件到存储桶中。
            //minioClient.putObject(bucketName, file.getOriginalFilename(), file.getInputStream(), file.getSize(), file.getContentType());
            StringBuilder flePath = new StringBuilder();
            if (paths != null) {
                for (String path : paths) {
                    flePath.append(path + "/");
                }
            }
            String objectName = flePath + file.getOriginalFilename();
            minioClient.putObject(bucketName, objectName, file.getInputStream(), file.getSize(), file.getContentType());
            System.out.println(file.getOriginalFilename() + " is successfully uploaded as " + file.getOriginalFilename() + " to " + bucketName + " bucket.");
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
        }
    }

    @Override
    public List<Album> listAlbums(String bucketName) throws Exception {
        List<Album> list = new ArrayList<Album>();
        Iterable<Result<Item>> myObjects = minioClient.listObjects(bucketName);

        // Iterate over each elements and set album url.
        for (Result<Item> result : myObjects) {
            Item item = result.get();
            System.out.println(item.lastModified() + ", " + item.size() + ", " + item.objectName());

            // Create a new Album Object
            Album album = new Album();

            // Set the presigned URL in the album object
            album.setUrl(minioClient.presignedGetObject(bucketName, item.objectName(), 60 * 60 * 24));

            // Add the album object to the list holding Album objects
            list.add(album);
        }
        // Return list of albums.
        return list;
    }

    @Override
    public ObjectStat statObject(String bucketName, String objectName) throws Exception {
        return minioClient.statObject(bucketName, objectName);
    }

    @Override
    public void removeObject(String bucketName, String objectName) throws Exception {
        minioClient.removeObject(bucketName, objectName);
        System.out.println("successfully removed mybucket/myobject");
    }

    @Override
    public Iterable<Result<Upload>> listIncompleteUploads(String bucketName, String prefix, boolean recursive) throws Exception {
        // Check whether 'mybucket' exist or not.
        boolean found = minioClient.bucketExists(bucketName);
        Iterable<Result<Upload>> myObjects = null;
        if (found) {
            // List all incomplete multipart upload of objects in 'my-bucketname
            myObjects = minioClient.listIncompleteUploads(bucketName);
            for (Result<Upload> result : myObjects) {
                Upload upload = result.get();
                System.out.println(upload.uploadId() + ", " + upload.objectName());
            }
        } else {
            System.out.println("mybucket does not exist");
        }
        return myObjects;
    }

    @Override
    public Iterable<Result<DeleteError>> removeObjects(String bucketName, Iterable<String> objectNames) throws Exception {
        List<Result<DeleteError>> list = new ArrayList<>();
        for (Result<DeleteError> errorResult : minioClient.removeObject(bucketName, objectNames)) {
            DeleteError error = errorResult.get();
            list.add(errorResult);
            System.out.println("Failed to remove '" + error.objectName() + "'. Error:" + error.message());
        }
        return list;
    }

    @Override
    public void removeIncompleteUpload(String bucketName, String objectName) throws Exception {
        // 从存储桶中删除名为myobject的未完整上传的对象。
        minioClient.removeIncompleteUpload(bucketName, objectName);
        System.out.println("successfully removed all incomplete upload session of my-bucketname/my-objectname");

    }

    @Override
    public InputStream getObject(String bucketName, String objectName) throws Exception {
        // 调用statObject()来判断对象是否存在。
        // 如果不存在, statObject()抛出异常,
        // 否则则代表对象存在。
        minioClient.statObject(bucketName, objectName);
        // 获取"myobject"的输入流。
        InputStream stream = minioClient.getObject(bucketName, objectName);
        return stream;
    }

    @Override
    public void setBucketPolicy(String bucketName, String policy) throws Exception {


        StringBuilder builder = new StringBuilder();
        switch (policy) {
            /**
             * 相当于mc客户端mc policy none minio/test命令
             */
            case "none":
                builder.append("{\n");
                builder.append("    \"Statement\": [],\n");
                builder.append("    \"Version\": \"2012-10-17\"\n");
                builder.append("}\n");
                break;
            /**
             * 开启永久访问权限即静态链接可以访问到
             * 相当于mc客户端mc policy download minio/test命令
             */
            case "download":
                builder.append("{\n");
                builder.append("    \"Statement\": [\n");
                builder.append("        {\n");
                builder.append("            \"Action\": [\n");
                builder.append("                \"s3:GetBucketLocation\",\n");
                builder.append("                \"s3:ListBucket\"\n");
                builder.append("            ],\n");
                builder.append("            \"Effect\": \"Allow\",\n");
                builder.append("            \"Principal\": \"*\",\n");
                builder.append("            \"Resource\": \"arn:aws:s3:::" + bucketName + "\"\n");
                builder.append("        },\n");
                builder.append("        {\n");
                builder.append("            \"Action\": \"s3:GetObject\",\n");
                builder.append("            \"Effect\": \"Allow\",\n");
                builder.append("            \"Principal\": \"*\",\n");
                builder.append("            \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n");
                builder.append("        }\n");
                builder.append("    ],\n");
                builder.append("    \"Version\": \"2012-10-17\"\n");
                builder.append("}\n");
                break;
            case "public":
                builder.append("{\n");
                builder.append("    \"Statement\": [\n");
                builder.append("        {\n");
                builder.append("            \"Action\": [\n");
                builder.append("                \"s3:GetBucketLocation\",\n");
                builder.append("                \"s3:ListBucket\",\n");
                builder.append("                \"s3:ListBucketMultipartUploads\"\n");
                builder.append("            ],\n");
                builder.append("            \"Effect\": \"Allow\",\n");
                builder.append("            \"Principal\": \"*\",\n");
                builder.append("            \"Resource\": \"arn:aws:s3:::" + bucketName + "\"\n");
                builder.append("        },\n");
                builder.append("        {\n");
                builder.append("            \"Action\": [\n");
                builder.append("                \"s3:ListMultipartUploadParts\",\n");
                builder.append("                \"s3:PutObject\",\n");
                builder.append("                \"s3:AbortMultipartUpload\",\n");
                builder.append("                \"s3:DeleteObject\",\n");
                builder.append("                \"s3:GetObject\"\n");
                builder.append("            ],\n");
                builder.append("            \"Effect\": \"Allow\",\n");
                builder.append("            \"Principal\": \"*\",\n");
                builder.append("            \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n");
                builder.append("        }\n");
                builder.append("    ],\n");
                builder.append("    \"Version\": \"2012-10-17\"\n");
                builder.append("}\n");
                break;
            case "upload":
                builder.append("{\n");
                builder.append("    \"Statement\": [\n");
                builder.append("        {\n");
                builder.append("            \"Action\": [\n");
                builder.append("                \"s3:GetBucketLocation\",\n");
                builder.append("                \"s3:ListBucketMultipartUploads\"\n");
                builder.append("            ],\n");
                builder.append("            \"Effect\": \"Allow\",\n");
                builder.append("            \"Principal\": \"*\",\n");
                builder.append("            \"Resource\": \"arn:aws:s3:::" + bucketName + "\"\n");
                builder.append("        },\n");
                builder.append("        {\n");
                builder.append("            \"Action\": [\n");
                builder.append("                \"s3:ListMultipartUploadParts\",\n");
                builder.append("                \"s3:PutObject\",\n");
                builder.append("                \"s3:AbortMultipartUpload\",\n");
                builder.append("                \"s3:DeleteObject\"\n");
                builder.append("            ],\n");
                builder.append("            \"Effect\": \"Allow\",\n");
                builder.append("            \"Principal\": \"*\",\n");
                builder.append("            \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n");
                builder.append("        }\n");
                builder.append("    ],\n");
                builder.append("    \"Version\": \"2012-10-17\"\n");
                builder.append("}\n");
                break;
            default:
                break;
        }
        minioClient.setBucketPolicy(bucketName, builder.toString());

    }

    @Override
    public String getBucketPolicy(String bucketName) throws Exception {
        return minioClient.getBucketPolicy(bucketName);
    }
}
