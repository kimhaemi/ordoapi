package com.example.demo.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;


@Builder
@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "likes")
public class LikeEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private PostEntity post;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static LikeEntity of(UserEntity user, PostEntity post) {
        LikeEntity entity = new LikeEntity();
        entity.setUser(user);
        entity.setPost(post);
        return entity;
    }
}
