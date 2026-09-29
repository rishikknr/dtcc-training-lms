package com.academy.lms.user;
import java.util.*;
import org.springframework.data.jpa.repository.*;
public interface UserRepository extends JpaRepository<User,UUID> { Optional<User> findByEmailIgnoreCase(String email); boolean existsByEmailIgnoreCase(String email); }

