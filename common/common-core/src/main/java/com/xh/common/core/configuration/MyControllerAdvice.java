package com.xh.common.core.configuration;

import com.xh.common.core.web.MyException;
import com.xh.common.core.web.RestResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 控制层异常统一处理
 * sunxh 2023/2/26
 */
@RestControllerAdvice
public class MyControllerAdvice {

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(MyException.class)
    public RestResponse<?> handleControllerException(MyException ex) {
        RestResponse<String> response = RestResponse.error(ex.getMessage());
        response.setHttpCode(HttpStatus.OK.value());
        return response;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public RestResponse<?> handleControllerException(HttpServletRequest request, Throwable ex) {
        ex.printStackTrace();
        RestResponse<?> response = RestResponse.error("服务器运行异常！");
        HttpStatus status = getStatus(request);
        if (status != null) {
            response.setHttpCode(status.value());
        }
        return response;
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer code = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (code != null) return HttpStatus.resolve(code);
        return null;
    }
}
