package com.academy.lms.security;
import com.academy.lms.common.exception.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
public final class CurrentUser { private CurrentUser(){} public static UUID id(Authentication a){if(a==null||!(a.getPrincipal() instanceof AuthenticatedUser u))throw new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Authentication required");return u.id();} }

