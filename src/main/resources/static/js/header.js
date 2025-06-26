// 토큰 자동 재발급 기능 포함한 fetch wrapper 정의
async function fetchWithAuth(url, options = {}) {
    let accessToken = localStorage.getItem("accessToken");
    if (!accessToken) {
        window.location.href = "/test/login.html";
        return;
    }

    // 기본 Authorization 헤더 설정
    options.headers = {
        ...(options.headers || {}),
        Authorization: `Bearer ${accessToken}`
    };
    options.credentials = "include";

    let response = await fetch(`${API_BASE_URL}${url}`, options);

    if (response.status === 401) {
        // accessToken 만료 → refresh 요청 시도
        const refreshRes = await fetch(`${API_BASE_URL}/api/token/refresh`, {
            method: "POST",
            credentials: "include"
        });

        const refreshData = await refreshRes.json();

        if (refreshRes.ok && refreshData.result?.accessToken) {
            // 새로운 accessToken 저장하고 요청 재시도
            localStorage.setItem("accessToken", refreshData.result.accessToken);
            options.headers.Authorization = `Bearer ${refreshData.result.accessToken}`;
            response = await fetch(`${API_BASE_URL}${url}`, options);
        } else {
            alert("로그인이 만료되었습니다. 다시 로그인해주세요.");
            window.location.href = "/test/login.html";
            return;
        }
    }

    return response;
}

document.addEventListener("DOMContentLoaded", async () => {
    let accessToken = localStorage.getItem("accessToken");
    if (!accessToken) {
        window.location.href = "/test/login.html";
        return;
    }

    window.loggedInUser = null;
    let profileImageUrl = "https://raw.githubusercontent.com/withong/my-storage/main/budderz/default.png";

    try {
        const response = await fetchWithAuth("/api/users/me", {
            method: "GET"
        });

        const data = await response.json();
        if (response.ok) {
            profileImageUrl = data.result.profileImageUrl || profileImageUrl;
            window.loggedInUser = data.result;
        } else {
            console.warn("사용자 정보 조회 실패:", data.message || data.code);
            window.location.href = "/test/login.html";
        }
    } catch (err) {
        console.error("사용자 정보 요청 중 오류:", err);
    }

    // 헤더 생성
    const header = document.createElement("header");
    header.style.display = "flex";
    header.style.justifyContent = "space-between";
    header.style.alignItems = "center";
    header.style.padding = "10px 20px";
    header.style.backgroundColor = "#ffffff";
    header.style.borderBottom = "1px solid #ddd";
    header.style.boxShadow = "0 2px 5px rgba(0,0,0,0.05)";
    header.style.position = "sticky";
    header.style.top = "0";
    header.style.zIndex = "1000";

    // 로고
    const logo = document.createElement("a");
    logo.href = "/test/main.html";
    const logoImg = document.createElement("img");
    logoImg.src = "https://raw.githubusercontent.com/withong/my-storage/main/budderz/free-icon-wing-12298568.png";
    logoImg.alt = "벗터 로고";
    logoImg.style.height = "32px";
    logoImg.style.cursor = "pointer";
    logo.appendChild(logoImg);

    // 검색창
    const searchContainer = document.createElement("div");
    searchContainer.style.display = "flex";
    searchContainer.style.alignItems = "center";
    const searchInput = document.createElement("input");
    searchInput.type = "text";
    searchInput.placeholder = "모임 이름 검색";
    searchInput.style.padding = "6px 12px";
    searchInput.style.border = "1px solid #ccc";
    searchInput.style.borderRadius = "6px 0 0 6px";
    searchInput.style.width = "240px";
    searchInput.style.outline = "none";
    const searchButton = document.createElement("button");
    searchButton.textContent = "검색";
    searchButton.style.padding = "6px 12px";
    searchButton.style.border = "1px solid #ccc";
    searchButton.style.borderLeft = "0";
    searchButton.style.borderRadius = "0 6px 6px 0";
    searchButton.style.backgroundColor = "#f2f2f2";
    searchButton.style.cursor = "pointer";
    searchButton.addEventListener("click", () => {
        const keyword = searchInput.value.trim();
        if (keyword) {
            window.location.href = `/test/search.html?keyword=${keyword}`;
        }
    });
    searchContainer.appendChild(searchInput);
    searchContainer.appendChild(searchButton);

    // 드롭다운 생성 함수
    function createIconWithDropdown(iconSrc, altText, dropdownContentHTML) {
        const wrapper = document.createElement("div");
        wrapper.style.position = "relative";

        const icon = document.createElement("img");
        icon.src = iconSrc;
        icon.alt = altText;
        icon.style.width = "24px";
        icon.style.height = "24px";
        icon.style.cursor = "pointer";

        const dropdown = document.createElement("div");
        dropdown.style.display = "none";
        dropdown.style.position = "absolute";
        dropdown.style.top = "32px";
        dropdown.style.right = "0";
        dropdown.style.backgroundColor = "#fff";
        dropdown.style.border = "1px solid #ccc";
        dropdown.style.borderRadius = "6px";
        dropdown.style.boxShadow = "0 2px 8px rgba(0,0,0,0.1)";
        dropdown.style.minWidth = "180px";
        dropdown.style.zIndex = "1001";
        dropdown.innerHTML = dropdownContentHTML;

        icon.addEventListener("click", () => {
            dropdown.style.display = dropdown.style.display === "block" ? "none" : "block";
        });

        document.addEventListener("click", (e) => {
            if (!wrapper.contains(e.target)) {
                dropdown.style.display = "none";
            }
        });

        wrapper.appendChild(icon);
        wrapper.appendChild(dropdown);
        return wrapper;
    }

    // 알림 아이콘
    const alarmIcon = createIconWithDropdown(
        "https://raw.githubusercontent.com/withong/my-storage/main/budderz/free-icon-notification-bell-8377307.png",
        "알림",
        `<div style="padding: 10px">알림이 없습니다</div>`
    );

    // 채팅 아이콘
    const chatIcon = createIconWithDropdown(
        "https://raw.githubusercontent.com/withong/my-storage/main/budderz/free-icon-conversation-5323491.png",
        "채팅",
        `<div style="padding: 10px">채팅 내역이 없습니다</div>`
    );

    // 프로필 이미지 + 메뉴
    const profileWrapper = document.createElement("div");
    profileWrapper.style.position = "relative";

    const profileImg = document.createElement("img");
    profileImg.src = profileImageUrl;
    profileImg.alt = "프로필";
    profileImg.style.width = "36px";
    profileImg.style.height = "36px";
    profileImg.style.borderRadius = "50%";
    profileImg.style.cursor = "pointer";

    const profileDropdown = document.createElement("div");
    profileDropdown.style.display = "none";
    profileDropdown.style.position = "absolute";
    profileDropdown.style.top = "44px";
    profileDropdown.style.right = "0";
    profileDropdown.style.backgroundColor = "#fff";
    profileDropdown.style.border = "1px solid #ccc";
    profileDropdown.style.borderRadius = "6px";
    profileDropdown.style.boxShadow = "0 2px 8px rgba(0,0,0,0.1)";
    profileDropdown.style.minWidth = "140px";
    profileDropdown.style.zIndex = "1001";

    const myPageItem = document.createElement("div");
    myPageItem.textContent = "내 정보 조회";
    myPageItem.style.padding = "10px 12px";
    myPageItem.style.cursor = "pointer";
    myPageItem.style.color = "#333";
    myPageItem.addEventListener("click", () => {
        window.location.href = "/test/user/my-page.html";
    });

    profileDropdown.appendChild(myPageItem);
    profileWrapper.appendChild(profileImg);
    profileWrapper.appendChild(profileDropdown);

    profileImg.addEventListener("click", () => {
        profileDropdown.style.display = profileDropdown.style.display === "block" ? "none" : "block";
    });

    document.addEventListener("click", (e) => {
        if (!profileWrapper.contains(e.target)) {
            profileDropdown.style.display = "none";
        }
    });

    // 우측 아이콘 영역 정렬
    const rightContainer = document.createElement("div");
    rightContainer.style.display = "flex";
    rightContainer.style.alignItems = "center";
    rightContainer.style.gap = "16px";
    rightContainer.style.position = "relative";
    rightContainer.appendChild(alarmIcon);
    rightContainer.appendChild(chatIcon);
    rightContainer.appendChild(profileWrapper);

    // 헤더에 구성 요소 추가
    header.appendChild(logo);
    header.appendChild(searchContainer);
    header.appendChild(rightContainer);
    document.body.prepend(header);
});


// 알림/채팅 데이터 연결용 함수

function fetchAlarmList() {
    // TODO: 백엔드 연동 후 알림 목록 가져오는 로직 작성
    return Promise.resolve([]);
}

function fetchChatList() {
    // TODO: 백엔드 연동 후 채팅 목록 가져오는 로직 작성
    return Promise.resolve([]);
}

function renderAlarmDropdown(alarms) {
    // TODO: 받아온 알림 데이터를 드롭다운 내부에 렌더링
}

function renderChatDropdown(chats) {
    // TODO: 받아온 채팅 데이터를 드롭다운 내부에 렌더링
}