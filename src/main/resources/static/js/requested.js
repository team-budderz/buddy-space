/**
 * 가입 요청한 모임 조회 페이지
 * - 가입 요청한 목록 조회
 * - 가입 요청 취소 처리
 * - 요청자 프로필 정보 표시
 * - 요청 상태별 필터링
 */

// 전역 변수
let userInfo = null
let requestedGroups = []
let filteredGroups = []

// DOM 요소들
const elements = {
    loading: null,
    requestsContent: null,
    requestsList: null,
    emptyState: null,
    totalRequests: null,
    typeFilter: null,
    interestFilter: null,
    sortSelect: null,
    cancelModal: null,
    toast: null,
}

// 초기화
document.addEventListener("DOMContentLoaded", async () => {
    initializeElements()
    setupEventListeners()
    await fetchUserInfo()
    await fetchRequestedGroups()
})

// DOM 요소 초기화
function initializeElements() {
    elements.loading = document.getElementById("loading")
    elements.requestsContent = document.getElementById("requests-content")
    elements.requestsList = document.getElementById("requests-list")
    elements.emptyState = document.getElementById("empty-state")
    elements.totalRequests = document.getElementById("total-requests")
    elements.typeFilter = document.getElementById("type-filter")
    elements.interestFilter = document.getElementById("interest-filter")
    elements.sortSelect = document.getElementById("sort-select")
    elements.cancelModal = document.getElementById("cancel-modal")
    elements.toast = document.getElementById("toast")
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 필터 및 정렬
    elements.typeFilter.addEventListener("change", applyFilters)
    elements.interestFilter.addEventListener("change", applyFilters)
    elements.sortSelect.addEventListener("change", applyFilters)

    // 모달 관련
    document.getElementById("cancel-request-cancel").addEventListener("click", () => hideModal("cancel"))
    document.getElementById("cancel-request-confirm").addEventListener("click", confirmCancelRequest)

    // 모달 닫기
    document.querySelectorAll(".modal-close").forEach((btn) => {
        btn.addEventListener("click", (e) => {
            const modal = e.target.closest(".modal")
            if (modal.id === "cancel-modal") hideModal("cancel")
        })
    })

    // 모달 외부 클릭시 닫기
    elements.cancelModal.addEventListener("click", (e) => {
        if (e.target === elements.cancelModal) hideModal("cancel")
    })
}

// 사용자 정보 조회
async function fetchUserInfo() {
    try {
        const response = await fetchWithAuth("/api/users/me")
        const data = await response.json()

        if (response.ok) {
            userInfo = data.result
            displayUserInfo()
        } else {
            console.warn("사용자 정보 조회 실패:", data.message || data.code)
        }
    } catch (error) {
        console.error("사용자 정보 요청 중 오류:", error)
    }
}

// 사용자 정보 표시
function displayUserInfo() {
    if (!userInfo) return

    // 사이드바 정보
    updateElement("sidebar-name", userInfo.name)
    updateElement("sidebar-email", userInfo.email)
    updateProfileImage("sidebar-avatar", "sidebar-fallback", userInfo.profileImageUrl, userInfo.name)
}

// 요소 업데이트 헬퍼
function updateElement(id, value) {
    const element = document.getElementById(id)
    if (element) element.textContent = value
}

// 프로필 이미지 업데이트
function updateProfileImage(imgId, fallbackId, imageUrl, name) {
    const img = document.getElementById(imgId)
    const fallback = document.getElementById(fallbackId)

    if (imageUrl) {
        img.src = imageUrl
        img.style.display = "block"
        fallback.style.display = "none"
    } else {
        img.style.display = "none"
        fallback.style.display = "flex"
        fallback.textContent = name ? name.charAt(0).toUpperCase() : "U"
    }
}

// 가입 요청 목록 조회
async function fetchRequestedGroups() {
    try {
        elements.loading.style.display = "flex"

        const response = await fetchWithAuth("/api/groups/my-requested")
        const data = await response.json()

        if (response.ok) {
            requestedGroups = data.result || []
            filteredGroups = [...requestedGroups]
            displayRequestedGroups()
        } else {
            showToast("가입 요청 목록을 불러올 수 없습니다.", "error")
        }
    } catch (error) {
        console.error("Error fetching requested groups:", error)
        showToast("네트워크 오류가 발생했습니다.", "error")
    } finally {
        elements.loading.style.display = "none"
        elements.requestsContent.style.display = "block"
    }
}

// 가입 요청 목록 표시
function displayRequestedGroups() {
    updateRequestStats()

    if (filteredGroups.length === 0) {
        elements.requestsList.style.display = "none"
        elements.emptyState.style.display = "block"
        return
    }

    elements.requestsList.style.display = "block"
    elements.emptyState.style.display = "none"

    elements.requestsList.innerHTML = filteredGroups.map((group) => createRequestCard(group)).join("")

    // 취소 버튼 이벤트 리스너 추가
    document.querySelectorAll(".cancel-request-btn").forEach((btn) => {
        btn.addEventListener("click", (e) => {
            const groupId = e.target.dataset.groupId
            const group = filteredGroups.find((g) => g.id.toString() === groupId)
            showCancelModal(group)
        })
    })
}

// 요청 카드 생성
function createRequestCard(group) {
    const typeText = getGroupTypeText(group.type)
    const typeClass = getGroupTypeClass(group.type)
    const interestText = getGroupInterestText(group.interest)
    const interestClass = getGroupInterestClass(group.interest)
    const accessText = group.access === "PUBLIC" ? "공개" : "비공개"
    const accessClass = group.access === "PUBLIC" ? "" : "private"

    return `
    <div class="request-card">
      <div class="request-card-content">
        ${
        group.coverImageUrl
            ? `<img src="${group.coverImageUrl}" alt="${group.name}" class="group-image">`
            : `<div class="group-image-fallback">${group.name.charAt(0).toUpperCase()}</div>`
    }
        
        <div class="group-info">
          <div class="group-header">
            <h3 class="group-name">${group.name}</h3>
            <div class="group-badges">
              <span class="group-badge badge-type ${typeClass}">${typeText}</span>
              <span class="group-badge badge-interest ${interestClass}">${interestText}</span>
              <span class="group-badge badge-access ${accessClass}">${accessText}</span>
            </div>
          </div>
          
          <p class="group-description">${group.description || "설명이 없습니다."}</p>
          
          ${group.address ? `<p class="group-address">📍 ${group.address}</p>` : ""}
        </div>
        
        <div class="request-actions">
          <span class="request-status">승인 대기중</span>
          <button class="cancel-request-btn" data-group-id="${group.id}">
            요청 취소
          </button>
        </div>
      </div>
    </div>
  `
}

// 모임 유형 텍스트 변환 (온라인/오프라인)
function getGroupTypeText(type) {
    const types = {
        ONLINE: "온라인",
        OFFLINE: "오프라인",
        HYBRID: "온·오프라인",
    }
    return types[type] || "오프라인"
}

// 모임 유형 클래스 변환
function getGroupTypeClass(type) {
    const classes = {
        ONLINE: "online",
        OFFLINE: "offline",
        HYBRID: "hybrid",
    }
    return classes[type] || "offline"
}

// 모임 관심사 텍스트 변환
function getGroupInterestText(interest) {
    const interests = {
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
    return interests[interest] || "기타"
}

// 모임 관심사 클래스 변환
function getGroupInterestClass(interest) {
    const classes = {
        HOBBY: "hobby",
        FAMILY: "family",
        SCHOOL: "school",
        BUSINESS: "business",
        EXERCISE: "exercise",
        GAME: "game",
        STUDY: "study",
        FAN: "fan",
        OTHER: "other",
    }
    return classes[interest] || "other"
}

// 요청 통계 업데이트
function updateRequestStats() {
    const total = filteredGroups.length
    elements.totalRequests.textContent = `${total}개 요청`
}

// 필터 및 정렬 적용
function applyFilters() {
    const typeFilter = elements.typeFilter.value
    const interestFilter = elements.interestFilter.value
    const sortBy = elements.sortSelect.value

    // 필터링
    filteredGroups = requestedGroups.filter((group) => {
        const typeMatch = !typeFilter || group.type === typeFilter
        const interestMatch = !interestFilter || group.interest === interestFilter
        return typeMatch && interestMatch && accessMatch
    })

    // 정렬
    filteredGroups.sort((a, b) => {
        switch (sortBy) {
            case "name":
                return a.name.localeCompare(b.name)
            case "type":
                return a.type.localeCompare(b.type)
            case "interest":
                return a.interest.localeCompare(b.interest)
            case "recent":
            default:
                return b.id - a.id // ID 기준 내림차순 (최근 순)
        }
    })

    displayRequestedGroups()
}

// 취소 모달 표시
function showCancelModal(group) {
    document.getElementById("cancel-group-name").textContent = group.name
    document.getElementById("cancel-group-description").textContent = group.description || "설명이 없습니다."

    const cancelGroupImage = document.getElementById("cancel-group-image")
    if (group.coverImageUrl) {
        cancelGroupImage.src = group.coverImageUrl
    } else {
        cancelGroupImage.src = "/placeholder.svg?height=60&width=60"
    }

    elements.cancelModal.dataset.groupId = group.id
    elements.cancelModal.classList.add("show")
}

// 요청 취소 확인
async function confirmCancelRequest() {
    const groupId = elements.cancelModal.dataset.groupId

    try {
        const response = await fetchWithAuth(`/api/groups/${groupId}/cancel-requests`, {
            method: "DELETE",
        })

        if (response.ok) {
            // API 호출 성공 시에만 클라이언트에서 제거
            requestedGroups = requestedGroups.filter((group) => group.id.toString() !== groupId)
            applyFilters()

            hideModal("cancel")
            showToast("가입 요청이 취소되었습니다.")
        } else {
            // API 응답이 실패인 경우
            const errorData = await response.json()
            const errorMessage = errorData.message || "요청 취소에 실패했습니다."
            showToast(errorMessage, "error")
        }
    } catch (error) {
        console.error("Error canceling request:", error)
        showToast("네트워크 오류가 발생했습니다.", "error")
    }
}

// 모달 숨기기
function hideModal(type) {
    if (type === "cancel") {
        elements.cancelModal.classList.remove("show")
    }
}

// 토스트 알림
function showToast(message, type = "success") {
    elements.toast.textContent = message
    elements.toast.className = `toast ${type}`
    elements.toast.classList.add("show")

    setTimeout(() => {
        elements.toast.classList.remove("show")
    }, 3000)
}
