package com.infinite.minio.service;

import com.infinite.minio.entity.Album;
import io.minio.ObjectStat;
import io.minio.Result;
import io.minio.messages.DeleteError;
import io.minio.messages.Upload;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface MinioService {

    void putObject(String bucketName, MultipartFile file, String[] paths) throws Exception;

    List<Album> listAlbums(String bucketName) throws Exception;

    ObjectStat statObject(String bucketName, String objectName) throws Exception;

    void removeObject(String bucketName, String objectName) throws Exception;

    Iterable<Result<Upload>> listIncompleteUploads(String bucketName, String prefix, boolean recursive) throws Exception ;

    Iterable<Result<DeleteError>> removeObjects(String bucketName, Iterable<String> objectNames) throws Exception;

    void removeIncompleteUpload(String bucketName, String objectName) throws Exception;

    InputStream getObject(String bucketName, String objectName) throws Exception;

    void setBucketPolicy(String bucketName, String policy) throws Exception;

    String getBucketPolicy(String bucketName) throws Exception;
}
