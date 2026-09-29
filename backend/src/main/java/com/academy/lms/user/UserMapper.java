package com.academy.lms.user;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
@Component public class UserMapper { public UserDtos.Profile toProfile(User u){return new UserDtos.Profile(u.getId(),u.getEmail(),u.getDisplayName(),u.getBio(),u.getAvatarUrl(),u.getRoles().stream().map(Role::getName).collect(Collectors.toUnmodifiableSet()));} }
