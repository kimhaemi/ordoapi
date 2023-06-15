package com.example.demo.repository;

import com.example.demo.domain.entity.PostEntity;
import com.example.demo.domain.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Integer> {
//    Optional<PostEntity> findByUserid(Integer userid);
    Page<PostEntity> findAllByUser(UserEntity userEntity, Pageable pageable);

}
