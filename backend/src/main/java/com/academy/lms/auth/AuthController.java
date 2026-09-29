package com.academy.lms.auth;
import com.academy.lms.security.CurrentUser;
import com.academy.lms.user.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth") public class AuthController {
  private final AuthService auth; private final UserRepository users; private final UserMapper mapper;
  public AuthController(AuthService a,UserRepository u,UserMapper m){auth=a;users=u;mapper=m;}
  @GetMapping("/csrf") Map<String,String> csrf(CsrfToken token){return Map.of("token",token.getToken());}
  @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) AuthDtos.AuthResponse register(@Valid @RequestBody AuthDtos.Register r,HttpServletRequest h){return auth.register(r,h);}
  @PostMapping("/login") AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.Login r,HttpServletRequest h){return auth.login(r,h);}
  @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT) void logout(HttpServletRequest req){var s=req.getSession(false);if(s!=null)s.invalidate();}
  @GetMapping("/me") UserDtos.Profile me(Authentication a){return mapper.toProfile(users.findById(CurrentUser.id(a)).orElseThrow());}
}

