package com.academy.lms.category;
import jakarta.persistence.*;import java.time.Instant;import java.util.UUID;
@Entity @Table(name="categories") public class Category { @Id private UUID id;@Column(nullable=false,unique=true) private String name;@Column(nullable=false,unique=true) private String slug;private String description;@Column(name="created_at") private Instant createdAt;
 protected Category(){} public Category(String n,String s,String d){id=UUID.randomUUID();name=n;slug=s;description=d;createdAt=Instant.now();} public UUID getId(){return id;}public String getName(){return name;}public String getSlug(){return slug;}public String getDescription(){return description;}}

