/**
 * 사용자 로그인 페이지
 * - 이메일/비밀번호 로그인 처리
 * - 실시간 입력 검증
 * - 로딩 상태 관리
 * - 에러 메시지 표시
 * - 엔터키 로그인 지원
 */

// DOM 요소 참조
const form = document.getElementById("loginForm")
const message = document.getElementById("message")
const loginBtn = document.getElementById("loginBtn")
const btnText = loginBtn.querySelector(".btn-text")
const loading = loginBtn.querySelector(".loading")

// 쿠키 설정
function setCookie(name, value, days = 1) {
    const expires = new Date(Date.now() + days * 864e5).toUTCString();
    document.cookie = `${name}=${encodeURIComponent(value)}; path=/; expires=${expires}`;
}

// 메시지 표시 함수
function showMessage(text, type = "error") {
    message.textContent = text
    message.className = `message ${type} show`

    // 에러 메시지는 5초 후 자동으로 사라짐
    if (type === "error") {
        setTimeout(() => {
            message.classList.remove("show")
        }, 5000)
    }
}

// 메시지 숨기기 함수
function hideMessage() {
    message.classList.remove("show")
}

// 로딩 상태 시작
function startLoading() {
    loginBtn.disabled = true
    btnText.style.display = "none"
    loading.classList.add("active")
}

// 로딩 상태 종료
function stopLoading() {
    loginBtn.disabled = false
    btnText.style.display = "inline"
    loading.classList.remove("active")
}

// 로그인 처리 함수
async function handleLogin(email, password) {
    hideMessage()
    startLoading()

    const requestBody = { email, password }

    try {
        const response = await fetch(`${API_BASE_URL}/api/users/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(requestBody),
            credentials: "include",
        })

        const data = await response.json()

        if (response.ok) {
            const accessToken = data.result?.accessToken
            if (accessToken) {
                showMessage('로그인 성공! 메인 페이지로 이동합니다...', 'success');
                localStorage.setItem('accessToken', accessToken);
                setCookie("accessToken", accessToken); // 로그인시 쿠키 저장

                setTimeout(() => {
                    window.location.href = "main"
                }, 1500)
            } else {
                showMessage("로그인 실패: 토큰이 없습니다.", "error")
            }
        } else {
            // 서버 응답에서 에러 메시지 추출
            let errorMessage = "알 수 없는 오류가 발생했습니다."

            if (data) {
                errorMessage = data.message || data.error || data.errorMessage || data.msg || errorMessage

                // 중첩된 구조 확인
                if (data.error && typeof data.error === "object") {
                    errorMessage = data.error.message || data.error.msg || errorMessage
                }

                if (data.result && typeof data.result === "object") {
                    errorMessage = data.result.message || data.result.error || errorMessage
                }
            }

            showMessage(errorMessage, "error")
        }
    } catch (error) {
        showMessage(`요청 실패: ${error.message}`, "error")
    } finally {
        stopLoading()
    }
}

// 입력 필드 실시간 검증
function setupInputValidation() {
    const inputs = document.querySelectorAll("input")
    inputs.forEach((input) => {
        input.addEventListener("input", function () {
            // 입력 시 기존 에러 메시지 숨기기
            if (message.classList.contains("error")) {
                hideMessage()
            }

            // 입력 필드 색상 변경
            if (this.validity.valid) {
                this.style.borderColor = "#48bb78"
            } else if (this.value.length > 0) {
                this.style.borderColor = "#f56565"
            } else {
                this.style.borderColor = "#e2e8f0"
            }
        })
    })
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 폼 제출 이벤트
    form.addEventListener("submit", async (e) => {
        e.preventDefault()

        const email = document.getElementById("email").value
        const password = document.getElementById("password").value

        await handleLogin(email, password)
    })

    // 엔터 키 처리
    document.addEventListener("keydown", (e) => {
        if (e.key === "Enter" && !loginBtn.disabled) {
            form.dispatchEvent(new Event("submit"))
        }
    })

    // 입력 필드 검증 설정
    setupInputValidation()
}

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", () => {
    setupEventListeners()
})
