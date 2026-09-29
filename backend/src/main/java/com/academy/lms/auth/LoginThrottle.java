package com.academy.lms.auth;
import com.github.benmanes.caffeine.cache.*;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import com.academy.lms.common.exception.ApiException;
@Component public class LoginThrottle {
  private final Cache<String,Integer> attempts=Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(15)).maximumSize(10000).build();
  public void check(String key){Integer n=attempts.getIfPresent(key);if(n!=null&&n>=10)throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,"RATE_LIMITED","Too many login attempts. Try again later");}
  public void fail(String key){attempts.asMap().merge(key,1,Integer::sum);} public void success(String key){attempts.invalidate(key);}
}

