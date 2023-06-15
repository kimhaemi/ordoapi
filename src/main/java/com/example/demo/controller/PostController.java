package com.example.demo.controller;

import com.example.demo.domain.dto.*;
import com.example.demo.domain.entity.CommentEntity;
import com.example.demo.domain.entity.PostEntity;
import com.example.demo.domain.response.CommentResponse;
import com.example.demo.domain.response.CommentSimpleResponse;
import com.example.demo.domain.response.PostResponse;
import com.example.demo.domain.response.Response;
import com.example.demo.service.PostService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
@Api(tags = {"Post Controller"})
public class PostController {

    private final PostService postService;

    @PostMapping
    public Response<PostResponse> posts(@RequestBody PostRequest dto, Authentication authentication){
        System.out.println("Controller Test Enter");
        PostDto postDto = postService.write(dto.getTitle(),dto.getBody(), authentication.getName());
        System.out.println("Controller Test");
        return Response.success(new PostResponse("포스트 등록 완료", postDto.getId()));
    }

    /* Post 1개 조회
     */
    @GetMapping("/{postId}")
    public ResponseEntity<Response<PostDto>> findById(@PathVariable Integer postId) {
        PostDto postDto = postService.get(postId);
        return ResponseEntity.ok().body(Response.success(postDto));
    }

    /* Post List 조회
     */

    @GetMapping
    public ResponseEntity<Response<Page<PostDto>>> getPostList(@PageableDefault(size = 20)
                                         @SortDefault (sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostDto> postDtos = postService.getAllItems(pageable);
        return ResponseEntity.ok().body(Response.success(postDtos));
    }

    @PutMapping("/{id}")    // postid → string으로만 오는 거 같은데 숫자형태로 올 수 없는지
    public ResponseEntity<Response<PostResponse>> modify(@PathVariable Integer id, @RequestBody ModifyRequest dto, Authentication authentication) {
        System.out.println("Modify Controller Tes1");

        PostEntity postEntity = postService.modify(authentication.getName(), id, dto.getTitle(), dto.getBody());
        System.out.println("Modify Controller Tes3");
        return ResponseEntity.ok(Response.success(new PostResponse("포스트 수정 완료", postEntity.getId())));
    }

    @DeleteMapping("/{postId}")
    public Response<PostResponse> delete(@PathVariable Integer postId, Authentication authentication) {
        System.out.println("Delete Controller Tes1");

        postService.delete(authentication.getName(), postId);
        return Response.success(new PostResponse("포스트 삭제 완료", postId));
    }


    // do Like
    @PostMapping("/{id}/likes")
    public Response<String> like(@PathVariable Integer id, Authentication authentication) {
        postService.like(id, authentication.getName());
        return Response.success("좋아요를 눌렀습니다.");
    }

    // get Likes
    @GetMapping("/{id}/likes")
    public Response<Long> likeCount(@PathVariable Integer id) {
        Long likeCnt = postService.likeCount(id);
        return Response.success(likeCnt);
    }

    @GetMapping("/my")
    public Response<Page<PostDto>> my(Pageable pageable, Authentication authentication) {
        Page<PostDto> my = postService.my(authentication.getName(),pageable);
        return Response.success(my);
    }

    @PostMapping("/{id}/comments")
    public Response<CommentResponse> comment(@PathVariable Integer id, @RequestBody PostCommentRequest request, Authentication authentication) {
        CommentEntity commentEntity = postService.comment(id, authentication.getName(), request.getComment());
        CommentResponse commentResponse = CommentResponse.fromComment(commentEntity);
        return Response.success(commentResponse);
    }

    @GetMapping("/{id}/comments")
    public Response<Page<CommentResponse>> comment(@PathVariable Integer id, @PageableDefault(size = 10)
    @SortDefault (sort = "createdAt",direction = Sort.Direction.DESC) Pageable pageable, Authentication authentication) {
        Page<CommentResponse> commentResponses = postService.getComments(id, pageable)
                .map(commentEntity -> CommentResponse.fromComment(commentEntity) );

        return Response.success(commentResponses);
    }

    @PutMapping("/{postId}/comments/{id}")
    public Response<CommentResponse> modifyComment(@PathVariable Integer id, @RequestBody CommentModifyRequest dto, Authentication authentication) {
        CommentEntity commentEntity = postService.modifyComment(authentication.getName(), id, dto.getComment());
        return Response.success(CommentResponse.fromComment(commentEntity));
    }

    @DeleteMapping("/{postId}/comments/{id}")
    public Response<CommentSimpleResponse> deleteComment(@PathVariable Integer id, Authentication authentication) {

        postService.deleteComment(authentication.getName(), id);

        return Response.success(new CommentSimpleResponse("댓글 삭제 완료", id));
    }
}



