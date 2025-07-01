# 🍽️ 오지라퍼 레스토랑 - 메뉴 관리 시스템

AWS 클라우드 배포를 위한 간단한 메뉴 관리 시스템입니다.

## 📋 프로젝트 개요

이 프로젝트는 다음과 같은 기능을 제공합니다:
- 📋 메뉴 목록 조회
- ➕ 신규 메뉴 추가 (이미지 업로드 포함)
- 🗑️ 메뉴 삭제 (이미지 파일도 함께 삭제)
- 📂 카테고리별 메뉴 분류

## 🛠️ 기술 스택

- **Backend**: Spring Boot 3.5.3, Spring Data JPA
- **Database**: PostgreSQL 17.5
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Build Tool**: Gradle
- **Java Version**: JDK 17

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/ohgiraffers/awsdeploy/
│   │   ├── entity/          # JPA 엔티티
│   │   ├── repository/      # 데이터 접근 계층
│   │   ├── service/         # 비즈니스 로직 계층
│   │   ├── controller/      # REST API 컨트롤러
│   │   ├── dto/             # 데이터 전송 객체
│   │   └── util/            # 유틸리티 클래스
│   └── resources/
│       ├── static/
│       │   ├── index.html   # 프론트엔드 페이지
│       │   └── images/      # 업로드된 이미지 저장소
│       ├── schema.sql       # 데이터베이스 스키마
│       ├── data.sql         # 더미 데이터
│       └── application.yaml # 애플리케이션 설정
└── test/                    # 테스트 코드
```

## 🚀 실행 방법

### 1. 사전 준비

#### PostgreSQL 설치 및 설정
```bash
# PostgreSQL 설치 (Windows의 경우 공식 사이트에서 다운로드)
# macOS의 경우
brew install postgresql

# 데이터베이스 및 사용자 생성
psql -U postgres
CREATE DATABASE menu_db;
CREATE USER menu_user WITH PASSWORD 'menu1234';
GRANT ALL PRIVILEGES ON DATABASE menu_db TO menu_user;
```

### 2. 애플리케이션 실행

```bash
# 프로젝트 루트 디렉토리에서
./gradlew bootRun

# 또는 IDE에서 Chap02AwsDeployApplication.java 실행
```

### 3. 웹 브라우저에서 접속

```
http://localhost:8080
```

## 🔗 API 엔드포인트

### 카테고리 API
- `GET /api/categories` - 모든 카테고리 목록 조회

### 메뉴 API
- `GET /api/menus` - 모든 메뉴 목록 조회
- `GET /api/menus/{id}` - 특정 메뉴 조회
- `POST /api/menus` - 새 메뉴 등록 (multipart/form-data)
- `DELETE /api/menus/{id}` - 메뉴 삭제

### 이미지 API
- `GET /api/images/{filename}` - 이미지 파일 서빙

## 📊 데이터베이스 스키마

### 카테고리 테이블 (tbl_category)
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| category_code | SERIAL | 카테고리 식별코드 (PK) |
| category_name | VARCHAR(255) | 카테고리명 |

### 메뉴 테이블 (tbl_menu)
| 컬럼명 | 타입 | 설명 |
|--------|------|------|
| menu_code | SERIAL | 메뉴 식별코드 (PK) |
| menu_name | VARCHAR(255) | 메뉴명 |
| menu_price | INTEGER | 메뉴 가격 |
| menu_description | TEXT | 메뉴 설명 |
| menu_orderable | CHAR(1) | 주문 가능 여부 |
| category_code | INTEGER | 카테고리 코드 (FK) |
| menu_image_url | VARCHAR(255) | 메뉴 이미지 URL |
| menu_stock | INTEGER | 메뉴 재고 |

## 🔧 설정 파일

### application.yaml 주요 설정
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/menu_db
    username: menu_user
    password: menu1234
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

file:
  upload:
    path: src/main/resources/static/images/
```

## 📝 더미 데이터

애플리케이션 시작 시 다음과 같은 더미 데이터가 자동으로 생성됩니다:

### 카테고리
- 식사
- 디저트  
- 음료

### 메뉴 (일부)
- 열무김치라떼 (4,500원) - 음료
- 우럭스무디 (5,000원) - 음료
- 앙버터김치찜 (13,000원) - 식사
- 갈릭미역파르페 (7,000원) - 디저트
- 등등...

## 🎯 다음 단계 (AWS 연동)

이 시작 프로젝트는 로컬 환경에서 동작하도록 구성되어 있습니다. 
다음 단계에서는 다음과 같은 AWS 서비스와 연동할 예정입니다:

1. **AWS S3** - 파일 저장소 (로컬 파일 시스템 → S3)
2. **AWS RDS** - 데이터베이스 (로컬 PostgreSQL → RDS PostgreSQL)  
3. **AWS ECS** - 컨테이너 배포
4. **AWS ECR** - 컨테이너 이미지 저장소
5. **GitHub Actions** - CI/CD 파이프라인

## 🐛 문제 해결

### 자주 발생하는 문제

1. **PostgreSQL 연결 오류**
   - PostgreSQL 서비스가 실행 중인지 확인
   - 데이터베이스 이름, 사용자명, 비밀번호 확인

2. **포트 충돌**
   - 8080 포트가 이미 사용 중인 경우 application.yaml에서 포트 변경

3. **파일 업로드 오류**
   - 업로드 디렉토리 권한 확인
   - 파일 크기 제한 확인 (10MB)

## 📄 라이선스

이 프로젝트는 교육 목적으로 제작되었습니다.

---

**오지라퍼 개발팀** 🦒 