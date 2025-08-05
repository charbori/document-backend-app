# 프로젝트 개요

## 프로젝트 정보

*   **프로젝트 이름:** video-app-backend
*   **언어:** Java 17
*   **프레임워크:** Spring Boot 3.2.2
*   **빌드 도구:** Gradle

## Running
* gradle 빌드 : ./gradlew bootjar 
* jar 파일 실행 : java -jar app.jar

## 현재 운영서비스
https://couhensoft.com/compare
  
## 주요 의존성

*   **데이터베이스:** MySQL, H2 (테스트용)
*   **보안:** 토큰 기반 인증을 위한 JWT
*   **API:** 사용자 및 문서 관리를 위한 RESTful API
*   **기타:** Lombok, 캐싱을 위한 Ehcache, Caffeine

## 핵심 기능

*   **사용자 관리:** 사용자 등록 (`JoinDTO`, `JoinService`), 인증 (`CustomUserDetailsService`, `JwtAuthFilter`) 및 인가.
*   **문서 관리:**
    *   문서 비교 기능 (`DiffService`, `ApiDiffController`).
*   **API:**
    *   애플리케이션 기능과 상호 작용하기 위한 RESTful API (`Api*Controller`).
    *   사용자 지정 필터 (`ApiAuthFilter`)를 사용한 API 인증.
*   **웹 인터페이스:**
    *   웹 페이지의 서버 측 렌더링을 위한 Thymeleaf.
    *   웹 요청을 처리하기 위한 컨트롤러 (`LoginController`, `PrivacyPolicyController` 등).
*   **보안:**
    *   인증 및 인가를 위한 Spring Security.
    *   REST API 보안을 위한 JWT.
    *   인증 및 인가 오류에 대한 사용자 지정 예외 처리기.
*   **캐싱:**
    *   자주 액세스하는 데이터를 캐싱하여 성능을 향상시키기 위한 Ehcache 및 Caffeine.

## 프로젝트 구조

프로젝트는 표준 Spring Boot 프로젝트 구조를 따릅니다.

*   `src/main/java/main/blog`: 애플리케이션의 기본 패키지.
    *   `config`: Spring Boot 구성 클래스.
    *   `domain`: DTO, 엔티티, 리포지토리 및 서비스를 포함한 핵심 도메인 로직.
    *   `exception`: 사용자 지정 예외 클래스.
    *   `filter`: 인증 및 인가를 위한 서블릿 필터.
    *   `resolver`: 사용자 지정 인수 확인자.
    *   `util`: 유틸리티 클래스.
    *   `web`: REST 컨트롤러 및 기존 컨트롤러를 포함한 웹 계층.
*   `src/main/resources`: 구성 파일, 템플릿 및 정적 자산을 포함한 애플리케이션 리소스.
*   `src/test`: 테스트 소스 코드.
