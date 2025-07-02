/**
 * 모임 멤버 관리 페이지
 * - 멤버 목록 조회 및 표시
 * - 권한별 멤버 관리 기능
 * - 초대 링크 생성 및 관리
 * - 멤버 강제 탈퇴 및 차단
 * - 1:1 대화 기능
 */

// 전역 변수
let currentUser = null
let groupMembers = []

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", async () => {
    groupId = new URLSearchParams(window.location.search).get("id")
    await loadMembersData()
    setupEventListeners()
})

// 멤버 데이터 로드
async function loadMembersData() {
    try {
        // 권한 데이터 초기화
        const permissionsInitialized = await GroupPermissions.initialize(groupId)
        if (!permissionsInitialized) {
            showError("권한 정보를 불러올 수 없습니다.")
            return
        }

        // 현재 사용자 정보 가져오기
        currentUser = GroupPermissions.getCurrentUserMembership()

        // 멤버 목록 로드
        const membersResponse = await fetchWithAuth(`/api/groups/${groupId}/members`)
        const membersData = await membersResponse.json()
        if (membersResponse.ok && membersData.result) {
            groupMembers = membersData.result.members || []
        }

        renderMembers()
        updateInviteButton()
        updateWithdrawButton()
    } catch (error) {
        console.error("멤버 데이터 로드 실패:", error)
        showError("멤버 정보를 불러오는데 실패했습니다.")
    }
}

// 멤버 목록 렌더링
function renderMembers() {
    const membersList = document.getElementById("members-list")
    const memberCount = document.getElementById("member-count")

    if (!groupMembers.length) {
        membersList.innerHTML = '<div class="loading">멤버가 없습니다.</div>'
        memberCount.textContent = "(0)"
        return
    }

    // 멤버 정렬: 현재 사용자 -> 리더 -> 부리더 -> 일반 멤버
    const sortedMembers = [...groupMembers].sort((a, b) => {
        // 현재 사용자가 최우선
        if (currentUser && a.id === currentUser.id) return -1
        if (currentUser && b.id === currentUser.id) return 1

        // 역할별 우선순위 (LEADER=3, SUB_LEADER=2, MEMBER=1)
        const roleOrder = { LEADER: 3, SUB_LEADER: 2, MEMBER: 1 }
        return (roleOrder[b.role] || 0) - (roleOrder[a.role] || 0)
    })

    memberCount.textContent = `(${sortedMembers.length})`

    const membersHTML = sortedMembers
        .map((member) => {
            const isCurrentUser = currentUser && member.id === currentUser.id
            const roleText = getRoleText(member.role)
            const showMenu = !isCurrentUser && (isLeader() || hasDirectChatPermission())

            return `
            <div class="member-item ${isCurrentUser ? "current-user" : ""}" data-member-id="${member.id}">
                <img src="${member.profileImageUrl || "https://raw.githubusercontent.com/withong/my-storage/main/budderz/default.png"}" 
                     alt="${member.name}" 
                     class="member-avatar"
                     onclick="showProfileModal('${member.profileImageUrl || "https://raw.githubusercontent.com/withong/my-storage/main/budderz/default.png"}', '${formatJoinDate(member.joinedAt)}')">
                
                <div class="member-info">
                    <p class="member-name">${member.name}</p>
                    <p class="member-role ${member.role.toLowerCase()}">${roleText}</p>
                </div>
                
                ${
                showMenu
                    ? `
                    <button class="member-menu" onclick="showMemberMenu(${member.id}, '${member.name}')">
                        ⋯
                    </button>
                `
                    : ""
            }
            </div>
        `
        })
        .join("")

    membersList.innerHTML = membersHTML
}

// 역할 텍스트 변환
function getRoleText(role) {
    const roleMap = {
        LEADER: "리더",
        SUB_LEADER: "부리더",
        MEMBER: "멤버",
    }
    return roleMap[role] || "멤버"
}

// 가입일자 포맷팅
function formatJoinDate(joinedAt) {
    if (!joinedAt) return "가입일 정보 없음"

    const date = new Date(joinedAt)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, "0")
    const day = String(date.getDate()).padStart(2, "0")

    return `${year}년 ${month}월 ${day}일 가입`
}

// 초대 버튼 업데이트
function updateInviteButton() {
    const inviteBtn = document.getElementById("invite-btn")
    const hasInvitePermission = GroupPermissions.canCreateInviteLink()

    if (hasInvitePermission) {
        inviteBtn.style.display = "block"
    } else {
        inviteBtn.style.display = "none"
    }
}

// 탈퇴 버튼 업데이트
function updateWithdrawButton() {
    const withdrawSection = document.getElementById("withdraw-section")
    const isCurrentUserLeader = GroupPermissions.isLeader()

    if (!isCurrentUserLeader && currentUser) {
        withdrawSection.style.display = "block"
    } else {
        withdrawSection.style.display = "none"
    }
}

// 권한 체크 함수들
function isLeader() {
    return GroupPermissions.isLeader()
}

function hasDirectChatPermission() {
    return GroupPermissions.canCreateDirectChat()
}

// 프로필 모달 표시
function showProfileModal(imageUrl, joinDate) {
    const modal = document.getElementById("profile-modal")
    const modalImage = document.getElementById("modal-image")
    const modalJoinDate = document.getElementById("modal-join-date")

    modalImage.src = imageUrl
    modalJoinDate.textContent = joinDate
    modal.style.display = "block"
}

// 멤버 메뉴 표시
function showMemberMenu(memberId, memberName) {
    const modal = document.getElementById("menu-modal")
    const menuOptions = document.getElementById("menu-options")

    let optionsHTML = ""

    console.log("isLeader: ", isLeader())
    if (isLeader()) {
        optionsHTML = `
            <div class="menu-option" onclick="startDirectChat(${memberId}, '${memberName}')">
                1:1 대화
            </div>
            <div class="menu-option danger" onclick="kickMember(${memberId}, '${memberName}')">
                강제 탈퇴
            </div>
            <div class="menu-option danger" onclick="blockMember(${memberId}, '${memberName}')">
                차단
            </div>
        `
    } else if (hasDirectChatPermission()) {
        optionsHTML = `
            <div class="menu-option" onclick="startDirectChat(${memberId}, '${memberName}')">
                1:1 대화
            </div>
        `
    }

    menuOptions.innerHTML = optionsHTML
    modal.style.display = "block"
}

// 멤버 강제 탈퇴
async function kickMember(memberId, memberName) {
    if (!confirm(`${memberName}님을 모임에서 강제 탈퇴시키시겠습니까?`)) {
        return
    }

    try {
        const response = await fetchWithAuth(`/api/groups/${groupId}/members/${memberId}/expel`, {
            method: "DELETE",
        })

        if (response.ok) {
            alert(`${memberName}님이 모임에서 탈퇴되었습니다.`)
            closeModal()
            await loadMembersData() // 목록 새로고침
        } else {
            const data = await response.json()
            alert(`강제 탈퇴 실패: ${data.message || "알 수 없는 오류"}`)
        }
    } catch (error) {
        console.error("강제 탈퇴 실패:", error)
        alert("강제 탈퇴 중 오류가 발생했습니다.")
    }
}

// 멤버 차단
async function blockMember(memberId, memberName) {
    if (!confirm(`${memberName}님을 차단하시겠습니까?`)) {
        return
    }

    try {
        const response = await fetchWithAuth(`/api/groups/${groupId}/members/${memberId}/block`, {
            method: "PATCH",
        })

        if (response.ok) {
            alert(`${memberName}님이 차단되었습니다.`)
            closeModal()
            await loadMembersData() // 목록 새로고침
        } else {
            const data = await response.json()
            alert(`차단 실패: ${data.message || "알 수 없는 오류"}`)
        }
    } catch (error) {
        console.error("차단 실패:", error)
        alert("차단 중 오류가 발생했습니다.")
    }
}

// 1:1 대화 시작
async function startDirectChat(memberId, memberName) {
    alert(`${memberName}님과의 1:1 대화 기능은 준비 중입니다.`)
    closeModal()
}

// 멤버 초대
async function inviteMembers() {
    try {
        // 초대 링크 조회
        const response = await fetchWithAuth(`/api/groups/${groupId}/invites`)
        const data = await response.json()

        if (response.ok && data.result) {
            showInviteModal(data.result)
        } else {
            alert(`초대 링크 조회 실패: ${data.message || "알 수 없는 오류"}`)
        }
    } catch (error) {
        console.error("초대 링크 조회 실패:", error)
        alert("초대 링크 조회 중 오류가 발생했습니다.")
    }
}

// 초대 모달 표시 함수 추가:
function showInviteModal(inviteData) {
    const modal = document.getElementById("invite-modal")
    const modalTitle = document.getElementById("invite-modal-title")
    const modalContent = document.getElementById("invite-modal-content")

    modalTitle.textContent = `${inviteData.groupName} 초대`

    if (inviteData.inviteLink) {
        // 초대 링크가 있는 경우
        modalContent.innerHTML = `
      <div class="invite-info">
        <p class="invite-description">${inviteData.groupDescription || "새로운 친구를 초대해보세요!"}</p>
        <div class="invite-link-container">
          <label for="invite-link-input">초대 링크</label>
          <div class="link-input-group">
            <input type="text" id="invite-link-input" value="${inviteData.inviteLink}" readonly>
            <button class="copy-btn" onclick="copyInviteLink()">복사</button>
          </div>
        </div>
        <div class="invite-code-container">
          <label for="invite-code-input">초대 코드</label>
          <div class="code-input-group">
            <input type="text" id="invite-code-input" value="${inviteData.code}" readonly>
            <button class="copy-btn" onclick="copyInviteCode()">복사</button>
          </div>
        </div>
        <div class="invite-actions">
          <button class="refresh-btn" onclick="refreshInviteLink()">새 링크 생성</button>
        </div>
      </div>
    `
    } else {
        // 초대 링크가 없는 경우
        modalContent.innerHTML = `
      <div class="invite-info">
        <p class="invite-description">아직 초대 링크가 생성되지 않았습니다.</p>
        <div class="invite-actions">
          <button class="create-btn" onclick="createInviteLink()">초대 링크 만들기</button>
        </div>
      </div>
    `
    }

    modal.style.display = "block"
}

// 초대 링크 생성/새로고침 함수 추가:
async function createInviteLink() {
    try {
        const createBtn = document.querySelector(".create-btn")
        if (createBtn) {
            createBtn.textContent = "생성 중..."
            createBtn.disabled = true
        }

        const response = await fetchWithAuth(`/api/groups/${groupId}/invites`, {
            method: "PATCH",
        })
        const data = await response.json()

        if (response.ok && data.result) {
            showInviteModal(data.result)
            showToast("초대 링크가 생성되었습니다!")
        } else {
            throw new Error(`초대 링크 생성 실패: ${data.message || "알 수 없는 오류"}`)
        }
    } catch (error) {
        console.error("초대 링크 생성 실패:", error)
        alert(error.message || "초대 링크 생성 중 오류가 발생했습니다.")

        // 버튼 상태 복원
        const createBtn = document.querySelector(".create-btn")
        if (createBtn) {
            createBtn.textContent = "초대 링크 만들기"
            createBtn.disabled = false
        }
    }
}

// 초대 링크 새로고침 (새 링크 생성)
async function refreshInviteLink() {
    if (!confirm("새로운 초대 링크를 생성하시겠습니까? 기존 링크는 사용할 수 없게 됩니다.")) {
        return
    }

    try {
        const refreshBtn = document.querySelector(".refresh-btn")
        if (refreshBtn) {
            refreshBtn.textContent = "삭제 중..."
            refreshBtn.disabled = true
        }

        // 1. 기존 초대 링크 삭제
        const deleteResponse = await fetchWithAuth(`/api/groups/${groupId}/invites`, {
            method: "DELETE",
        })

        if (!deleteResponse.ok) {
            const deleteData = await deleteResponse.json()
            throw new Error(`기존 링크 삭제 실패: ${deleteData.message || "알 수 없는 오류"}`)
        }

        if (refreshBtn) {
            refreshBtn.textContent = "생성 중..."
        }

        // 2. 새로운 초대 링크 생성
        const createResponse = await fetchWithAuth(`/api/groups/${groupId}/invites`, {
            method: "PATCH",
        })
        const createData = await createResponse.json()

        if (createResponse.ok && createData.result) {
            showInviteModal(createData.result)
            showToast("새로운 초대 링크가 생성되었습니다!")
        } else {
            throw new Error(`새 링크 생성 실패: ${createData.message || "알 수 없는 오류"}`)
        }
    } catch (error) {
        console.error("초대 링크 새로고침 실패:", error)
        alert(error.message || "초대 링크 새로고침 중 오류가 발생했습니다.")

        // 버튼 상태 복원
        const refreshBtn = document.querySelector(".refresh-btn")
        if (refreshBtn) {
            refreshBtn.textContent = "새 링크 생성"
            refreshBtn.disabled = false
        }
    }
}

// 초대 링크 복사 함수 추가:
async function copyInviteLink() {
    const linkInput = document.getElementById("invite-link-input")
    try {
        await navigator.clipboard.writeText(linkInput.value)
        showToast("초대 링크가 복사되었습니다!")
    } catch (error) {
        // 클립보드 API가 지원되지 않는 경우 fallback
        linkInput.select()
        document.execCommand("copy")
        showToast("초대 링크가 복사되었습니다!")
    }
}

// 초대 코드 복사 함수 추가:
async function copyInviteCode() {
    const codeInput = document.getElementById("invite-code-input")
    try {
        await navigator.clipboard.writeText(codeInput.value)
        showToast("초대 코드가 복사되었습니다!")
    } catch (error) {
        // 클립보드 API가 지원되지 않는 경우 fallback
        codeInput.select()
        document.execCommand("copy")
        showToast("초대 코드가 복사되었습니다!")
    }
}

// 토스트 메시지 표시 함수 추가:
function showToast(message) {
    // 기존 토스트 제거
    const existingToast = document.querySelector(".toast")
    if (existingToast) {
        existingToast.remove()
    }

    const toast = document.createElement("div")
    toast.className = "toast"
    toast.textContent = message
    document.body.appendChild(toast)

    // 애니메이션 후 제거
    setTimeout(() => {
        toast.classList.add("show")
    }, 100)

    setTimeout(() => {
        toast.classList.remove("show")
        setTimeout(() => toast.remove(), 300)
    }, 3000)
}

// 모임 탈퇴
async function withdrawFromGroup() {
    if (!confirm("정말로 이 모임에서 탈퇴하시겠습니까?\n탈퇴 후에는 다시 가입 요청을 하거나, \n비공개 모임일 경우 초대 링크로만 참여할 수 있습니다.")) {
        return
    }

    try {
        const response = await fetchWithAuth(`/api/groups/${groupId}/withdraw`, {
            method: "DELETE",
        })

        if (response.ok) {
            alert("모임에서 탈퇴되었습니다.")
            // 메인 페이지로 이동
            window.location.href = "/test/main"
        } else {
            const data = await response.json()
            alert(`탈퇴 실패: ${data.message || "알 수 없는 오류"}`)
        }
    } catch (error) {
        console.error("모임 탈퇴 실패:", error)
        alert("모임 탈퇴 중 오류가 발생했습니다.")
    }
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 초대 버튼
    const inviteBtn = document.getElementById("invite-btn")
    if (inviteBtn) {
        inviteBtn.addEventListener("click", inviteMembers)
    }

    // 탈퇴 버튼
    const withdrawBtn = document.getElementById("withdraw-btn")
    if (withdrawBtn) {
        withdrawBtn.addEventListener("click", withdrawFromGroup)
    }

    // 모달 닫기
    const modals = document.querySelectorAll(".modal")
    const closeButtons = document.querySelectorAll(".close")

    closeButtons.forEach((closeBtn) => {
        closeBtn.addEventListener("click", closeModal)
    })

    modals.forEach((modal) => {
        modal.addEventListener("click", (e) => {
            if (e.target === modal) {
                closeModal()
            }
        })
    })

    // ESC 키로 모달 닫기
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            closeModal()
        }
    })
}

// 모달 닫기
function closeModal() {
    const modals = document.querySelectorAll(".modal")
    modals.forEach((modal) => {
        modal.style.display = "none"
    })
}

// 에러 표시
function showError(message) {
    const membersList = document.getElementById("members-list")
    membersList.innerHTML = `<div class="error">${message}</div>`
}
