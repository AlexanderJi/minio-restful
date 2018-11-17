package com.infinite.minio.common.util;

import com.infinite.minio.entity.ResultEntity;

public class ResultUtil {
    private ResultUtil() {
    }


    public static <T> ResultEntity<T> toResult(int code) {
        return toResult(code, code == 200 ? "success" : "fail", null);
    }

    public static ResultEntity toResult(int code, String msg) {
        return toResult(code, msg, null);
    }

    public static <T> ResultEntity<T> toResult(int code, T content) {
        return toResult(code, code == 200 ? "success" : "fail", content);
    }

    public static <T> ResultEntity<T> toResult(T content) {
        return toResult(200, content);
    }

    public static <T> ResultEntity<T> toResult(int code, String msg, T content) {
        ResultEntity<T> resultEntity = new ResultEntity();
        resultEntity.code = code;
        resultEntity.msg = msg;
        resultEntity.content = content;
        return resultEntity;
    }
}
