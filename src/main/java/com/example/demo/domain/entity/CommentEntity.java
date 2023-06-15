package com.example.demo.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;


@Builder
@Setter
@Getter
@Entity
@Table(name = "comment", indexes = {
        @Index(name = "post_id_idx", columnList = "post_id")
})
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private PostEntity post;

    @Column(name = "comment")
    private String comment;


    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static CommentEntity of(UserEntity user, PostEntity post, String comment) {
        CommentEntity entity = new CommentEntity();
        entity.setUser(user);
        entity.setPost(post);
        entity.setComment(comment);
        return entity;
    }
}
