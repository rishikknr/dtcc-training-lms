package com.academy.lms.course;
import com.academy.lms.category.Category;
import com.academy.lms.common.domain.AuditedEntity;
import com.academy.lms.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;import java.time.Instant;import java.util.*;
@Entity @Table(name="courses") public class Course extends AuditedEntity {
  public enum Level{BEGINNER,INTERMEDIATE,ADVANCED} public enum Status{DRAFT,PUBLISHED,ARCHIVED}
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="instructor_id") private User instructor;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="category_id") private Category category;
  @Column(nullable=false,length=160) private String title;@Column(nullable=false,unique=true,length=190) private String slug;
  @Column(name="short_description",nullable=false,length=300) private String shortDescription;@Column(nullable=false,columnDefinition="text") private String description;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Level level;@Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.DRAFT;
  @Column(name="thumbnail_url",length=500) private String thumbnailUrl;@Column(name="average_rating",nullable=false) private BigDecimal averageRating=BigDecimal.ZERO;
  @Column(name="rating_count",nullable=false) private int ratingCount;@Column(name="published_at") private Instant publishedAt;
  @OneToMany(mappedBy="course",cascade=CascadeType.ALL,orphanRemoval=true) @OrderBy("position") private List<CourseSection> sections=new ArrayList<>();
  protected Course(){} public Course(User i,Category c,String title,String slug,String shortD,String description,Level level,String thumbnail){instructor=i;category=c;this.title=title;this.slug=slug;shortDescription=shortD;this.description=description;this.level=level;thumbnailUrl=thumbnail;}
  public User getInstructor(){return instructor;}public Category getCategory(){return category;}public String getTitle(){return title;}public String getSlug(){return slug;}public String getShortDescription(){return shortDescription;}public String getDescription(){return description;}public Level getLevel(){return level;}public Status getStatus(){return status;}public String getThumbnailUrl(){return thumbnailUrl;}public BigDecimal getAverageRating(){return averageRating;}public int getRatingCount(){return ratingCount;}public Instant getPublishedAt(){return publishedAt;}public List<CourseSection> getSections(){return List.copyOf(sections);}
  public void update(Category c,String t,String slug,String s,String d,Level l,String thumb){category=c;title=t;this.slug=slug;shortDescription=s;description=d;level=l;thumbnailUrl=thumb;}
  public void publish(){status=Status.PUBLISHED;if(publishedAt==null)publishedAt=Instant.now();} public void archive(){status=Status.ARCHIVED;}
  public void updateRating(BigDecimal avg,int count){averageRating=avg;ratingCount=count;}
  public void addSection(CourseSection s){sections.add(s);}
}

