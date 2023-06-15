package com.example.demo.service;


//import com.example.demo.configuration.filter.JwtTokenfilter;
import com.example.demo.domain.Alarm;
import com.example.demo.domain.User;
import com.example.demo.domain.entity.AlarmEntity;
import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.response.UserJoinResponse;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.AlarmEntityRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.JwtTokenUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class UserService {

    //private final JwtTokenUtils jwtTokenUtil;
    private final UserRepository userRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private final BCryptPasswordEncoder encoder;
    @Value("${jwt.token.secret}")
    private String secretKey;
//    private Long expireTimeMs = 1000 * 60 *60l;
    private Long expireTimeMs = 1000 * 30L;


    public User  loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUserName(userName)
                .orElseThrow(()->new AppException(ErrorCode.USERNAME_NOT_FOUND,userName+ "이 없습니다." ));

        User user = User.fromEntity(userEntity);

        return user;
    }

    public UserEntity join(String userName, String password){
        userRepository.findByUserName(userName).ifPresent(
                user -> {
                    throw new AppException(ErrorCode.DUPLICATED_USER_NAME, " "+ userName +"는 이미 있습니다.");
                }
        );
        UserEntity user = UserEntity.of(userName, encoder.encode(password));

        UserEntity savedUser = userRepository.save(user);

        return savedUser;
//        return "회원가입 성공";
    }

    public String login(String userName, String password){
//        System.out.println("userName:::::" + userName);
//        System.out.println("password:::::" + password);
        //userName 없음
        User savedUser = loadUserByUsername(userName);

        //password 틀림
        if(!encoder.matches(password,savedUser.getPassword())){
            throw new AppException(ErrorCode.INVALID_PASSWORD,"패스워드가 잘못 되었습니다.");
        }

        String token = JwtTokenUtils.generateAccessToken(savedUser.getUsername(), secretKey, expireTimeMs);
//        System.out.println("token:::::" + token);

        return token;
    }

    public Page<AlarmEntity> alarmList(Integer userId, Pageable pageable) {
        return alarmEntityRepository.findAllByUserId(userId, pageable);
    }
}

