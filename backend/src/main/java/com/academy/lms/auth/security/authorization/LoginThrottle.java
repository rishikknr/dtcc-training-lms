package com.academy.lms.auth.security.authorization;

import com.academy.lms.common.exception.ApiException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class LoginThrottle {
  private final Cache<String, Integer> attempts = Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofMinutes(15))
      .maximumSize(10_000)
      .build();

  public void check(String key) {
    Integer count = attempts.getIfPresent(key);
    if (count != null && count >= 10) {
      throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED",
          "Too many login attempts. Try again later");
    }
  }

  public void failure(String key) { attempts.asMap().merge(key, 1, Integer::sum); }
  public void success(String key) { attempts.invalidate(key); }
}
