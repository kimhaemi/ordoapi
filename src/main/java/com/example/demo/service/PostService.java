package com.example.demo.service;

import com.example.demo.domain.AlarmArgs;
import com.example.demo.domain.AlarmType;
import com.example.demo.domain.dto.Comment;
import com.example.demo.domain.dto.PostDto;
import com.example.demo.domain.entity.AlarmEntity;
import com.example.demo.domain.entity.CommentEntity;
import com.example.demo.domain.entity.LikeEntity;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.AlarmEntityRepository;
import com.example.demo.repository.CommentEntityRepository;
import com.example.demo.repository.LikeEntityRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.demo.domain.entity.PostEntity;
import com.example.demo.domain.entity.UserEntity;

import javax.transaction.Transactional;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeEntityRepository likeEntityRepository;
    private final CommentEntityRepository commentEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;

    public PostDto get(Integer id) {
        PostEntity postEntity = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, String.format("%d의 포스트가 없습니다.", id)));
        return PostDto.fromEntity(postEntity);
    }

    public Page<PostDto> getAllItems(Pageable pageable) {
            Page<PostEntity> postEntities = postRepository.findAll(pageable);
            Page<PostDto> postDtos = PostDto.toDtoList(postEntities);
            return postDtos;
    }

    @Transactional
    public PostDto write(String title, String body, String username) {
        UserEntity userEntity = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND, String.format("%s not founded", username)));
        System.out.println("Service Test Post1");
        PostEntity savedPostEntity = postRepository.save(PostEntity.of(title, body, userEntity));

        PostDto postDto = PostDto.builder()
                .id(savedPostEntity.getId())
                .build();
        System.out.println("Service Test Post2");
        
        return postDto;
    }

    @Transactional
    public PostEntity modify(String userName, Integer postId, String title, String body) {
        System.out.println("Modify Service Tes1");
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, String.format("postId is %d", postId)));
        System.out.println("Modify PostEntity");


        System.out.println(userName);
        UserEntity userEntity = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND, String.format("%s not founded", userName)));

        Integer userId = userEntity.getId();

        if (!Objects.equals(postEntity.getUser().getId(), userId)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION, String.format("user %s has no permission with post %d", userId, postId));
        }
        System.out.println("test4");

        postEntity.setTitle(title);
        postEntity.setBody(body);
        PostEntity savedEntity = postRepository.saveAndFlush(postEntity);

        return savedEntity;
    }

    @Transactional
    public boolean delete(String userName, Integer postId) {
        System.out.println("Delete Service Tes1");
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, String.format("postId is %d", postId)));
        System.out.println("Delete PostEntity");

        UserEntity userEntity = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND, String.format("%s not founded", userName)));


        if (!Objects.equals(postEntity.getUser().getUserName(), userName)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION, String.format("user %s has no permission with post %d", userName, postId)); }

        likeEntityRepository.deleteAllByPost(postEntity);
        commentEntityRepository.deleteAllByPost(postEntity);
        postRepository.delete(postEntity);

        return true;
    }

    @Transactional
    public void like(Integer postId, String userName) {

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, String.format("postId is %d", postId)));

        UserEntity userEntity = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND, String.format("%s not founded", userName)));

        // 이미 눌렀는지
        likeEntityRepository.findByUserAndPost(userEntity, postEntity)
                .ifPresent(item -> {
                    throw new AppException(ErrorCode.ALREADY_LIKED, String.format("userName %s는 이미 %d 포스트에 좋아요를 눌렀습니다.",userName, postId));
                });

        likeEntityRepository.save(LikeEntity.of(userEntity, postEntity));
        alarmEntityRepository.save(AlarmEntity.of(postEntity.getUser(), AlarmType.NEW_LIKE_ON_POST,
                userEntity.getId(), postEntity.getId()));

    }

    public Long likeCount(Integer postId) {
        // Post가 있는지 확인
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, String.format("postId is %d", postId)));

        // like를 불러옴
        return likeEntityRepository.countByPost(postEntity);
    }

    public Page<PostDto> my(String userName, Pageable pageable) {
        UserEntity userEntity = userRepository.findByUserName(userName).orElseThrow(() ->
                new AppException(ErrorCode.USERNAME_NOT_FOUND,String.format("%s not founded",userName)));

        return postRepository.findAllByUser(userEntity,pageable).map(PostDto::fromEntity);
    }

    @Transactional
    public CommentEntity comment (Integer postId, String userName, String comment) {
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(() ->
                new AppException(ErrorCode.POST_NOT_FOUND,String.format("%s not founded", postId)));

        UserEntity userEntity = userRepository.findByUserName(userName).orElseThrow(() ->
                new AppException(ErrorCode.USERNAME_NOT_FOUND, String.format("%s not founded",userName)));

        CommentEntity commentEntity = commentEntityRepository.save(CommentEntity.of(userEntity,postEntity,comment));
        alarmEntityRepository.save(AlarmEntity.of(postEntity.getUser(), AlarmType.NEW_COMMENT_ON_POST,
                userEntity.getId(),
                postEntity.getId()));

        return commentEntity;
    }

    public Page<CommentEntity> getComments (Integer postId, Pageable pageable) {
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(() ->
                new AppException(ErrorCode.POST_NOT_FOUND,String.format("%s not founded", postId)));

        return commentEntityRepository.findAllByPost(postEntity,pageable);

    }

    @Transactional
    public CommentEntity modifyComment(String userName, Integer id, String comment) {
        CommentEntity entity = commentEntityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND, String.format("Comment id is %d", id)));

        UserEntity userEntity = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND, String.format("%s not founded", userName)));

        Integer userId = userEntity.getId();

        if (!Objects.equals(entity.getUser().getId(), userId)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION, String.format("user %s has no permission with comment %d", userId, id));
        }

        entity.setComment(comment);
        CommentEntity savedEntity = commentEntityRepository.saveAndFlush(entity);

        return savedEntity;
    }

    @Transactional
    public Boolean deleteComment(String userName, Integer id) {
        CommentEntity entity = commentEntityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND, String.format("comment id is %d", id)));

        UserEntity userEntity = userRepository.findByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_FOUND, String.format("%s not founded", userName)));

        Integer userId = userEntity.getId();

        if (!Objects.equals(entity.getUser().getId(), userId)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION, String.format("user %s has no permission with comment %d", userId, id));
        }

        commentEntityRepository.delete(entity);

        return true;
    }
}
