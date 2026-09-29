package com.academy.lms.user;
import jakarta.persistence.*;
@Entity @Table(name="roles")
public class Role { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Short id; @Enumerated(EnumType.STRING) @Column(nullable=false,unique=true) private RoleName name; protected Role(){} public RoleName getName(){return name;} }

