/**
 * 모임 초대 처리 페이지
 * - URL에서 초대 코드 추출
 * - 로그인 상태 확인 및 처리
 * - 모임 참여 요청 처리
 * - 성공/실패 상태 표시
 * - 자동 리다이렉트 기능
 */

// 전역 변수
let inviteCode = null
let isProcessing = false

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", async () => {
    // URL에서 초대 코드 추출
    inviteCode = extractInviteCode()

    if (!inviteCode) {
        showError("유효하지 않은 초대 링크입니다.", "초대 코드를 찾을 수 없습니다.")
        return
    }

    // 로그인 상태 확인 및 처리
    await checkLoginStatus()
})

// URL에서 초대 코드 추출
function extractInviteCode() {
    // URL 파라미터에서 코드 추출
    const urlParams = new URLSearchParams(window.location.search)
    const code = urlParams.get("code")

    if (code) {
        return code
    }

    // URL 경로에서 코드 추출 (예: /invite/ABC123)
    const pathParts = window.location.pathname.split("/")
    const lastPart = pathParts[pathParts.length - 1]

    if (lastPart && lastPart !== "invite.html") {
        return lastPart
    }

    return null
}

// 로그인 상태 확인
async function checkLoginStatus() {
    const token = localStorage.getItem("accessToken")

    if (!token) {
        showLoginRequired()
        return
    }

    try {
        // 사용자 정보 확인
        const response = await fetchWithAuth("/api/users/me")

        if (response && response.ok) {
            const userData = await response.json()
            if (userData.result) {
                // 로그인된 상태 - 초대 처리 진행
                showInviteInfo()
            } else {
                showLoginRequired()
            }
        } else {
            showLoginRequired()
        }
    } catch (error) {
        console.error("로그인 상태 확인 실패:", error)
        showLoginRequired()
    }
}

// 로그인 필요 상태 표시
function showLoginRequired() {
    const content = document.getElementById("invite-content")
    const actions = document.getElementById("invite-actions")

    content.innerHTML = `
        <div class="error-state">
            <div class="error-icon">🔐</div>
            <h2 class="error-title">로그인이 필요합니다</h2>
            <p class="error-message">모임에 참여하려면 먼저 로그인해주세요.</p>
        </div>
    `

    actions.innerHTML = `
        <button class="btn btn-primary" onclick="goToLogin()">
            로그인하기
        </button>
        <a href="/test/main.html" class="btn btn-secondary">
            메인으로 돌아가기
        </a>
    `

    actions.style.display = "flex"
    setupEnterKeyHandler()
}

// 초대 정보 표시
function showInviteInfo() {
    const content = document.getElementById("invite-content")
    const actions = document.getElementById("invite-actions")

    content.innerHTML = `
        <div class="group-info">
            <h2 class="group-name">모임 초대</h2>
            <p class="group-description">
                초대 코드: <strong>${inviteCode}</strong><br>
                아래 버튼을 클릭하여 모임에 참여하세요.
            </p>
        </div>
    `

    actions.innerHTML = `
        <button class="btn btn-primary" onclick="joinGroup()" id="join-btn">
            모임 참여하기
        </button>
        <a href="/test/main.html" class="btn btn-secondary">
            취소
        </a>
    `

    actions.style.display = "flex"
    setupEnterKeyHandler()
}

// 모임 참여 처리
async function joinGroup() {
    if (isProcessing) return

    isProcessing = true
    const joinBtn = document.getElementById("join-btn")

    // 버튼 상태 변경
    if (joinBtn) {
        setButtonLoadingState(joinBtn, "참여 중...")
    }

    try {
        const response = await fetchWithAuth("/api/invites", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: `code=${encodeURIComponent(inviteCode)}`,
        })

        const data = await response.json()

        if (response.ok && data.result) {
            showSuccess(data.result)
        } else {
            throw new Error(data.message || "모임 참여에 실패했습니다.")
        }
    } catch (error) {
        console.error("모임 참여 실패:", error)
        showError("참여 실패", error.message || "모임 참여 중 오류가 발생했습니다.")

        // 버튼 상태 복원
        if (joinBtn) {
            resetButtonState(joinBtn, "모임 참여하기")
        }
    } finally {
        isProcessing = false
    }
}

// 버튼 로딩 상태 설정
function setButtonLoadingState(button, text) {
    button.textContent = text
    button.disabled = true
}

// 버튼 상태 복원
function resetButtonState(button, text) {
    button.textContent = text
    button.disabled = false
}

// 성공 상태 표시
function showSuccess(membershipData) {
    const content = document.getElementById("invite-content")
    const actions = document.getElementById("invite-actions")

    content.innerHTML = `
        <div class="success-state">
            <div class="success-icon">🎉</div>
            <h2 class="success-title">참여 완료!</h2>
            <p class="success-message">
                <strong>${membershipData.groupName}</strong> 모임에 성공적으로 참여했습니다.
            </p>
            <div class="group-info">
                <h3 class="group-name">${membershipData.groupName}</h3>
                <div class="member-count">
                    👥 멤버 ${membershipData.members ? membershipData.members.length : 0}명
                </div>
            </div>
        </div>
    `

    actions.innerHTML = `
        <button class="btn btn-success" onclick="goToGroupMain(${membershipData.groupId})">
            모임으로 이동
        </button>
        <a href="/test/main.html" class="btn btn-secondary">
            메인으로 이동
        </a>
    `

    actions.style.display = "flex"
    setupEnterKeyHandler()

    // 3초 후 자동으로 모임 메인으로 이동
    setTimeout(() => {
        goToGroupMain(membershipData.groupId)
    }, 3000)
}

// 에러 상태 표시
function showError(title, message) {
    const content = document.getElementById("invite-content")
    const actions = document.getElementById("invite-actions")

    content.innerHTML = `
        <div class="error-state">
            <div class="error-icon">❌</div>
            <h2 class="error-title">${title}</h2>
            <p class="error-message">${message}</p>
        </div>
    `

    actions.innerHTML = `
        <button class="btn btn-primary" onclick="location.reload()">
            다시 시도
        </button>
        <a href="/test/main.html" class="btn btn-secondary">
            메인으로 돌아가기
        </a>
    `

    actions.style.display = "flex"
    setupEnterKeyHandler()
}

// 로그인 페이지로 이동
function goToLogin() {
    // 현재 URL을 리다이렉트 파라미터로 저장
    const currentUrl = encodeURIComponent(window.location.href)
    window.location.href = `/test/login.html?redirect=${currentUrl}`
}

// 모임 메인으로 이동
function goToGroupMain(groupId) {
    window.location.href = `/test/group/main.html?id=${groupId}`
}

// 엔터키 이벤트 핸들러 설정
function setupEnterKeyHandler() {
    document.addEventListener("keydown", handleEnterKey)
}

// 엔터키 처리
function handleEnterKey(e) {
    if (e.key === "Enter") {
        const primaryBtn = document.querySelector(".btn-primary")
        if (primaryBtn && !primaryBtn.disabled) {
            primaryBtn.click()
        }
    }
}
