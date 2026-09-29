package com.academy.lms.course;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.math.BigDecimal;import java.time.Instant;import java.util.*;
public final class CourseDtos {private CourseDtos(){}
 public record Upsert(@NotBlank @Size(max=160) String title,@NotBlank @Size(max=300) String shortDescription,@NotBlank @Size(max=20000) String description,@NotNull Course.Level level,UUID categoryId,@Size(max=500)@Pattern(regexp="^https?://.*",message="must be an http(s) URL")String thumbnailUrl){}
 public record Summary(UUID id,String title,String slug,String shortDescription,Course.Level level,String thumbnailUrl,BigDecimal averageRating,int ratingCount,UUID categoryId,String categoryName,UUID instructorId,String instructorName){}
 public record Detail(UUID id,String title,String slug,String shortDescription,String description,Course.Level level,Course.Status status,String thumbnailUrl,BigDecimal averageRating,int ratingCount,CategoryRef category,InstructorRef instructor,List<Section> sections,Instant createdAt,Instant updatedAt){}
 public record CategoryRef(UUID id,String name,String slug){} public record InstructorRef(UUID id,String displayName){}
 public record Section(UUID id,String title,int position,List<LessonItem> lessons){} public record LessonItem(UUID id,String title,String content,String videoUrl,int position,int durationMinutes,boolean preview){}
 public record CreateSection(@NotBlank@Size(max=160)String title,@Min(0)int position){}
 public record CreateLesson(@NotBlank@Size(max=160)String title,@NotBlank@Size(max=50000)String content,@Size(max=500)String videoUrl,@Min(0)int position,@Min(0)@Max(1440)int durationMinutes,boolean preview){}
}

