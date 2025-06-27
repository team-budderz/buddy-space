// 토큰 없으면 로그인 페이지로 이동
function redirectToLogin() {
    window.location.href = "/test/login.html"
}

// accessToken 확인 및 리턴
function getAccessTokenOrRedirect() {
    const token = localStorage.getItem("accessToken")
    if (!token) redirectToLogin()
    return token
}

// 토큰 자동 재발급 포함 fetch wrapper
async function fetchWithAuth(url, options = {}) {
    const token = getAccessTokenOrRedirect()
    options.headers = {
        ...(options.headers || {}),
        Authorization: `Bearer ${token}`,
    }
    options.credentials = "include"

    let response = await fetch(`${API_BASE_URL}${url}`, options)

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

// 로그아웃
function logoutUser() {
    localStorage.removeItem("accessToken")
    window.location.href = "/test/login.html"
}

// 드롭다운 생성 함수
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

    // 클릭 이벤트
    icon.addEventListener("click", (e) => {
        e.stopPropagation()
        // 다른 드롭다운 닫기
        document.querySelectorAll(".dropdown-menu").forEach((menu) => {
            if (menu !== dropdown) {
                menu.classList.remove("show")
            }
        })
        // 현재 드롭다운 토글
        dropdown.classList.toggle("show")
    })

    // 외부 클릭시 드롭다운 닫기
    document.addEventListener("click", (e) => {
        if (!wrapper.contains(e.target)) {
            dropdown.classList.remove("show")
        }
    })

    wrapper.appendChild(icon)
    wrapper.appendChild(dropdown)
    return wrapper
}

// 헤더 렌더링
document.addEventListener("DOMContentLoaded", async () => {
    // CSS 파일 로드
    const link = document.createElement("link")
    link.rel = "stylesheet"
    link.href = "/css/header.css"
    document.head.appendChild(link)

    getAccessTokenOrRedirect()
    window.loggedInUser = null
    let profileImageUrl = "https://raw.githubusercontent.com/withong/my-storage/main/budderz/default.png"

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

    // 헤더 생성
    const header = document.createElement("header")
    header.className = "main-header"

    // 로고 섹션
    const logoSection = document.createElement("a")
    logoSection.href = "/test/main.html"
    logoSection.className = "logo-section"

    const logoImg = document.createElement("img")
    logoImg.src = "https://raw.githubusercontent.com/withong/my-storage/main/budderz/free-icon-wing-12298574.png"
    logoImg.alt = "벗터 로고"
    logoImg.className = "logo-image"

    logoSection.appendChild(logoImg)

    // 검색 섹션
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

    // 검색 기능
    const handleSearch = () => {
        const keyword = searchInput.value.trim()
        if (keyword) {
            window.location.href = `/test/search.html?keyword=${encodeURIComponent(keyword)}`
        }
    }

    searchButton.addEventListener("click", handleSearch)
    searchInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
            handleSearch()
        }
    })

    searchContainer.appendChild(searchInput)
    searchContainer.appendChild(searchButton)
    searchSection.appendChild(searchContainer)

    // 네비게이션 섹션
    const navSection = document.createElement("div")
    navSection.className = "nav-section"

    // 알림 아이콘
    const alarmIcon = createIconWithDropdown(
        "https://raw.githubusercontent.com/withong/my-storage/main/budderz/free-icon-notification-bell-8377307.png",
        "알림",
        `<div class="empty-message">알림이 없습니다</div>`,
    )

    // 채팅 아이콘
    const chatIcon = createIconWithDropdown(
        "https://raw.githubusercontent.com/withong/my-storage/main/budderz/free-icon-conversation-5323491.png",
        "채팅",
        `<div class="empty-message">채팅 내역이 없습니다</div>`,
    )

    // 프로필 드롭다운
    const profileDropdownContent = `
        <div class="dropdown-item" onclick="window.location.href='/test/user/my-page.html'">
            👤 내 정보 조회
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

// 알림/채팅용 placeholder 함수
function fetchAlarmList() {
    return Promise.resolve([])
}

function fetchChatList() {
    return Promise.resolve([])
}

function renderAlarmDropdown(alarms) {
    // 향후 구현
}

function renderChatDropdown(chats) {
    // 향후 구현
}
