// 토큰 없으면 로그인 페이지로 이동
function redirectToLogin() {
    window.location.href = "/test/login.html";
}

// accessToken 확인 및 리턴
function getAccessTokenOrRedirect() {
    const token = localStorage.getItem("accessToken");
    if (!token) redirectToLogin();
    return token;
}

// 토큰 자동 재발급 포함 fetch wrapper
async function fetchWithAuth(url, options = {}) {
    const token = getAccessTokenOrRedirect();
    options.headers = {
        ...(options.headers || {}),
        Authorization: `Bearer ${token}`
    };
    options.credentials = "include";

    let response = await fetch(`${API_BASE_URL}${url}`, options);

    if (response.status === 401) {
        const refreshRes = await fetch(`${API_BASE_URL}/api/token/refresh`, {
            method: "POST",
            credentials: "include"
        });

        const refreshData = await refreshRes.json();

        if (refreshRes.ok && refreshData.result?.accessToken) {
            const newToken = refreshData.result.accessToken;
            localStorage.setItem("accessToken", newToken);
            options.headers.Authorization = `Bearer ${newToken}`;
            response = await fetch(`${API_BASE_URL}${url}`, options);
        } else {
            alert("로그인이 만료되었습니다. 다시 로그인해주세요.");
            redirectToLogin();
            return;
        }
    }

    return response;
}

// 로그아웃
function logoutUser() {
    localStorage.removeItem("accessToken");
    window.location.href = "/test/login.html";
}

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

// 헤더 렌더링
document.addEventListener("DOMContentLoaded", async () => {
    getAccessTokenOrRedirect();
    window.loggedInUser = null;
    let profileImageUrl = "https://raw.githubusercontent.com/withong/my-storage/main/budderz/default.png";

    try {
        const response = await fetchWithAuth("/api/users/me");
        const data = await response.json();
        if (response.ok) {
            profileImageUrl = data.result.profileImageUrl || profileImageUrl;
            window.loggedInUser = data.result;
        } else {
            console.warn("사용자 정보 조회 실패:", data.message || data.code);
            redirectToLogin();
        }
    } catch (err) {
        console.error("사용자 정보 요청 중 오류:", err);
    }

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

    const logo = document.createElement("a");
    logo.href = "/test/main.html";
    const logoImg = document.createElement("img");
    logoImg.src = "https://raw.githubusercontent.com/withong/my-storage/main/budderz/free-icon-wing-12298568.png";
    logoImg.alt = "벗터 로고";
    logoImg.style.height = "32px";
    logoImg.style.cursor = "pointer";
    logo.appendChild(logoImg);

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

    // 알림
    const alarmIcon = createIconWithDropdown(
        "https://raw.githubusercontent.com/withong/my-storage/main/budderz/free-icon-notification-bell-8377307.png",
        "알림",
        `<div style="padding: 10px">알림이 없습니다</div>`
    );

    // 채팅
    const chatIcon = createIconWithDropdown(
        "https://raw.githubusercontent.com/withong/my-storage/main/budderz/free-icon-conversation-5323491.png",
        "채팅",
        `<div style="padding: 10px">채팅 내역이 없습니다</div>`
    );

    // 프로필
    const profileDropdownContent = `
        <div style="padding: 10px 12px; cursor: pointer; color: #333;" onclick="window.location.href='/test/user/my-page.html'">
            내 정보 조회
        </div>
        <div style="padding: 10px 12px; cursor: pointer; color: #333; border-top: 1px solid #eee;" onclick="logoutUser()">
            로그아웃
        </div>
    `;


    const profileWrapper = createIconWithDropdown(
        profileImageUrl,
        "프로필",
        profileDropdownContent
    );
    const profileImg = profileWrapper.querySelector("img");
    profileImg.style.width = "36px";
    profileImg.style.height = "36px";
    profileImg.style.borderRadius = "50%";

    const rightContainer = document.createElement("div");
    rightContainer.style.display = "flex";
    rightContainer.style.alignItems = "center";
    rightContainer.style.gap = "16px";
    rightContainer.style.position = "relative";
    rightContainer.appendChild(alarmIcon);
    rightContainer.appendChild(chatIcon);
    rightContainer.appendChild(profileWrapper);

    header.appendChild(logo);
    header.appendChild(searchContainer);
    header.appendChild(rightContainer);
    document.body.prepend(header);
});

// 알림/채팅용 placeholder 함수
function fetchAlarmList() {
    return Promise.resolve([]);
}

function fetchChatList() {
    return Promise.resolve([]);
}

function renderAlarmDropdown(alarms) {
}

function renderChatDropdown(chats) {
}
