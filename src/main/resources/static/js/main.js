/**
 * 메인 페이지 - 모임 목록 및 관리
 * - 내 모임, 온라인, 오프라인 모임 탭 관리
 * - 모임 목록 조회 및 렌더링
 * - 모임 참여 요청 처리
 * - 위치 기반 오프라인 모임 표시
 * - 정렬 기능 (인기순, 최신순)
 */

// DOM 요소 참조
const tabs = document.querySelectorAll(".tab")
const container = document.getElementById("groupListContainer")
const locationDiv = document.getElementById("user-location")

// 모임 타입 매핑
const groupTypeMap = {
    ONLINE: "온라인",
    OFFLINE: "오프라인",
    HYBRID: "온·오프라인",
}

// 모임 관심사 매핑
const groupInterestMap = {
    HOBBY: "취미",
    FAMILY: "가족",
    SCHOOL: "학교",
    BUSINESS: "업무",
    EXERCISE: "운동",
    GAME: "게임",
    STUDY: "스터디",
    FAN: "팬",
    OTHER: "기타",
}

// 현재 정렬 방식
let currentSort = "popular"

// 주소에서 동 이름 추출
function extractDong(address) {
    if (!address) return ""
    const parts = address.split(" ")
    return parts.length > 0 ? parts[parts.length - 1] : address
}

// 사용자 위치 정보 업데이트
function updateUserLocation(tabType) {
    const sortOptions = document.querySelector(".sort-options")

    // 정렬 드롭다운 표시 (온라인, 오프라인 탭에서만)
    sortOptions.style.display = tabType === "online" || tabType === "offline" ? "block" : "none"

    // 오프라인 탭에서 사용자 위치 표시
    if (tabType === "offline" && window.loggedInUser?.address) {
        const dong = extractDong(window.loggedInUser.address)
        locationDiv.textContent = `📍${dong}`
        locationDiv.style.display = "block"
    } else {
        locationDiv.style.display = "none"
    }
}

// 모임 목록 조회
async function fetchGroups(tabType) {
    let url = ""
    let includeCreate = false

    // 탭 타입에 따른 API URL 설정
    if (tabType === "my") {
        url = `/api/groups/my`
        includeCreate = true
    } else if (tabType === "online") {
        url = `/api/groups/on?sort=${currentSort}`
    } else if (tabType === "offline") {
        url = `/api/groups/off?sort=${currentSort}`
    }

    try {
        const res = await fetchWithAuth(url)
        const data = await res.json()
        renderGroups(data.result.content, includeCreate, tabType)
    } catch (err) {
        console.error("모임 불러오기 실패", err)
        showErrorMessage("모임을 불러오는데 실패했습니다.")
    }
}

// 모임 참여 요청 처리
async function handleJoinRequest(groupId, joinBtn, groupName) {
    try {
        const res = await fetchWithAuth(`/api/groups/${groupId}/members/requests`, {
            method: "POST",
        })
        const data = await res.json()

        if (res.ok && data.result) {
            alert("참여 요청이 완료되었습니다.")
            joinBtn.disabled = true
            joinBtn.textContent = "가입 요청 중"
        } else {
            alert(data.message || "참여 요청 실패")
        }
    } catch (err) {
        console.error("참여 요청 실패", err)
        alert("참여 요청 중 오류가 발생했습니다.")
    }
}

// 참여 버튼 생성
function createJoinButton(group) {
    const joinBtn = document.createElement("button")
    joinBtn.style.cssText = `
        margin-top: 8px;
        padding: 6px 12px;
        border: none;
        border-radius: 4px;
        background-color: #4CAF50;
        color: #fff;
        cursor: pointer;
    `

    // 가입 상태에 따른 버튼 텍스트 설정
    joinBtn.textContent = group.joinStatus === "REQUESTED" ? "가입 요청 중" : "참여하기"

    // 참여 버튼 클릭 이벤트
    joinBtn.addEventListener("click", async (e) => {
        e.stopPropagation()

        if (group.joinStatus === "BLOCKED") {
            alert("가입 요청할 수 없는 모임입니다.")
            return
        }

        if (group.joinStatus === "REQUESTED") {
            alert("이미 가입 요청 중인 모임입니다.")
            return
        }

        await handleJoinRequest(group.groupId, joinBtn, group.groupName)
    })

    return joinBtn
}

// 모임 카드 생성
function createGroupCard(group) {
    const card = document.createElement("div")
    card.className = "group-card"

    // 모임 이미지
    const img = document.createElement("img")
    img.src = group.groupCoverImageUrl || "https://via.placeholder.com/300x214?text=No+Image"
    card.appendChild(img)

    // 모임 정보
    const info = document.createElement("div")
    info.className = "group-info"

    // 모임 이름
    const name = document.createElement("h3")
    name.textContent = group.groupName
    info.appendChild(name)

    // 모임 메타 정보
    const meta = document.createElement("div")
    meta.className = "group-meta"
    meta.innerHTML = `
        ${groupTypeMap[group.groupType] || group.groupType} /
        ${groupInterestMap[group.groupInterest] || group.groupInterest} ·
        멤버 ${group.memberCount}명
    `
    info.appendChild(meta)

    // 가입 상태에 따른 처리
    if (group.joinStatus !== "APPROVED") {
        // 참여 버튼 추가
        const joinBtn = createJoinButton(group)
        info.appendChild(joinBtn)
    } else {
        // 승인된 멤버는 클릭 시 모임으로 이동
        card.addEventListener("click", () => {
            window.location.href = `/test/group/main.html?id=${group.groupId}`
        })
    }

    card.appendChild(info)
    return card
}

// 모임 생성 카드 생성
function createCreateCard() {
    const createCard = document.createElement("div")
    createCard.className = "create-card"
    createCard.innerHTML = `
        <div class="plus-icon">＋</div>
        <div>만들기</div>
    `
    createCard.addEventListener("click", () => {
        window.location.href = "/test/group/create.html"
    })
    return createCard
}

// 모임 목록 렌더링
function renderGroups(groups, includeCreate, tabType) {
    container.innerHTML = ""

    // 모임 생성 카드 추가 (내 모임 탭에서만)
    if (includeCreate) {
        const createCard = createCreateCard()
        container.appendChild(createCard)
    }

    // 모임 카드들 생성 및 추가
    groups.forEach((group) => {
        const card = createGroupCard(group)
        container.appendChild(card)
    })
}

// 에러 메시지 표시
function showErrorMessage(message) {
    container.innerHTML = `<div class="error-message">${message}</div>`
}

// 탭 변경 처리
function handleTabChange(selectedTab) {
    // 정렬 초기화
    if (selectedTab === "online" || selectedTab === "offline") {
        currentSort = "popular"
        document.getElementById("sortSelect").value = "popular"
    }

    // 모임 목록 조회 및 UI 업데이트
    fetchGroups(selectedTab)
    updateUserLocation(selectedTab)
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 탭 클릭 이벤트
    tabs.forEach((tab) => {
        tab.addEventListener("click", () => {
            tabs.forEach((t) => t.classList.remove("active"))
            tab.classList.add("active")
            const selectedTab = tab.dataset.tab
            handleTabChange(selectedTab)
        })
    })

    // 정렬 변경 이벤트
    document.getElementById("sortSelect").addEventListener("change", (e) => {
        currentSort = e.target.value
        const activeTab = document.querySelector(".tab.active").dataset.tab
        fetchGroups(activeTab)
    })
}

// 페이지 초기화
document.addEventListener("DOMContentLoaded", () => {
    setupEventListeners()
    fetchGroups("my")
    updateUserLocation("my")
})
