/**
 * 공통 헤더 컴포넌트
 * - 사용자 인증 및 토큰 관리
 * - 네비게이션 및 검색 기능
 * - 프로필, 알림, 채팅 드롭다운
 * - 인증이 필요한 API 요청을 위한 fetchWithAuth 함수 제공
 */

// 로그인 페이지로 리다이렉트
function redirectToLogin() {
    window.location.href = "/test/login.html"
}

// 액세스 토큰 확인 및 반환
function getAccessTokenOrRedirect() {
    const token = localStorage.getItem("accessToken")
    if (!token) redirectToLogin()
    return token
}

// 토큰 자동 재발급을 포함한 인증 fetch 래퍼
async function fetchWithAuth(url, options = {}) {
    const token = getAccessTokenOrRedirect()
    options.headers = {
        ...(options.headers || {}),
        Authorization: `Bearer ${token}`,
    }
    options.credentials = "include"

    let response = await fetch(`${API_BASE_URL}${url}`, options)

    // 토큰 만료 시 자동 재발급 시도
    if (response.status === 401) {
        const refreshRes = await fetch(`${API_BASE_URL}/api/token/refresh`, {
            method: "POST",
            credentials: "include",
        })

        const refreshData = await refreshRes.json()

        if (refreshRes.ok && refreshData.result?.accessToken) {
            const newToken = refreshData.result.accessToken
            localStorage.setItem("accessToken", newToken)
            options.headers.Authorization = `Bearer ${newToken}`
            response = await fetch(`${API_BASE_URL}${url}`, options)
        } else {
            alert("로그인이 만료되었습니다. 다시 로그인해주세요.")
            redirectToLogin()
            return
        }
    }

    return response
}

// 사용자 로그아웃 처리
function logoutUser() {
    localStorage.removeItem("accessToken")
    window.location.href = "/test/login.html"
}

// 아이콘과 드롭다운 메뉴를 생성하는 공통 함수
function createIconWithDropdown(iconSrc, altText, dropdownContentHTML) {
    const wrapper = document.createElement("div")
    wrapper.className = "dropdown-wrapper"

    const icon = document.createElement("img")
    icon.src = iconSrc
    icon.alt = altText
    icon.className = "nav-icon"

    const dropdown = document.createElement("div")
    dropdown.className = "dropdown-menu"
    dropdown.innerHTML = dropdownContentHTML

    // 아이콘 클릭 시 드롭다운 토글
    icon.addEventListener("click", (e) => {
        e.stopPropagation()
        // 다른 드롭다운 메뉴 닫기
        document.querySelectorAll(".dropdown-menu").forEach((menu) => {
            if (menu !== dropdown) {
                menu.classList.remove("show")
            }
        })
        dropdown.classList.toggle("show")
    })

    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener("click", (e) => {
        if (!wrapper.contains(e.target)) {
            dropdown.classList.remove("show")
        }
    })

    wrapper.appendChild(icon)
    wrapper.appendChild(dropdown)
    return wrapper
}

// 검색 기능 처리
function handleSearch(searchInput) {
    const keyword = searchInput.value.trim()
    if (keyword) {
        window.location.href = `/test/search.html?keyword=${encodeURIComponent(keyword)}`
    }
}

// 헤더 렌더링 및 초기화
document.addEventListener("DOMContentLoaded", async () => {
    // 헤더 CSS 파일 동적 로드
    const link = document.createElement("link")
    link.rel = "stylesheet"
    link.href = "/css/header.css"
    document.head.appendChild(link)

    // 토큰 확인
    getAccessTokenOrRedirect()
    window.loggedInUser = null
    let profileImageUrl = "https://raw.githubusercontent.com/withong/my-storage/main/budderz/default.png"

    // 사용자 정보 로드
    try {
        const response = await fetchWithAuth("/api/users/me")
        const data = await response.json()
        if (response.ok) {
            profileImageUrl = data.result.profileImageUrl || profileImageUrl
            window.loggedInUser = data.result
        } else {
            console.warn("사용자 정보 조회 실패:", data.message || data.code)
            redirectToLogin()
        }
    } catch (err) {
        console.error("사용자 정보 요청 중 오류:", err)
    }

    // 헤더 엘리먼트 생성
    const header = document.createElement("header")
    header.className = "main-header"

    // 로고 섹션 생성
    const logoSection = document.createElement("a")
    logoSection.href = "/test/main.html"
    logoSection.className = "logo-section"

    const logoImg = document.createElement("img")
    logoImg.src = "https://raw.githubusercontent.com/withong/my-storage/main/budderz/free-icon-wing-12298574.png"
    logoImg.alt = "벗터 로고"
    logoImg.className = "logo-image"

    logoSection.appendChild(logoImg)

    // 검색 섹션 생성
    const searchSection = document.createElement("div")
    searchSection.className = "search-section"

    const searchContainer = document.createElement("div")
    searchContainer.className = "search-container"

    const searchInput = document.createElement("input")
    searchInput.type = "text"
    searchInput.placeholder = "모임 이름 검색"
    searchInput.className = "search-input"

    const searchButton = document.createElement("button")
    searchButton.textContent = "검색"
    searchButton.className = "search-button"

    // 검색 이벤트 리스너 등록
    searchButton.addEventListener("click", () => handleSearch(searchInput))
    searchInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
            handleSearch(searchInput)
        }
    })

    searchContainer.appendChild(searchInput)
    searchContainer.appendChild(searchButton)
    searchSection.appendChild(searchContainer)

    // 네비게이션 섹션 생성
    const navSection = document.createElement("div")
    navSection.className = "nav-section"

    // 알림 아이콘 생성
    const alarmIcon = createIconWithDropdown(
        "https://raw.githubusercontent.com/withong/my-storage/main/budderz/free-icon-notification-bell-8377307.png",
        "알림",
        `<div class="empty-message">알림이 없습니다</div>`,
    )

    // 채팅 아이콘 생성
    const chatIcon = createIconWithDropdown(
        "https://raw.githubusercontent.com/withong/my-storage/main/budderz/free-icon-conversation-5323491.png",
        "채팅",
        `<div class="empty-message">채팅 내역이 없습니다</div>`,
    )

    // 프로필 드롭다운 메뉴 생성
    const profileDropdownContent = `
        <div class="dropdown-item" onclick="window.location.href='/test/my/profile.html'">
            👤 내 정보
        </div>
        <div class="dropdown-item" onclick="logoutUser()">
            🚪 로그아웃
        </div>
    `

    const profileWrapper = createIconWithDropdown(profileImageUrl, "프로필", profileDropdownContent)

    // 프로필 이미지 스타일 적용
    const profileImg = profileWrapper.querySelector("img")
    profileImg.className = "profile-image"

    // 네비게이션에 아이콘들 추가
    navSection.appendChild(alarmIcon)
    navSection.appendChild(chatIcon)
    navSection.appendChild(profileWrapper)

    // 헤더에 모든 섹션 추가
    header.appendChild(logoSection)
    header.appendChild(searchSection)
    header.appendChild(navSection)

    // 페이지에 헤더 추가
    document.body.prepend(header)
})

// 알림 목록 조회 (향후 구현 예정)
function fetchAlarmList() {
    return Promise.resolve([])
}

// 채팅 목록 조회 (향후 구현 예정)
function fetchChatList() {
    return Promise.resolve([])
}

// 알림 드롭다운 렌더링 (향후 구현 예정)
function renderAlarmDropdown(alarms) {
    // 향후 구현
}

// 채팅 드롭다운 렌더링 (향후 구현 예정)
function renderChatDropdown(chats) {
    // 향후 구현
}