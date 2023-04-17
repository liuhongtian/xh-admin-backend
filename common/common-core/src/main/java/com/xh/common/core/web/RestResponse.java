package com.xh.common.core.web;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Data
public class RestResponse<T> implements Serializable {

    private Integer httpCode; //http状态码
    private String status; //响应消息状态：success, error, warning, info
    private String message;//响应的消息内容
    private T data;//响应的数据

    /**
     * 响应成功信息
     */
    public static <T> RestResponse<T> success() {
        RestResponse<T> restResponse = new RestResponse<>();
        restResponse.httpCode = HttpStatus.OK.value();
        restResponse.status = "success";
        return restResponse;
    }

    /**
     * 响应成功信息，携带data
     */
    public static <T> RestResponse<T> success(T data) {
        RestResponse<T> restResponse = new RestResponse<>();
        restResponse.httpCode = HttpStatus.OK.value();
        restResponse.status = "success";
        restResponse.data = data;
        return restResponse;
    }

    /**
     * 错误响应，携带data
     */
    public static <T> RestResponse<T> errorData(T data) {
        RestResponse<T> restResponse = new RestResponse<>();
        restResponse.httpCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        restResponse.status = "error";
        restResponse.data = data;
        return restResponse;
    }

    /**
     * 错误响应，携带错误message
     */
    public static RestResponse<String> error(String message) {
        RestResponse<String> restResponse = new RestResponse<>();
        restResponse.httpCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        restResponse.status = "error";
        restResponse.message = message;
        return restResponse;
    }
}
