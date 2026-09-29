package com.academy.lms.common.exception;
import org.springframework.http.HttpStatus;
public class ApiException extends RuntimeException {
  private final HttpStatus status; private final String code;
  public ApiException(HttpStatus status,String code,String message){super(message);this.status=status;this.code=code;}
  public HttpStatus status(){return status;} public String code(){return code;}
  public static ApiException notFound(String resource){return new ApiException(HttpStatus.NOT_FOUND,"NOT_FOUND",resource+" not found");}
  public static ApiException forbidden(){return new ApiException(HttpStatus.FORBIDDEN,"FORBIDDEN","You are not authorized to perform this action");}
  public static ApiException conflict(String message){return new ApiException(HttpStatus.CONFLICT,"CONFLICT",message);}
}

