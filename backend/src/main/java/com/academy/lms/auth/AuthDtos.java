package com.academy.lms.auth;
import com.academy.lms.user.UserDtos;
import jakarta.validation.constraints.*;
public final class AuthDtos { private AuthDtos(){}
  public record Register(@NotBlank @Email @Size(max=254) String email,@NotBlank @Size(min=12,max=72) String password,@NotBlank @Size(max=100) String displayName){}
  public record Login(@NotBlank @Email String email,@NotBlank String password){}
  public record AuthResponse(UserDtos.Profile user){}
}

