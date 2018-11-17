package com.infinite.minio.controller;

import com.infinite.minio.common.util.ResultUtil;
import com.infinite.minio.entity.Album;
import com.infinite.minio.entity.ResultEntity;
import com.infinite.minio.service.MinioService;
import io.minio.ObjectStat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/minio")
public class MinioController {
    private String bucketName;
    @Resource(name = "minioService")
    private MinioService minioService;

    @PostMapping(value = "/putObject")
    public ResultEntity putObject(@RequestParam("file") MultipartFile file, @RequestParam("bucketName") String bucketName, @RequestParam("paths") String[] paths) {
        try {
            this.minioService.putObject(bucketName, file, paths);
        } catch (Exception e) {
            return ResultUtil.toResult(400, e.getMessage());
        }
        return ResultUtil.toResult(200);
    }

    @PostMapping(value = "/putObjects")
    public ResultEntity putObjects(@RequestParam("files") MultipartFile[] files, @RequestParam("bucketName") String bucketName, @RequestParam("paths") String[] paths) {
        try {
            for (MultipartFile file : files) {
                this.minioService.putObject(bucketName, file, paths);
            }
        } catch (Exception e) {
            return ResultUtil.toResult(400, e.getMessage());
        }
        return ResultUtil.toResult(200);
    }

    @PostMapping(value = "/getObject")
    public void getObject(@RequestParam("bucketName") String bucketName, @RequestParam("objectName") String objectName, HttpServletResponse httpServletResponse) {
        OutputStream os = null;
        InputStream stream = null;
        try {
            stream = this.minioService.getObject(bucketName, objectName);
            os = httpServletResponse.getOutputStream();
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = stream.read(buf, 0, buf.length)) >= 0) {
                os.write(buf, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @PostMapping(value = "/listAlbums")
    public ResultEntity listAlbums(@RequestParam("bucketName") String bucketName) {
        List<Album> listAlbums;
        try {
            listAlbums = this.minioService.listAlbums(bucketName);
        } catch (Exception e) {
            return ResultUtil.toResult(400, e.getMessage());
        }
        return ResultUtil.toResult(200, listAlbums);
    }

    @PostMapping(value = "/removeObject")
    public ResultEntity removeObject(@RequestParam("bucketName") String bucketName, @RequestParam("objectName") String objectName) {
        try {
            this.minioService.removeObject(bucketName, objectName);
        } catch (Exception e) {
            return ResultUtil.toResult(400, e.getMessage());
        }
        return ResultUtil.toResult(200);
    }

    @PostMapping(value = "/removeObjects")
    public ResultEntity removeObjects(@RequestParam("bucketName") String bucketName, @RequestParam("objectNames") String[] objectNames) {
        List<String>objectNameses = new ArrayList<>();
        for(String objectName: objectNames){
            objectNameses.add(objectName);
        }
        try {
            this.minioService.removeObjects(bucketName, objectNameses);
        } catch (Exception e) {
            return ResultUtil.toResult(400, e.getMessage());
        }
        return ResultUtil.toResult(200);
    }

    @PostMapping(value = "/removeIncompleteUpload")
    public ResultEntity removeIncompleteUpload(@RequestParam("bucketName") String bucketName, @RequestParam("objectName") String objectName) {
        try {
            this.minioService.removeIncompleteUpload(bucketName, objectName);
        } catch (Exception e) {
            return ResultUtil.toResult(400, e.getMessage());
        }
        return ResultUtil.toResult(200);
    }

    @PostMapping(value = "/statObject")
    public ResultEntity statObject(@RequestParam("bucketName") String bucketName, @RequestParam("objectName") String objectName) {
        ObjectStat objectStat;
        try {
            objectStat = this.minioService.statObject(bucketName, objectName);
            System.out.println(objectStat);
        } catch (Exception e) {
            return ResultUtil.toResult(400, e.getMessage());
        }
        return ResultUtil.toResult(200, "success", objectStat.toString());
    }

    @PostMapping(value = "/getBucketPolicy")
    public ResultEntity getBucketPolicy(@RequestParam("bucketName") String bucketName) {
        String policy;
        try {
            policy = this.minioService.getBucketPolicy(bucketName);
            System.out.println(policy);
        } catch (Exception e) {
            return ResultUtil.toResult(400, e.getMessage());
        }
        return ResultUtil.toResult(200, "", policy);
    }

    @PostMapping(value = "/setBucketPolicy")
    public ResultEntity setBucketPolicy(@RequestParam("bucketName") String bucketName, @RequestParam("policy") String policy) {
        try {
            this.minioService.setBucketPolicy(bucketName, policy);
        } catch (Exception e) {
            return ResultUtil.toResult(400, e.getMessage());
        }
        return ResultUtil.toResult(200);
    }
}
