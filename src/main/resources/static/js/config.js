/**
 * 애플리케이션 설정 파일
 * - API 기본 URL 설정
 * - 개발/운영 환경 구분
 * - 전역 설정 값 관리
 */

const hostname = window.location.hostname;

let API_BASE_URL;

if (hostname.includes("localhost") || hostname.includes("127.0.0.1")) {
    API_BASE_URL = "http://localhost:8080";
} else {
    API_BASE_URL = "https://api.budderz.co.kr";
}

console.log("API_BASE_URL", API_BASE_URL);