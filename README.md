<div align=center>
 <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/7/74/%EB%A9%8B%EC%9F%81%EC%9D%B4%EC%82%AC%EC%9E%90%EC%B2%98%EB%9F%BC_%EB%A1%9C%EA%B3%A0.png/692px-%EB%A9%8B%EC%9F%81%EC%9D%B4%EC%82%AC%EC%9E%90%EC%B2%98%EB%9F%BC_%EB%A1%9C%EA%B3%A0.png" width="150"/>
 <h2>멋사스네스(MutsaSNS)</h2>
 </div>

**Mutsa:SNS(멋사스네스)** 는 글쓰기, 로그인, 피드, 댓글, 좋아요, 알림 기능이 있는 SNS API입니다.

<div align="center">
 <img src="https://img.shields.io/badge/SpringBoot-6DB33F.svg?logo=Spring-Boot&logoColor=white" />
 <img src="https://img.shields.io/badge/SpringSecurity-6DB33F.svg?logo=Spring-Security&logoColor=white" />
 <img src="https://img.shields.io/badge/MySQL-3776AB.svg?logo=MySql&logoColor=white" />
 <img src="https://img.shields.io/badge/Docker-2496ED.svg?logo=Docker&logoColor=white" />
 <img src="https://img.shields.io/badge/AmazonEC2-FF9900.svg?logo=Amazon-EC2&logoColor=white" />
</div>


## ⚒ 실행 방법
Environment Variable에 아래 값들을 설정하고 실행 합니다.

|환경변수명|예제|
|---|---|
|SPRING_DATASOURCE_URL|jdbc:mysql://ec2-1-23-456-789.ap-northeast-2.compute.amazonaws.com:3306/hospital-review2|
|SPRING_DATASOURCE_USERNAME|root|
|SPRING_DATASOURCE_PASSWORD|password|

## 📃 Swagger

> Local
http://localhost:80890/swagger-ui/

> Remote
http://ec2-3-38-173-194.ap-northeast-2.compute.amazonaws.com:8081/swagger-ui/

## 🚴🏻 Endpoints

> 조회 기능

GET /api/v1/posts

ex) http://localhost:8081/api/v1/posts

> 수정 기능

PUT /api/v1/{postId}

ex) http://localhost:8081/api/v1/posts/{postId}

> 삭제 기능

DELETE /api/v1/users/{postId}

ex) http://localhost:8081/api/v1/posts/{postId}
