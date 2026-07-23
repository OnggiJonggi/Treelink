# 트리링크

## 🛠 기술 스택

| 영역 | 기술 |
|------|------|
| 프론트 | Thymeleaf (+ Extras Spring Security 6), Bootstrap 5.3.3 |
| 백엔드 | Java 17, Spring Boot 4.0.7, Maven |
| DB | Oracle 26, MyBatis 4.0.1 (Spring Boot Starter) |

## 📄 API 문서

Swagger UI : http://localhost:8080/swagger-ui.html

전체 요청 경로 : doc/요청 주소록.txt

## 🗃️ DB

[ERD](https://www.erdcloud.com/d/eouBZEeTvHRiMui6A)

doc/sql 경로 파일들 순서대로 실행

---

## ⚙️ 환경 설정

### `src/main/resources/config/key.properties`

```properties
# AES-128 평문 키 (16자리 암호화 키)
crypto.key=

# AWS S3
aws.credentials.accessKey=    # 엑세스 키
aws.credentials.secretKey=    # 시크릿 키
aws.s3.region=ap-northeast-2  # 리전
aws.s3.bucket=                # 버킷 이름

# 공공데이터포털 API 키 (Encoding 인증키)
# 국세청 사업자등록정보 진위확인 API
# https://www.data.go.kr/data/15107737/standard.do
public-data.key=

# 카카오 디벨로퍼 - 카카오 지도
# https://apis.map.kakao.com/
kakao-rest.key=   # REST API 키
kakao-js.key=     # JavaScript 키
```