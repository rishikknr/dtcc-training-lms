package com.academy.lms.user;
import jakarta.validation.constraints.*;
import java.util.*;
public final class UserDtos {
  private UserDtos(){}
  public record Profile(UUID id,String email,String displayName,String bio,String avatarUrl,Set<RoleName> roles){}
  public record UpdateProfile(@NotBlank @Size(max=100) String displayName,@Size(max=500) String bio,@Size(max=500) @Pattern(regexp="^https?://.*",message="must be an http(s) URL") String avatarUrl){}
  public record ChangePassword(@NotBlank String currentPassword,@NotBlank @Size(min=12,max=72) String newPassword){}
}

