export type Role='STUDENT'|'INSTRUCTOR'|'ADMIN';
export type User={id:string;email:string;displayName:string;bio?:string;avatarUrl?:string;roles:Role[]};
export type Category={id:string;name:string;slug:string;description?:string};
export type CourseSummary={id:string;title:string;slug:string;shortDescription:string;level:'BEGINNER'|'INTERMEDIATE'|'ADVANCED';thumbnailUrl?:string;averageRating:number;ratingCount:number;categoryId?:string;categoryName?:string;instructorId:string;instructorName:string};
export type Lesson={id:string;title:string;content?:string;videoUrl?:string;position:number;durationMinutes:number;preview:boolean};
export type Course=CourseSummary&{description:string;status:'DRAFT'|'PUBLISHED'|'ARCHIVED';category?:Category;instructor:{id:string;displayName:string};sections:{id:string;title:string;position:number;lessons:Lesson[]}[];createdAt:string;updatedAt:string};
export type Page<T>={content:T[];totalElements:number;totalPages:number;number:number;size:number;first:boolean;last:boolean};
export type Review={id:string;courseId:string;studentId:string;studentName:string;rating:number;comment:string;status:'PUBLISHED'|'HIDDEN';moderationReason?:string;createdAt:string;updatedAt:string};
export type Enrollment={id:string;course:CourseSummary;progress:number;enrolledAt:string;completedAt?:string};

