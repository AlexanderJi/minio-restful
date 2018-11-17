package com.infinite.minio.entity;

import java.io.Serializable;

public class ResultEntity<T> implements Serializable {
    public int code;
    public String msg;
    public T content;
}
