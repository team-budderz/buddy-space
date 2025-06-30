const form = document.getElementById('loginForm');
const message = document.getElementById('message');
const loginBtn = document.getElementById('loginBtn');
const btnText = loginBtn.querySelector('.btn-text');
const loading = loginBtn.querySelector('.loading');

// 쿠키 설정
function setCookie(name, value, days = 1) {
    const expires = new Date(Date.now() + days * 864e5).toUTCString();
    document.cookie = `${name}=${encodeURIComponent(value)}; path=/; expires=${expires}`;
}

// 메시지 표시 함수
function showMessage(text, type = 'error') {
    message.textContent = text;
    message.className = `message ${type} show`;

    // 성공 메시지는 자동으로 사라지지 않음 (페이지 이동하므로)
    if (type === 'error') {
        setTimeout(() => {
            message.classList.remove('show');
        }, 5000); // 5초 후 자동으로 사라짐
    }
}

// 메시지 숨기기 함수
function hideMessage() {
    message.classList.remove('show');
}

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    // 기존 메시지 숨기기
    hideMessage();

    // 로딩 상태 시작
    loginBtn.disabled = true;
    btnText.style.display = 'none';
    loading.classList.add('active');

    const requestBody = {email, password};

    try {
        const response = await fetch(`${API_BASE_URL}/api/users/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestBody),
            credentials: 'include'
        });

        const data = await response.json();

        if (response.ok) {
            const accessToken = data.result?.accessToken;
            if (accessToken) {
                showMessage('로그인 성공! 메인 페이지로 이동합니다...', 'success');
                localStorage.setItem('accessToken', accessToken);
                setCookie("accessToken", accessToken); // 로그인시 쿠키 저장

                setTimeout(() => {
                    window.location.href = 'main.html';
                }, 1500);
            } else {
                showMessage('로그인 실패: 토큰이 없습니다.', 'error');
            }
        } else {
            // 서버 응답에서 메시지 추출
            let errorMessage = '알 수 없는 오류가 발생했습니다.';

            console.log('서버 응답 데이터:', data); // 디버깅용

            if (data) {
                errorMessage = data.message || data.error || data.errorMessage || data.msg || errorMessage;

                // 중첩된 구조 확인
                if (data.error && typeof data.error === 'object') {
                    errorMessage = data.error.message || data.error.msg || errorMessage;
                }

                if (data.result && typeof data.result === 'object') {
                    errorMessage = data.result.message || data.result.error || errorMessage;
                }
            }

            showMessage(errorMessage, 'error');
        }
    } catch (error) {
        showMessage(`요청 실패: ${error.message}`, 'error');
    } finally {
        // 로딩 상태 종료
        loginBtn.disabled = false;
        btnText.style.display = 'inline';
        loading.classList.remove('active');
    }
});

// 입력 필드 실시간 검증
const inputs = document.querySelectorAll('input');
inputs.forEach(input => {
    input.addEventListener('input', function () {
        // 입력 시 기존 에러 메시지 숨기기
        if (message.classList.contains('error')) {
            hideMessage();
        }

        if (this.validity.valid) {
            this.style.borderColor = '#48bb78';
        } else if (this.value.length > 0) {
            this.style.borderColor = '#f56565';
        } else {
            this.style.borderColor = '#e2e8f0';
        }
    });
});

// 엔터 키 처리
document.addEventListener('keydown', function (e) {
    if (e.key === 'Enter' && !loginBtn.disabled) {
        form.dispatchEvent(new Event('submit'));
    }
});