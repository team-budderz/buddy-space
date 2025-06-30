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

let notificationEventSource = null;

// 사용자 로그아웃 처리
function logoutUser() {
    localStorage.removeItem("accessToken")
    window.location.href = "/test/login.html"
}

// SSE 연결 함수
function connectNotificationSSE() {
    const token = localStorage.getItem("accessToken");
    if (!token) return;

    if (notificationEventSource) {
        notificationEventSource.close();
    }

    let clientId = localStorage.getItem('clientId');
    if (!clientId) {
        clientId = crypto.randomUUID();
        localStorage.setItem('clientId', clientId);
    }

    notificationEventSource = new EventSource(`${API_BASE_URL}/api/notifications/subscribe?clientId=${clientId}`, {
        withCredentials: true
    });

    notificationEventSource.addEventListener("connect", (event) => {
        console.log("SSE 연결 성공:", event.data);
        const statusEl = document.getElementById("sse-status");
        if (statusEl) {
            statusEl.textContent = "SSE: 연결 성공";
            statusEl.style.backgroundColor = "#d4edda";
            statusEl.style.borderColor = "#c3e6cb";
            statusEl.style.color = "#155724";
        }
    });

    notificationEventSource.addEventListener("notification", async (event) => {
        const notification = JSON.parse(event.data);
        console.log("새 알림 도착:", notification);

        await initAlarmDropdownWithPaging(); // 새 알림에 따라 전체 목록 재갱신

        const alarms = await fetchAlarmList();
        if (alarms.some(alarm => !alarm.isRead)) {
            showNotificationRedDot();
        }
    });

    notificationEventSource.onerror = (event) => {
        console.error("SSE 연결 오류 또는 종료", event);
        notificationEventSource.close();

        const statusEl = document.getElementById("sse-status");
        if (statusEl) {
            statusEl.textContent = "SSE: 연결 실패 또는 종료됨";
            statusEl.style.backgroundColor = "#f8d7da";
            statusEl.style.borderColor = "#f5c6cb";
            statusEl.style.color = "#721c24";
        }

        setTimeout(connectNotificationSSE, 5000);
    };
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
    await initAlarmDropdownWithPaging();    // 처음 페이지 접속 시 알림 무한스크롤 세팅
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

    // SSE 상태 표시용 DOM 요소 생성
    const sseStatus = document.createElement("div");
    sseStatus.id = "sse-status";
    sseStatus.className = "sse-status";
    sseStatus.textContent = "SSE: 연결 대기 중";

    // 네비게이션에 아이콘들 추가
    navSection.appendChild(alarmIcon)
    navSection.appendChild(chatIcon)
    navSection.appendChild(profileWrapper)

    // 헤더에 모든 섹션 추가
    header.appendChild(logoSection)
    header.appendChild(searchSection)
    header.appendChild(navSection)
    // header.appendChild(sseStatus)   // SSE 연결 여부 확인 css (실전용 나중에 주석 풀어주세요!)

    // 페이지에 헤더 추가
    document.body.prepend(header)

    // 페이지 하단에 SSE 연결 여부 확인 (테스트용)
    document.body.appendChild(sseStatus);

    // 알림 목록 로드 및 렌더링
    async function initAlarms() {
        const alarms = await fetchAlarmList();
        renderAlarmDropdown(alarms);
        if (alarms.some(alarm => !alarm.isRead)) {
            showNotificationRedDot();
        }
    }

    initAlarms();
    connectNotificationSSE();
})

// 알림/채팅용 placeholder 함수
// 알림 리스트 API 호출
async function fetchAlarmList() {
    try {
        const response = await fetchWithAuth("/api/notifications");
        if (!response.ok) throw new Error("알림 불러오기 실패");
        const data = await response.json();
        return data.result.content || [];
    } catch (error) {
        console.error("알림 목록 요청 중 오류:", error);
        return [];
    }
}

// 알림 드롭다운 렌더링
function renderAlarmDropdown(alarms) {
    const alarmWrapper = document.querySelector('img[alt="알림"]')?.parentElement;
    const dropdown = alarmWrapper?.querySelector('div');
    if (!dropdown) return;

    if (!alarms || alarms.length === 0) {
        dropdown.innerHTML = `<div style="padding: 10px;">알림이 없습니다</div>`;
        return;
    }

    const listHTML = alarms.map(alarm => {
        const date = new Date(alarm.createdAt);
        const formattedDate = date.toLocaleString('ko-KR', {
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit'
        });

        return `
            <div class="alarm-item" data-url="${alarm.url}" data-id="${alarm.notificationId}" style="padding: 10px; border-bottom: 1px solid #eee; cursor: pointer;">
                <div style="font-weight: ${alarm.isRead ? 'normal' : 'bold'};">${alarm.content}</div>
                <div style="font-size: 12px; color: #666;">${alarm.groupName} · ${formattedDate}</div>
            </div>
        `;
    }).join("");

    dropdown.innerHTML = listHTML;

    dropdown.querySelectorAll('.alarm-item').forEach(item => {
        item.addEventListener('click', async () => {
            const notificationId = item.dataset.id;
            const apiUrl = item.dataset.url;
            const contentEl = item.querySelector("div");

            try {
                const res = await fetchWithAuth(`/api/notifications/${notificationId}/read`, {
                    method: "PATCH"
                });

                if (res.ok && contentEl) {
                    contentEl.style.fontWeight = "normal";
                    const pageUrl = convertApiUrlToPageUrl(apiUrl);
                    window.location.href = pageUrl;
                } else {
                    console.warn("알림 읽음 처리 실패");
                }
            } catch (e) {
                console.error("읽음 처리 중 오류:", e);
            }
        });
    });
}

// API URL → 실제 페이지 URL로 변환
function convertApiUrlToPageUrl(apiUrl) {
    const postMatch = apiUrl.match(/^\/api\/groups\/(\d+)\/posts\/(\d+)$/);
    if (postMatch) {
        const groupId = postMatch[1];
        const postId = postMatch[2];
        return `/test/group/post.html?groupId=${groupId}&postId=${postId}`;
    }
    return apiUrl;
}

// 빨간점 표시
function showNotificationRedDot() {
    const alarmWrapper = document.querySelector('img[alt="알림"]')?.parentElement;
    if (!alarmWrapper || alarmWrapper.querySelector('.red-dot')) return;

    const dot = document.createElement("span");
    dot.classList.add("red-dot");
    dot.setAttribute("style", redDotStyle);
    alarmWrapper.style.position = "relative"; // 포지션 필요
    alarmWrapper.appendChild(dot);
}

function setCookie(name, value, days = 1) {
    const expires = new Date(Date.now() + days * 864e5).toUTCString();
    document.cookie = `${name}=${encodeURIComponent(value)}; path=/; expires=${expires}`;
}

function deleteCookie(name) {
    document.cookie = `${name}=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;`;
}

let currentPage = 0;
let lastPage = false;

/**
 * 페이지 단위로 알림 목록을 불러오는 함수
 * @param {number} page - 요청할 페이지 번호 (0부터 시작)
 * @returns {Promise<Array>} 알림 목록 배열
 */
async function fetchAlarmListPage(page = 0) {
    try {
        const response = await fetchWithAuth(`/api/notifications?page=${page}`);
        if (!response.ok) throw new Error("알림 페이지 요청 실패");

        const data = await response.json();

        lastPage = data.result.last;         // 다음 페이지 없으면 true
        currentPage = data.result.number + 1; // 다음 요청할 페이지 번호 업데이트

        return data.result.content || [];    // 알림 목록 반환
    } catch (error) {
        console.error("알림 페이지 불러오기 오류:", error);
        lastPage = true; // 에러 시 추가 요청 막기
        return [];
    }
}

function renderAlarmItemsHTML(alarms) {
    return alarms.map(alarm => {
        const date = new Date(alarm.createdAt);
        const formattedDate = date.toLocaleString('ko-KR', {
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit'
        });

        return `
            <div class="alarm-item" data-url="${alarm.url}" data-id="${alarm.notificationId}" style="padding: 10px; border-bottom: 1px solid #eee; cursor: pointer;">
                <div style="font-weight: ${alarm.isRead ? 'normal' : 'bold'};">${alarm.content}</div>
                <div style="font-size: 12px; color: #666;">${alarm.groupName} · ${formattedDate}</div>
            </div>
        `;
    }).join("");
}

async function initAlarmDropdownWithPaging() {
    const alarmWrapper = document.querySelector('img[alt="알림"]')?.parentElement;
    const dropdown = alarmWrapper?.querySelector('div');
    if (!dropdown) return;

    dropdown.innerHTML = ""; // 기존 내용 제거
    currentPage = 0;
    lastPage = false;

    const alarms = await fetchAlarmListPage(currentPage);
    dropdown.innerHTML = renderAlarmItemsHTML(alarms);
    rebindAlarmClickEvents(dropdown); // 클릭 이벤트 다시 연결

    const sentinel = document.createElement("div");
    sentinel.id = "alarm-sentinel";
    sentinel.style.height = "1px";
    sentinel.style.visibility = "hidden";
    dropdown.appendChild(sentinel);

    const observer = new IntersectionObserver(async (entries) => {
        const entry = entries[0];
        if (entry.isIntersecting && !lastPage) {
            const moreAlarms = await fetchAlarmListPage(currentPage);
            const moreHTML = renderAlarmItemsHTML(moreAlarms);
            dropdown.insertAdjacentHTML("beforeend", moreHTML);
            rebindAlarmClickEvents(dropdown);
        }
    }, { threshold: 1 });

    observer.observe(sentinel);
}

function rebindAlarmClickEvents(dropdown) {
    dropdown.querySelectorAll('.alarm-item').forEach(item => {
        item.addEventListener('click', async () => {
            const notificationId = item.dataset.id;
            const apiUrl = item.dataset.url;
            const contentEl = item.querySelector("div");

            try {
                const res = await fetchWithAuth(`/api/notifications/${notificationId}/read`, { method: "PATCH" });

                if (res.ok && contentEl) {
                    contentEl.style.fontWeight = "normal";
                    const pageUrl = convertApiUrlToPageUrl(apiUrl);
                    window.location.href = pageUrl;
                } else {
                    console.warn("알림 읽음 처리 실패");
                }
            } catch (e) {
                console.error("읽음 처리 중 오류:", e);
            }
        });
    });
}

function fetchChatList() {
    return Promise.resolve([]);
}

function renderChatDropdown(chats) {
  // 향후 구현
}