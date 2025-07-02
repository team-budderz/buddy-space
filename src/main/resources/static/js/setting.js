/**
 * 모임 설정 관리 페이지
 * - 모임 기본 정보 수정
 * - 커버 이미지 변경
 * - 모임 공개/비공개 설정
 * - 권한 설정 관리
 * - 모임 삭제 기능
 * - 리더 권한 위임
 */

// 전역 변수
let currentGroupData = null
let currentMembers = []
let currentPermissions = []

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", async () => {
    groupId = new URLSearchParams(window.location.search).get("id")

    if (!groupId) {
        showError("모임 ID를 찾을 수 없습니다.")
        return
    }

    await initializeSettings()
    setupEventListeners()
})

// 설정 페이지 초기화
async function initializeSettings() {
    try {
        showLoading(true)

        // 권한 데이터 초기화
        if (typeof GroupPermissions !== "undefined") {
            const permissionsInitialized = await GroupPermissions.initialize(groupId)
            if (!permissionsInitialized) {
                showError("권한 정보를 불러올 수 없습니다.")
                return
            }

            // 리더가 아니면 설정 페이지 접근 불가
            if (!GroupPermissions.isLeader()) {
                showError("설정 페이지는 리더만 접근할 수 있습니다.")
                return
            }
        }

        // 모임 정보 로드
        await loadGroupData()

        // UI 업데이트
        updateCurrentInfo()
    } catch (error) {
        console.error("설정 초기화 실패:", error)
        showError("설정 정보를 불러오는데 실패했습니다.")
    } finally {
        showLoading(false)
    }
}

// 모임 데이터 로드
async function loadGroupData() {
    try {
        const response = await fetchWithAuth(`/api/groups/${groupId}`)
        const data = await response.json()

        if (response.ok && data.result) {
            currentGroupData = data.result
        } else {
            throw new Error(data.message || "모임 정보 조회 실패")
        }
    } catch (error) {
        console.error("모임 데이터 로드 실패:", error)
        throw error
    }
}

// 현재 정보 업데이트
function updateCurrentInfo() {
    if (!currentGroupData) return

    // 공개 타입
    const accessText = currentGroupData.access === "PUBLIC" ? "공개" : "비공개"
    document.getElementById("current-access").textContent = accessText

    // 모임 유형
    const typeMap = {
        ONLINE: "온라인",
        OFFLINE: "오프라인",
        HYBRID: "온·오프라인",
    }
    document.getElementById("current-type").textContent = typeMap[currentGroupData.type] || "온라인"

    // 관심사
    const interestMap = {
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
    document.getElementById("current-interest").textContent = interestMap[currentGroupData.interest] || "취미"

    // 오프라인 모임 관련 요소 표시/숨김
    const offlineElements = document.querySelectorAll(".offline-only")
    const isOffline = currentGroupData.type === "OFFLINE" || currentGroupData.type === "HYBRID"

    offlineElements.forEach((element) => {
        element.style.display = isOffline ? "flex" : "none"
    })

    // 동네 인증 토글 상태
    if (isOffline) {
        const toggle = document.getElementById("neighborhood-auth-toggle")
        if (toggle) {
            toggle.checked = currentGroupData.isNeighborhoodAuthRequired || false
        }

        // 현재 주소 표시
        const addressElement = document.getElementById("current-address")
        if (addressElement) {
            addressElement.textContent = currentGroupData.address || "현재 동네 정보 없음"
        }
    }
}

// 이벤트 리스너 설정 - 중복 등록 방지
function setupEventListeners() {
    // 기존 이벤트 리스너 제거 후 새로 등록
    const neighborhoodToggle = document.getElementById("neighborhood-auth-toggle")
    if (neighborhoodToggle) {
        // 기존 이벤트 리스너 제거
        neighborhoodToggle.removeEventListener("change", handleNeighborhoodAuthToggle)
        // 새로운 이벤트 리스너 등록
        neighborhoodToggle.addEventListener("change", handleNeighborhoodAuthToggle)
    }

    // 모달 닫기 이벤트
    document.addEventListener("click", (e) => {
        if (e.target.classList.contains("settings-modal-close")) {
            const modal = e.target.closest(".settings-modal")
            if (modal) {
                closeModal(modal.id)
            }
        }

        if (e.target.classList.contains("settings-modal")) {
            closeModal(e.target.id)
        }
    })

    // ESC 키로 모달 닫기
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            const openModal = document.querySelector('.settings-modal[style*="block"]')
            if (openModal) {
                closeModal(openModal.id)
            }
        }
    })

    // 커버 이미지 업로드
    const coverImageInput = document.getElementById("cover-image")
    if (coverImageInput) {
        coverImageInput.removeEventListener("change", handleCoverImageChange)
        coverImageInput.addEventListener("change", handleCoverImageChange)
    }

    // 입력 필드 글자 수 카운터
    const groupNameInput = document.getElementById("group-name")
    if (groupNameInput) {
        groupNameInput.removeEventListener("input", updateCharCount)
        groupNameInput.addEventListener("input", updateCharCount)
    }
}

// 모달 열기/닫기
function openModal(modalId) {
    const modal = document.getElementById(modalId)
    if (modal) {
        modal.style.display = "block"
        document.body.style.overflow = "hidden"
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId)
    if (modal) {
        modal.style.display = "none"
        document.body.style.overflow = "auto"
    }
}

// 로딩 표시
function showLoading(show) {
    const overlay = document.getElementById("loading-overlay")
    if (overlay) {
        overlay.style.display = show ? "flex" : "none"
    }
}

// 에러 표시
function showError(message) {
    alert(message) // 추후 더 나은 에러 표시 방식으로 개선 가능
}

// 성공 메시지 표시
function showSuccess(message) {
    alert(message) // 추후 더 나은 성공 메시지 표시 방식으로 개선 가능
}

// 모임 정보 수정 모달 열기
function openGroupInfoModal() {
    if (!currentGroupData) return

    // 현재 데이터로 폼 채우기
    document.getElementById("group-name").value = currentGroupData.name || ""
    document.getElementById("cover-preview").src =
        currentGroupData.coverImageUrl || "/placeholder.svg?height=200&width=400"

    updateCharCount()
    openModal("group-info-modal")
}

// 커버 이미지 제거 함수 추가
function removeCoverImage() {
    const preview = document.getElementById("cover-preview")
    const fileInput = document.getElementById("cover-image")

    // 미리보기를 기본 이미지로 변경
    preview.src = "/placeholder.svg?height=200&width=400"

    // 파일 입력 초기화
    fileInput.value = ""
}

// 모임 정보 업데이트
async function updateGroupInfo() {
    try {
        showLoading(true)

        const name = document.getElementById("group-name").value.trim()
        const coverImageFile = document.getElementById("cover-image").files[0]

        if (!name) {
            showError("벗터 이름을 입력해주세요.")
            return
        }

        const formData = new FormData()

        const requestData = {
            name: name,
            description: currentGroupData.description,
            access: currentGroupData.access,
            type: currentGroupData.type,
            interest: currentGroupData.interest,
        }

        // 커버 이미지 처리 로직 개선
        if (coverImageFile) {
            // 새 이미지로 변경
            formData.append("coverImage", coverImageFile)
            requestData.coverAttachmentId = null
        } else {
            // 기존 이미지 유지 또는 기본 이미지로 변경
            // 현재 미리보기가 기본 placeholder인지 확인
            const preview = document.getElementById("cover-preview")
            const isDefaultImage = preview.src.includes("placeholder.svg") || preview.src.includes("#")

            if (isDefaultImage) {
                // 기본 이미지로 변경 (이미지 제거)
                requestData.coverAttachmentId = null
            } else {
                // 기존 이미지 유지
                requestData.coverAttachmentId = currentGroupData.coverAttachmentId
            }
        }

        formData.append(
            "request",
            new Blob([JSON.stringify(requestData)], {
                type: "application/json",
            }),
        )

        const response = await fetchWithAuth(`/api/groups/${groupId}`, {
            method: "PUT",
            body: formData,
        })

        const data = await response.json()

        if (response.ok && data.result) {
            currentGroupData = data.result
            updateCurrentInfo()
            closeModal("group-info-modal")
            showSuccess("벗터 정보가 성공적으로 업데이트되었습니다.")
        } else {
            throw new Error(data.message || "업데이트 실패")
        }
    } catch (error) {
        console.error("모임 정보 업데이트 실패:", error)
        showError(error.message || "모임 정보 업데이트 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 소개 모달 열기
function openDescriptionModal() {
    const modal = document.getElementById("dynamic-modal")
    document.getElementById("modal-title").textContent = "벗터 소개"

    document.getElementById("modal-body").innerHTML = `
    <div class="settings-form-group">
      <label for="group-description">벗터 소개</label>
      <textarea id="group-description" maxlength="200" placeholder="벗터를 소개해주세요">${currentGroupData?.description || ""}</textarea>
      <small class="settings-char-count">0/200</small>
    </div>
  `

    document.getElementById("modal-footer").innerHTML = `
    <button type="button" class="settings-btn settings-btn-secondary" onclick="closeModal('dynamic-modal')">취소</button>
    <button type="button" class="settings-btn settings-btn-primary" onclick="updateDescription()">저장</button>
  `

    // 글자 수 카운터 설정
    const textarea = document.getElementById("group-description")
    textarea.addEventListener("input", () => {
        const count = textarea.value.length
        textarea.nextElementSibling.textContent = `${count}/200`
    })
    textarea.dispatchEvent(new Event("input"))

    openModal("dynamic-modal")
}

// 소개 업데이트
async function updateDescription() {
    try {
        showLoading(true)

        const description = document.getElementById("group-description").value.trim()

        const requestData = {
            name: currentGroupData.name,
            description: description,
            access: currentGroupData.access,
            type: currentGroupData.type,
            interest: currentGroupData.interest,
        }

        const formData = new FormData()
        formData.append(
            "request",
            new Blob([JSON.stringify(requestData)], {
                type: "application/json",
            }),
        )

        const response = await fetchWithAuth(`/api/groups/${groupId}`, {
            method: "PUT",
            body: formData,
        })

        const data = await response.json()

        if (response.ok && data.result) {
            currentGroupData = data.result
            closeModal("dynamic-modal")
            showSuccess("벗터 소개가 성공적으로 업데이트되었습니다.")
        } else {
            throw new Error(data.message || "업데이트 실패")
        }
    } catch (error) {
        console.error("소개 업데이트 실패:", error)
        showError(error.message || "소개 업데이트 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 공개 타입 모달 열기 - 생성 화면 스타일로 개선
function openAccessModal() {
    const modal = document.getElementById("dynamic-modal")
    document.getElementById("modal-title").textContent = "벗터 공개 타입"

    document.getElementById("modal-body").innerHTML = `
    <div class="settings-form-group">
      <div class="settings-section-header">
        <p>모임의 공개 범위를 선택해주세요</p>
      </div>
      <div class="settings-radio-group">
        <label class="settings-radio-card" for="access-public">
          <input type="radio" id="access-public" name="access" value="PUBLIC" ${currentGroupData?.access === "PUBLIC" ? "checked" : ""}>
          <div class="settings-radio-content">
            <div class="settings-radio-icon">🌍</div>
            <div class="settings-radio-info">
              <div class="settings-radio-title">공개</div>
              <div class="settings-radio-desc">누구나 벗터를 찾고 가입 요청할 수 있습니다</div>
            </div>
          </div>
        </label>
        <label class="settings-radio-card" for="access-private">
          <input type="radio" id="access-private" name="access" value="PRIVATE" ${currentGroupData?.access === "PRIVATE" ? "checked" : ""}>
          <div class="settings-radio-content">
            <div class="settings-radio-icon">🔒</div>
            <div class="settings-radio-info">
              <div class="settings-radio-title">비공개</div>
              <div class="settings-radio-desc">초대를 통해서만 가입할 수 있습니다</div>
            </div>
          </div>
        </label>
      </div>
    </div>
  `

    document.getElementById("modal-footer").innerHTML = `
    <button type="button" class="settings-btn settings-btn-secondary" onclick="closeModal('dynamic-modal')">취소</button>
    <button type="button" class="settings-btn settings-btn-primary" onclick="updateAccess()">저장</button>
  `

    openModal("dynamic-modal")
}

// 공개 타입 업데이트
async function updateAccess() {
    try {
        showLoading(true)

        const selectedAccess = document.querySelector('input[name="access"]:checked')?.value
        if (!selectedAccess) {
            showError("공개 설정을 선택해주세요.")
            return
        }

        const requestData = {
            name: currentGroupData.name,
            description: currentGroupData.description,
            access: selectedAccess,
            type: currentGroupData.type,
            interest: currentGroupData.interest,
        }

        const formData = new FormData()
        formData.append(
            "request",
            new Blob([JSON.stringify(requestData)], {
                type: "application/json",
            }),
        )

        const response = await fetchWithAuth(`/api/groups/${groupId}`, {
            method: "PUT",
            body: formData,
        })

        const data = await response.json()

        if (response.ok && data.result) {
            currentGroupData = data.result
            updateCurrentInfo()
            closeModal("dynamic-modal")
            showSuccess("공개 설정이 성공적으로 업데이트되었습니다.")
        } else {
            throw new Error(data.message || "업데이트 실패")
        }
    } catch (error) {
        console.error("공개 설정 업데이트 실패:", error)
        showError(error.message || "공개 설정 업데이트 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 유형 모달 열기 - 생성 화면 스타일로 개선
function openTypeModal() {
    const modal = document.getElementById("dynamic-modal")
    document.getElementById("modal-title").textContent = "벗터 유형"

    document.getElementById("modal-body").innerHTML = `
    <div class="settings-form-group">
      <div class="settings-section-header">
        <p>어떤 방식으로 모임을 진행하시나요?</p>
      </div>
      <div class="settings-radio-group">
        <label class="settings-radio-card" for="type-online">
          <input type="radio" id="type-online" name="type" value="ONLINE" ${currentGroupData?.type === "ONLINE" ? "checked" : ""}>
          <div class="settings-radio-content">
            <div class="settings-radio-icon">💻</div>
            <div class="settings-radio-info">
              <div class="settings-radio-title">온라인</div>
              <div class="settings-radio-desc">채팅으로 만나요</div>
            </div>
          </div>
        </label>
        <label class="settings-radio-card" for="type-offline">
          <input type="radio" id="type-offline" name="type" value="OFFLINE" ${currentGroupData?.type === "OFFLINE" ? "checked" : ""}>
          <div class="settings-radio-content">
            <div class="settings-radio-icon">🏢</div>
            <div class="settings-radio-info">
              <div class="settings-radio-title">오프라인</div>
              <div class="settings-radio-desc">동네에서 만나요</div>
            </div>
          </div>
        </label>
        <label class="settings-radio-card" for="type-hybrid">
          <input type="radio" id="type-hybrid" name="type" value="HYBRID" ${currentGroupData?.type === "HYBRID" ? "checked" : ""}>
          <div class="settings-radio-content">
            <div class="settings-radio-icon">🔄</div>
            <div class="settings-radio-info">
              <div class="settings-radio-title">온·오프라인</div>
              <div class="settings-radio-desc">상황에 따라 유연하게 진행해요</div>
            </div>
          </div>
        </label>
      </div>
    </div>
  `

    document.getElementById("modal-footer").innerHTML = `
    <button type="button" class="settings-btn settings-btn-secondary" onclick="closeModal('dynamic-modal')">취소</button>
    <button type="button" class="settings-btn settings-btn-primary" onclick="updateType()">저장</button>
  `

    openModal("dynamic-modal")
}

// 유형 업데이트
async function updateType() {
    try {
        showLoading(true)

        const selectedType = document.querySelector('input[name="type"]:checked')?.value
        if (!selectedType) {
            showError("모임 유형을 선택해주세요.")
            return
        }

        const requestData = {
            name: currentGroupData.name,
            description: currentGroupData.description,
            access: currentGroupData.access,
            type: selectedType,
            interest: currentGroupData.interest,
        }

        const formData = new FormData()
        formData.append(
            "request",
            new Blob([JSON.stringify(requestData)], {
                type: "application/json",
            }),
        )

        const response = await fetchWithAuth(`/api/groups/${groupId}`, {
            method: "PUT",
            body: formData,
        })

        const data = await response.json()

        if (response.ok && data.result) {
            currentGroupData = data.result
            updateCurrentInfo()
            closeModal("dynamic-modal")
            showSuccess("모임 유형이 성공적으로 업데이트되었습니다.")
        } else {
            throw new Error(data.message || "업데이트 실패")
        }
    } catch (error) {
        console.error("모임 유형 업데이트 실패:", error)
        showError(error.message || "모임 유형 업데이트 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 관심사 모달 열기 - 생성 화면 스타일로 개선
function openInterestModal() {
    const modal = document.getElementById("dynamic-modal")
    document.getElementById("modal-title").textContent = "벗터 관심사"

    const interests = [
        {value: "HOBBY", label: "취미", icon: "🎨"},
        {value: "FAMILY", label: "가족", icon: "👨‍👩‍👧‍👦"},
        {value: "SCHOOL", label: "학교", icon: "🎓"},
        {value: "BUSINESS", label: "업무", icon: "💼"},
        {value: "EXERCISE", label: "운동", icon: "💪"},
        {value: "GAME", label: "게임", icon: "🎮"},
        {value: "STUDY", label: "스터디", icon: "📚"},
        {value: "FAN", label: "팬", icon: "⭐"},
        {value: "OTHER", label: "기타", icon: "🌟"},
    ]

    const interestCards = interests
        .map(
            (interest) => `
    <label class="settings-interest-card" for="interest-${interest.value.toLowerCase()}">
      <input type="radio" id="interest-${interest.value.toLowerCase()}" name="interest" value="${interest.value}" ${currentGroupData?.interest === interest.value ? "checked" : ""}>
      <div class="settings-interest-content">
        <div class="settings-interest-icon">${interest.icon}</div>
        <div class="settings-interest-title">${interest.label}</div>
      </div>
    </label>
  `,
        )
        .join("")

    document.getElementById("modal-body").innerHTML = `
    <div class="settings-form-group">
      <div class="settings-section-header">
        <p>모임의 주요 관심사를 선택해주세요</p>
      </div>
      <div class="settings-interest-grid">
        ${interestCards}
      </div>
    </div>
  `

    document.getElementById("modal-footer").innerHTML = `
    <button type="button" class="settings-btn settings-btn-secondary" onclick="closeModal('dynamic-modal')">취소</button>
    <button type="button" class="settings-btn settings-btn-primary" onclick="updateInterest()">저장</button>
  `

    openModal("dynamic-modal")
}

// 관심사 업데이트
async function updateInterest() {
    try {
        showLoading(true)

        const selectedInterest = document.querySelector('input[name="interest"]:checked')?.value
        if (!selectedInterest) {
            showError("관심사를 선택해주세요.")
            return
        }

        const requestData = {
            name: currentGroupData.name,
            description: currentGroupData.description,
            access: currentGroupData.access,
            type: currentGroupData.type,
            interest: selectedInterest,
        }

        const formData = new FormData()
        formData.append(
            "request",
            new Blob([JSON.stringify(requestData)], {
                type: "application/json",
            }),
        )

        const response = await fetchWithAuth(`/api/groups/${groupId}`, {
            method: "PUT",
            body: formData,
        })

        const data = await response.json()

        if (response.ok && data.result) {
            currentGroupData = data.result
            updateCurrentInfo()
            closeModal("dynamic-modal")
            showSuccess("관심사가 성공적으로 업데이트되었습니다.")
        } else {
            throw new Error(data.message || "업데이트 실패")
        }
    } catch (error) {
        console.error("관심사 업데이트 실패:", error)
        showError(error.message || "관심사 업데이트 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 동네 인증 토글 처리 - 중복 실행 방지 추가
async function handleNeighborhoodAuthToggle(event) {
    // 이미 처리 중인 경우 중복 실행 방지
    if (event.target.dataset.processing === "true") {
        return
    }

    try {
        // 처리 중 플래그 설정
        event.target.dataset.processing = "true"
        showLoading(true)

        const isEnabled = event.target.checked

        const response = await fetchWithAuth(`/api/groups/${groupId}/neighborhood-auth-required`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(isEnabled),
        })

        const data = await response.json()

        if (response.ok && data.result) {
            currentGroupData = data.result
            event.target.checked = isEnabled
            showSuccess(`동네 인증 설정이 ${isEnabled ? "활성화" : "비활성화"}되었습니다.`)
        } else {
            // 실패 시 토글 상태 되돌리기
            event.target.checked = !isEnabled
            throw new Error(data.message || "설정 변경 실패")
        }
    } catch (error) {
        console.error("동네 인증 설정 변경 실패:", error)
        showError(error.message || "동네 인증 설정 변경 중 오류가 발생했습니다.")
    } finally {
        // 처리 완료 후 플래그 제거
        event.target.dataset.processing = "false"
        showLoading(false)
    }
}

// 동네 인증 토글 (클릭 이벤트용) - 중복 호출 방지
function toggleNeighborhoodAuth() {
    const toggle = document.getElementById("neighborhood-auth-toggle")
    if (toggle && toggle.dataset.processing !== "true") {
        toggle.checked = !toggle.checked
        // 프로그래밍 방식으로 change 이벤트 발생시키지 않고 직접 함수 호출
        const event = {target: toggle}
        handleNeighborhoodAuthToggle(event)
    }
}

// 동네 업데이트
async function updateGroupAddress() {
    try {
        if (!confirm("리더의 동네를 기반으로 벗터의 동네를 업데이트하시겠습니까?")) {
            return
        }

        showLoading(true)

        const response = await fetchWithAuth(`/api/groups/${groupId}/address`, {
            method: "PATCH",
        })

        const data = await response.json()

        if (response.ok && data.result) {
            currentGroupData = data.result
            updateCurrentInfo()
            showSuccess("벗터 동네가 성공적으로 업데이트되었습니다.")
        } else {
            throw new Error(data.message || "동네 업데이트 실패")
        }
    } catch (error) {
        console.error("동네 업데이트 실패:", error)
        showError(error.message || "동네 업데이트 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 멤버 권한 설정 모달 열기
async function openMemberRoleModal() {
    try {
        showLoading(true)

        // 멤버 목록 로드
        const response = await fetchWithAuth(`/api/groups/${groupId}/members`)
        const data = await response.json()

        if (!response.ok || !data.result) {
            throw new Error(data.message || "멤버 목록 조회 실패")
        }

        currentMembers = data.result.members || []

        const modal = document.getElementById("dynamic-modal")
        document.getElementById("modal-title").textContent = "멤버 권한 설정"

        const membersList = currentMembers
            .map((member) => {
                const isCurrentUser = window.loggedInUser && member.id === window.loggedInUser.id
                const canChangeRole = !isCurrentUser && member.role !== "LEADER"

                return `
        <div class="settings-member-item">
          <div class="settings-member-info">
            <img src="${member.profileImageUrl || "https://raw.githubusercontent.com/withong/my-storage/main/budderz/default.png"}" 
                 alt="${member.name}" class="settings-member-avatar">
            <div class="settings-member-details">
              <h5>${member.name} ${isCurrentUser ? "(나)" : ""}</h5>
              <p>${formatJoinDate(member.joinedAt)}</p>
            </div>
          </div>
          <div style="display: flex; align-items: center; gap: 12px;">
            <span class="settings-member-role ${member.role.toLowerCase()}">${getRoleText(member.role)}</span>
            ${
                    canChangeRole
                        ? `
              <button class="settings-btn settings-btn-secondary" style="padding: 6px 12px; font-size: 0.8rem;" 
                      onclick="changeMemberRole(${member.id}, '${member.role === "SUB_LEADER" ? "MEMBER" : "SUB_LEADER"}')">
                ${member.role === "SUB_LEADER" ? "멤버로 변경" : "부리더로 변경"}
              </button>
            `
                        : ""
                }
          </div>
        </div>
      `
            })
            .join("")

        document.getElementById("modal-body").innerHTML = `
      <div class="settings-member-list">
        ${membersList}
      </div>
    `

        document.getElementById("modal-footer").innerHTML = `
      <button type="button" class="settings-btn settings-btn-secondary" onclick="closeModal('dynamic-modal')">닫기</button>
    `

        openModal("dynamic-modal")
    } catch (error) {
        console.error("멤버 권한 설정 모달 열기 실패:", error)
        showError(error.message || "멤버 목록을 불러오는데 실패했습니다.")
    } finally {
        showLoading(false)
    }
}

// 멤버 역할 변경
async function changeMemberRole(memberId, newRole) {
    try {
        showLoading(true)

        const response = await fetchWithAuth(`/api/groups/${groupId}/members/${memberId}/role`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({role: newRole}),
        })

        const data = await response.json()

        if (response.ok && data.result) {
            showSuccess("멤버 권한이 성공적으로 변경되었습니다.")
            closeModal("dynamic-modal")
            // 멤버 목록 새로고침을 위해 모달 다시 열기
            setTimeout(() => openMemberRoleModal(), 500)
        } else {
            throw new Error(data.message || "권한 변경 실패")
        }
    } catch (error) {
        console.error("멤버 권한 변경 실패:", error)
        showError(error.message || "멤버 권한 변경 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 기능별 권한 설정 모달 열기 - 모든 권한 타입을 한번에 처리하도록 개선
async function openPermissionModal() {
    try {
        showLoading(true)

        // 권한 정보 로드
        const response = await fetchWithAuth(`/api/groups/${groupId}/permissions`)
        const data = await response.json()

        if (!response.ok || !data.result) {
            throw new Error(data.message || "권한 정보 조회 실패")
        }

        currentPermissions = data.result.permissions || []

        const modal = document.getElementById("dynamic-modal")
        document.getElementById("modal-title").textContent = "기능별 권한 설정"

        // 모든 권한 타입을 정의 (서버에서 누락된 권한도 포함)
        const allPermissionTypes = [
            "CREATE_POST",
            "DELETE_POST",
            "CREATE_SCHEDULE",
            "DELETE_SCHEDULE",
            "CREATE_MISSION",
            "DELETE_MISSION",
            "CREATE_VOTE",
            "DELETE_VOTE",
            "CREATE_DIRECT_CHAT_ROOM",
            "CREATE_INVITE_LINK",
            "INVITE_CHAT_PARTICIPANT",
            "KICK_CHAT_PARTICIPANT",
        ]

        // 현재 권한 설정을 맵으로 변환
        const permissionMap = {}
        currentPermissions.forEach((p) => {
            permissionMap[p.type] = p.role
        })

        // 누락된 권한은 기본값(MEMBER)으로 설정
        allPermissionTypes.forEach((type) => {
            if (!permissionMap[type]) {
                permissionMap[type] = "MEMBER"
            }
        })

        const createPermissions = allPermissionTypes.filter((type) => type.startsWith("CREATE_") || type.includes("INVITE_"))
        const deletePermissions = allPermissionTypes.filter((type) => type.startsWith("DELETE_") || type.includes("KICK_"))

        const permissionGroups = [
            {
                title: "콘텐츠 생성 권한",
                permissions: createPermissions,
                descriptions: {
                    CREATE_POST: "게시글 작성",
                    CREATE_SCHEDULE: "일정 등록",
                    CREATE_MISSION: "미션 등록",
                    CREATE_VOTE: "투표 생성",
                    CREATE_DIRECT_CHAT_ROOM: "일대일 채팅방 생성",
                    CREATE_INVITE_LINK: "초대 링크 생성",
                    INVITE_CHAT_PARTICIPANT: "채팅방 초대",
                },
            },
            {
                title: "다른 멤버의 콘텐츠 삭제 권한",
                permissions: deletePermissions,
                descriptions: {
                    DELETE_POST: "다른 멤버의 게시글 삭제",
                    DELETE_SCHEDULE: "다른 멤버의 일정 삭제",
                    DELETE_MISSION: "다른 멤버의 미션 삭제",
                    DELETE_VOTE: "다른 멤버의 투표 삭제",
                    KICK_CHAT_PARTICIPANT: "채팅방 강퇴",
                },
            },
        ]

        const permissionGroupsHTML = permissionGroups
            .map(
                (group) => `
          <div class="settings-permission-group">
            <h4>${group.title}</h4>
            ${group.permissions
                    .map((permissionType) => {
                        // 삭제 권한인지 확인
                        const isDeletePermission = permissionType.startsWith("DELETE_") || permissionType.includes("KICK_")

                        return `
              <div class="settings-permission-item">
                <div class="settings-permission-info">
                  <h5>${group.descriptions[permissionType] || permissionType}</h5>
                  <p>이 기능을 사용할 수 있는 최소 권한을 설정합니다</p>
                </div>
                <select class="settings-permission-select" data-permission="${permissionType}">
                  ${!isDeletePermission ? `<option value="MEMBER" ${permissionMap[permissionType] === "MEMBER" ? "selected" : ""}>멤버</option>` : ""}
                  <option value="SUB_LEADER" ${permissionMap[permissionType] === "SUB_LEADER" ? "selected" : ""}>부리더</option>
                  <option value="LEADER" ${permissionMap[permissionType] === "LEADER" ? "selected" : ""}>리더</option>
                </select>
              </div>
            `
                    })
                    .join("")}
          </div>
        `,
            )
            .join("")

        document.getElementById("modal-body").innerHTML = permissionGroupsHTML

        document.getElementById("modal-footer").innerHTML = `
      <button type="button" class="settings-btn settings-btn-secondary" onclick="closeModal('dynamic-modal')">취소</button>
      <button type="button" class="settings-btn settings-btn-primary" onclick="updatePermissions()">저장</button>
    `

        openModal("dynamic-modal")
    } catch (error) {
        console.error("권한 설정 모달 열기 실패:", error)
        showError(error.message || "권한 정보를 불러오는데 실패했습니다.")
    } finally {
        showLoading(false)
    }
}

// 권한 업데이트 - 모든 권한 타입을 한번에 전송하도록 개선
async function updatePermissions() {
    try {
        showLoading(true)

        const selects = document.querySelectorAll(".settings-permission-select")

        // 모든 권한 타입을 포함하여 전송
        const permissions = Array.from(selects).map((select) => ({
            type: select.dataset.permission,
            role: select.value,
        }))

        console.log(JSON.stringify(permissions));

        const response = await fetchWithAuth(`/api/groups/${groupId}/permissions`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(permissions),
        })

        const data = await response.json()

        if (response.ok && data.result) {
            currentPermissions = data.result.permissions || []
            closeModal("dynamic-modal")
            showSuccess("권한 설정이 성공적으로 업데이트되었습니다.")
        } else {
            throw new Error(data.message || "권한 업데이트 실패")
        }
    } catch (error) {
        console.error("권한 업데이트 실패:", error)
        showError(error.message || "권한 업데이트 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 가입 요청 관리 모달 열기
async function openJoinRequestModal() {
    try {
        showLoading(true)

        // 가입 요청 중인 멤버 목록 로드
        const response = await fetchWithAuth(`/api/groups/${groupId}/members/requested`)
        const data = await response.json()

        if (!response.ok || !data.result) {
            throw new Error(data.message || "가입 요청 목록 조회 실패")
        }

        const requestedMembers = data.result.members || []

        const modal = document.getElementById("dynamic-modal")
        document.getElementById("modal-title").textContent = "가입 요청 중인 회원 관리"

        if (requestedMembers.length === 0) {
            document.getElementById("modal-body").innerHTML = `
        <div style="text-align: center; padding: 40px; color: #718096;">
          <p>가입 요청 중인 회원이 없습니다.</p>
        </div>
      `
        } else {
            const membersList = requestedMembers
                .map(
                    (member) => `
        <div class="settings-member-item">
          <div class="settings-member-info">
            <img src="${member.profileImageUrl || "https://raw.githubusercontent.com/withong/my-storage/main/budderz/default.png"}" 
                 alt="${member.name}" class="settings-member-avatar">
            <div class="settings-member-details">
              <h5>${member.name}</h5>
              <p>가입 요청일: ${formatJoinDate(member.joinedAt)}</p>
            </div>
          </div>
          <div style="display: flex; gap: 8px;">
            <button class="settings-btn settings-btn-primary" style="padding: 6px 12px; font-size: 0.8rem;" 
                    onclick="approveMember(${member.id})">승인</button>
            <button class="settings-btn settings-btn-danger" style="padding: 6px 12px; font-size: 0.8rem;" 
                    onclick="rejectMember(${member.id})">거절</button>
          </div>
        </div>
      `,
                )
                .join("")

            document.getElementById("modal-body").innerHTML = `
        <div class="settings-member-list">
          ${membersList}
        </div>
      `
        }

        document.getElementById("modal-footer").innerHTML = `
      <button type="button" class="settings-btn settings-btn-secondary" onclick="closeModal('dynamic-modal')">닫기</button>
    `

        openModal("dynamic-modal")
    } catch (error) {
        console.error("가입 요청 관리 모달 열기 실패:", error)
        showError(error.message || "가입 요청 목록을 불러오는데 실패했습니다.")
    } finally {
        showLoading(false)
    }
}

// 가입 승인
async function approveMember(memberId) {
    try {
        showLoading(true)

        const response = await fetchWithAuth(`/api/groups/${groupId}/members/${memberId}/approve`, {
            method: "PATCH",
        })

        const data = await response.json()

        if (response.ok && data.result) {
            showSuccess("가입 요청이 승인되었습니다.")
            closeModal("dynamic-modal")
            // 목록 새로고침을 위해 모달 다시 열기
            setTimeout(() => openJoinRequestModal(), 500)
        } else {
            throw new Error(data.message || "가입 승인 실패")
        }
    } catch (error) {
        console.error("가입 승인 실패:", error)
        showError(error.message || "가입 승인 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 가입 거절
async function rejectMember(memberId) {
    try {
        if (!confirm("이 가입 요청을 거절하시겠습니까?")) {
            return
        }

        showLoading(true)

        const response = await fetchWithAuth(`/api/groups/${groupId}/members/${memberId}/reject`, {
            method: "DELETE",
        })

        if (response.ok) {
            showSuccess("가입 요청이 거절되었습니다.")
            closeModal("dynamic-modal")
            // 목록 새로고침을 위해 모달 다시 열기
            setTimeout(() => openJoinRequestModal(), 500)
        } else {
            const data = await response.json()
            throw new Error(data.message || "가입 거절 실패")
        }
    } catch (error) {
        console.error("가입 거절 실패:", error)
        showError(error.message || "가입 거절 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 멤버 강제 탈퇴 모달 열기
async function openExpelMemberModal() {
    try {
        showLoading(true)

        // 멤버 목록 로드 (현재 사용자와 리더 제외)
        const response = await fetchWithAuth(`/api/groups/${groupId}/members`)
        const data = await response.json()

        if (!response.ok || !data.result) {
            throw new Error(data.message || "멤버 목록 조회 실패")
        }

        const members = data.result.members || []
        const expellableMembers = members.filter(
            (member) => member.id !== window.loggedInUser?.id && member.role !== "LEADER",
        )

        const modal = document.getElementById("dynamic-modal")
        document.getElementById("modal-title").textContent = "멤버 강제 탈퇴"

        if (expellableMembers.length === 0) {
            document.getElementById("modal-body").innerHTML = `
        <div style="text-align: center; padding: 40px; color: #718096;">
          <p>강제 탈퇴시킬 수 있는 멤버가 없습니다.</p>
        </div>
      `
        } else {
            const membersList = expellableMembers
                .map(
                    (member) => `
        <div class="settings-member-item">
          <div class="settings-member-info">
            <img src="${member.profileImageUrl || "https://raw.githubusercontent.com/withong/my-storage/main/budderz/default.png"}" 
                 alt="${member.name}" class="settings-member-avatar">
            <div class="settings-member-details">
              <h5>${member.name}</h5>
              <p>${getRoleText(member.role)} • ${formatJoinDate(member.joinedAt)}</p>
            </div>
          </div>
          <button class="settings-btn settings-btn-danger" style="padding: 6px 12px; font-size: 0.8rem;" 
                  onclick="expelMember(${member.id}, '${member.name}')">강제 탈퇴</button>
        </div>
      `,
                )
                .join("")

            document.getElementById("modal-body").innerHTML = `
        <div style="margin-bottom: 16px; padding: 12px; background: rgba(229, 62, 62, 0.1); border-radius: 8px; color: #e53e3e; font-size: 0.9rem;">
          ⚠️ 강제 탈퇴된 멤버는 다시 가입할 수 있습니다. 완전히 차단하려면 '멤버 차단' 기능을 사용하세요.
        </div>
        <div class="settings-member-list">
          ${membersList}
        </div>
      `
        }

        document.getElementById("modal-footer").innerHTML = `
      <button type="button" class="settings-btn settings-btn-secondary" onclick="closeModal('dynamic-modal')">닫기</button>
    `

        openModal("dynamic-modal")
    } catch (error) {
        console.error("멤버 강제 탈퇴 모달 열기 실패:", error)
        showError(error.message || "멤버 목록을 불러오는데 실패했습니다.")
    } finally {
        showLoading(false)
    }
}

// 멤버 강제 탈퇴
async function expelMember(memberId, memberName) {
    try {
        if (
            !confirm(
                `${memberName}님을 벗터에서 강제 탈퇴시키시겠습니까?\n\n강제 탈퇴된 멤버는 다시 가입 요청할 수 있습니다.`,
            )
        ) {
            return
        }

        showLoading(true)

        const response = await fetchWithAuth(`/api/groups/${groupId}/members/${memberId}/expel`, {
            method: "DELETE",
        })

        if (response.ok) {
            showSuccess(`${memberName}님이 강제 탈퇴되었습니다.`)
            closeModal("dynamic-modal")
            // 목록 새로고침을 위해 모달 다시 열기
            setTimeout(() => openExpelMemberModal(), 500)
        } else {
            const data = await response.json()
            throw new Error(data.message || "강제 탈퇴 실패")
        }
    } catch (error) {
        console.error("멤버 강제 탈퇴 실패:", error)
        showError(error.message || "멤버 강제 탈퇴 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 멤버 차단 및 차단 해제 모달 열기
async function openBlockMemberModal() {
    try {
        showLoading(true)

        // 일반 멤버와 차단된 멤버 목록을 병렬로 로드
        const [membersResponse, blockedResponse] = await Promise.all([
            fetchWithAuth(`/api/groups/${groupId}/members`),
            fetchWithAuth(`/api/groups/${groupId}/members/blocked`),
        ])

        const membersData = await membersResponse.json()
        const blockedData = await blockedResponse.json()

        if (!membersResponse.ok || !membersData.result) {
            throw new Error(membersData.message || "멤버 목록 조회 실패")
        }

        if (!blockedResponse.ok || !blockedData.result) {
            throw new Error(blockedData.message || "차단된 멤버 목록 조회 실패")
        }

        const members = membersData.result.members || []
        const blockedMembers = blockedData.result.members || []

        const blockableMembers = members.filter(
            (member) => member.id !== window.loggedInUser?.id && member.role !== "LEADER",
        )

        const modal = document.getElementById("dynamic-modal")
        document.getElementById("modal-title").textContent = "멤버 차단 및 차단 해제"

        let bodyHTML = ""

        // 차단 가능한 멤버들
        if (blockableMembers.length > 0) {
            const membersList = blockableMembers
                .map(
                    (member) => `
        <div class="settings-member-item">
          <div class="settings-member-info">
            <img src="${member.profileImageUrl || "https://raw.githubusercontent.com/withong/my-storage/main/budderz/default.png"}" 
                 alt="${member.name}" class="settings-member-avatar">
            <div class="settings-member-details">
              <h5>${member.name}</h5>
              <p>${getRoleText(member.role)} • ${formatJoinDate(member.joinedAt)}</p>
            </div>
          </div>
          <button class="settings-btn settings-btn-danger" style="padding: 6px 12px; font-size: 0.8rem;" 
                  onclick="blockMember(${member.id}, '${member.name}')">차단</button>
        </div>
      `,
                )
                .join("")

            bodyHTML += `
        <div style="margin-bottom: 24px;">
          <h4 style="color: #4a5568; margin-bottom: 12px;">멤버 차단</h4>
          <div style="margin-bottom: 12px; padding: 12px; background: rgba(229, 62, 62, 0.1); border-radius: 8px; color: #e53e3e; font-size: 0.9rem;">
            ⚠️ 차단된 멤버는 벗터에서 탈퇴되며, 다시 가입할 수 없습니다.
          </div>
          <div class="settings-member-list" style="max-height: 200px;">
            ${membersList}
          </div>
        </div>
      `
        }

        // 차단된 멤버들
        if (blockedMembers.length > 0) {
            const blockedList = blockedMembers
                .map(
                    (member) => `
        <div class="settings-member-item">
          <div class="settings-member-info">
            <img src="${member.profileImageUrl || "https://raw.githubusercontent.com/withong/my-storage/main/budderz/default.png"}" 
                 alt="${member.name}" class="settings-member-avatar">
            <div class="settings-member-details">
              <h5>${member.name}</h5>
              <p>차단됨 • ${formatJoinDate(member.joinedAt)}</p>
            </div>
          </div>
          <button class="settings-btn settings-btn-primary" style="padding: 6px 12px; font-size: 0.8rem;" 
                  onclick="unblockMember(${member.id}, '${member.name}')">차단 해제</button>
        </div>
      `,
                )
                .join("")

            bodyHTML += `
        <div>
          <h4 style="color: #4a5568; margin-bottom: 12px;">차단된 멤버</h4>
          <div class="settings-member-list" style="max-height: 200px;">
            ${blockedList}
          </div>
        </div>
      `
        }

        if (blockableMembers.length === 0 && blockedMembers.length === 0) {
            bodyHTML = `
        <div style="text-align: center; padding: 40px; color: #718096;">
          <p>차단할 수 있는 멤버나 차단된 멤버가 없습니다.</p>
        </div>
      `
        }

        document.getElementById("modal-body").innerHTML = bodyHTML

        document.getElementById("modal-footer").innerHTML = `
      <button type="button" class="settings-btn settings-btn-secondary" onclick="closeModal('dynamic-modal')">닫기</button>
    `

        openModal("dynamic-modal")
    } catch (error) {
        console.error("멤버 차단 관리 모달 열기 실패:", error)
        showError(error.message || "멤버 목록을 불러오는데 실패했습니다.")
    } finally {
        showLoading(false)
    }
}

// 멤버 차단
async function blockMember(memberId, memberName) {
    try {
        if (!confirm(`${memberName}님을 차단하시겠습니까?\n\n차단된 멤버는 벗터에서 탈퇴되며, 다시 가입할 수 없습니다.`)) {
            return
        }

        showLoading(true)

        const response = await fetchWithAuth(`/api/groups/${groupId}/members/${memberId}/block`, {
            method: "PATCH",
        })

        const data = await response.json()

        if (response.ok && data.result) {
            showSuccess(`${memberName}님이 차단되었습니다.`)
            closeModal("dynamic-modal")
            // 목록 새로고침을 위해 모달 다시 열기
            setTimeout(() => openBlockMemberModal(), 500)
        } else {
            throw new Error(data.message || "멤버 차단 실패")
        }
    } catch (error) {
        console.error("멤버 차단 실패:", error)
        showError(error.message || "멤버 차단 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 멤버 차단 해제
async function unblockMember(memberId, memberName) {
    try {
        if (!confirm(`${memberName}님의 차단을 해제하시겠습니까?`)) {
            return
        }

        showLoading(true)

        const response = await fetchWithAuth(`/api/groups/${groupId}/members/${memberId}/unblock`, {
            method: "DELETE",
        })

        if (response.ok) {
            showSuccess(`${memberName}님의 차단이 해제되었습니다.`)
            closeModal("dynamic-modal")
            // 목록 새로고침을 위해 모달 다시 열기
            setTimeout(() => openBlockMemberModal(), 500)
        } else {
            const data = await response.json()
            throw new Error(data.message || "차단 해제 실패")
        }
    } catch (error) {
        console.error("멤버 차단 해제 실패:", error)
        showError(error.message || "멤버 차단 해제 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 리더 위임 모달 열기
async function openDelegateLeaderModal() {
    try {
        showLoading(true)

        // 멤버 목록 로드 (현재 사용자 제외)
        const response = await fetchWithAuth(`/api/groups/${groupId}/members`)
        const data = await response.json()

        if (!response.ok || !data.result) {
            throw new Error(data.message || "멤버 목록 조회 실패")
        }

        const members = data.result.members || []
        const delegatableMembers = members.filter(
            (member) => member.id !== window.loggedInUser?.id && member.role !== "LEADER",
        )

        const modal = document.getElementById("dynamic-modal")
        document.getElementById("modal-title").textContent = "리더 위임하기"

        if (delegatableMembers.length === 0) {
            document.getElementById("modal-body").innerHTML = `
        <div style="text-align: center; padding: 40px; color: #718096;">
          <p>리더를 위임할 수 있는 멤버가 없습니다.</p>
        </div>
      `
        } else {
            const membersList = delegatableMembers
                .map(
                    (member) => `
        <div class="settings-member-item">
          <div class="settings-member-info">
            <img src="${member.profileImageUrl || "https://raw.githubusercontent.com/withong/my-storage/main/budderz/default.png"}" 
                 alt="${member.name}" class="settings-member-avatar">
            <div class="settings-member-details">
              <h5>${member.name}</h5>
              <p>${getRoleText(member.role)} • ${formatJoinDate(member.joinedAt)}</p>
            </div>
          </div>
          <button class="settings-btn settings-btn-primary" style="padding: 6px 12px; font-size: 0.8rem;" 
                  onclick="delegateLeader(${member.id}, '${member.name}')">리더 위임하기</button>
        </div>
      `,
                )
                .join("")

            document.getElementById("modal-body").innerHTML = `
        <div style="margin-bottom: 16px; padding: 12px; background: rgba(229, 62, 62, 0.1); border-radius: 8px; color: #e53e3e; font-size: 0.9rem;">
          ⚠️ 리더를 위임하면 본인은 일반 멤버가 됩니다. 이 작업은 되돌릴 수 없습니다.
        </div>
        <div class="settings-member-list">
          ${membersList}
        </div>
      `
        }

        document.getElementById("modal-footer").innerHTML = `
      <button type="button" class="settings-btn settings-btn-secondary" onclick="closeModal('dynamic-modal')">닫기</button>
    `

        openModal("dynamic-modal")
    } catch (error) {
        console.error("리더 위임 모달 열기 실패:", error)
        showError(error.message || "멤버 목록을 불러오는데 실패했습니다.")
    } finally {
        showLoading(false)
    }
}

// 리더 위임
async function delegateLeader(memberId, memberName) {
    try {
        if (
            !confirm(
                `${memberName}님에게 리더를 위임하시겠습니까?\n\n⚠️ 이 작업은 되돌릴 수 없으며, 본인은 일반 멤버가 됩니다.`,
            )
        ) {
            return
        }

        if (!confirm("정말로 리더를 위임하시겠습니까? 다시 한 번 확인해주세요.")) {
            return
        }

        showLoading(true)

        const response = await fetchWithAuth(`/api/groups/${groupId}/members/${memberId}/delegate`, {
            method: "PATCH",
        })

        const data = await response.json()

        if (response.ok && data.result) {
            showSuccess(`${memberName}님에게 리더가 위임되었습니다.`)
            closeModal("dynamic-modal")
            // 리더가 변경되었으므로 페이지 새로고침
            setTimeout(() => {
                window.location.reload()
            }, 1000)
        } else {
            throw new Error(data.message || "리더 위임 실패")
        }
    } catch (error) {
        console.error("리더 위임 실패:", error)
        showError(error.message || "리더 위임 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 벗터 삭제
async function deleteGroup() {
    try {
        if (!confirm("정말로 이 벗터를 삭제하시겠습니까?\n\n⚠️ 삭제된 벗터의 모든 데이터는 복구할 수 없습니다.")) {
            return
        }

        if (
            !confirm(
                "마지막 확인입니다. 벗터를 삭제하면 모든 게시글, 댓글, 일정, 미션 등이 영구적으로 삭제됩니다.\n\n정말로 삭제하시겠습니까?",
            )
        ) {
            return
        }

        showLoading(true)

        const response = await fetchWithAuth(`/api/groups/${groupId}`, {
            method: "DELETE",
        })

        if (response.ok) {
            showSuccess("벗터가 성공적으로 삭제되었습니다.")
            // 메인 페이지로 이동
            setTimeout(() => {
                window.location.href = "/test/main"
            }, 1000)
        } else {
            const data = await response.json()
            throw new Error(data.message || "벗터 삭제 실패")
        }
    } catch (error) {
        console.error("벗터 삭제 실패:", error)
        showError(error.message || "벗터 삭제 중 오류가 발생했습니다.")
    } finally {
        showLoading(false)
    }
}

// 유틸리티 함수들
function handleCoverImageChange(event) {
    const file = event.target.files[0]
    if (file) {
        const reader = new FileReader()
        reader.onload = (e) => {
            document.getElementById("cover-preview").src = e.target.result
        }
        reader.readAsDataURL(file)
    }
}

function updateCharCount() {
    const input = document.getElementById("group-name")
    if (input) {
        const count = input.value.length
        const counter = input.parentElement.querySelector(".settings-char-count")
        if (counter) {
            counter.textContent = `${count}/20`
        }
    }
}

function getRoleText(role) {
    const roleMap = {
        LEADER: "리더",
        SUB_LEADER: "부리더",
        MEMBER: "멤버",
    }
    return roleMap[role] || "멤버"
}

function formatJoinDate(joinedAt) {
    if (!joinedAt) return "가입일 정보 없음"

    const date = new Date(joinedAt)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, "0")
    const day = String(date.getDate()).padStart(2, "0")

    return `${year}년 ${month}월 ${day}일 가입`
}
