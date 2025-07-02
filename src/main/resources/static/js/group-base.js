/**
 * 모임 기본 기능 및 권한 관리
 * - 모임 탭 네비게이션 생성
 * - 사용자 권한 시스템 관리
 * - 역할별 접근 제어
 * - 권한 기반 UI 표시/숨김 처리
 */

// 전역 변수
let groupId = null

// 권한 레벨 정의 (숫자가 높을수록 상위 권한)
const ROLE_LEVELS = {
    MEMBER: 1,
    SUB_LEADER: 2,
    LEADER: 3,
}

// 권한 관리 전역 변수
let currentUserMembership = null
let groupPermissions = []
let groupInfo = null

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", () => {
    // URL에서 그룹 ID 추출
    groupId =
        new URLSearchParams(window.location.search).get("id") || new URLSearchParams(window.location.search).get("groupId")

    if (!groupId) {
        showError("모임 ID를 찾을 수 없습니다.")
        return
    }

    // 탭 네비게이션 생성
    createGroupNavigation()

    // 권한 시스템 초기화
    initializePermissions()
})

// 모임 탭 네비게이션 생성
function createGroupNavigation() {
    const tabContainer = document.createElement("nav")
    tabContainer.className = "group-nav"

    const tabs = [
        { name: "게시글", path: "main" },
        { name: "일정", path: "schedule" },
        { name: "미션", path: "mission" },
        { name: "투표", path: "vote" },
        { name: "사진첩", path: "album" },
        { name: "멤버", path: "members" },
        { name: "설정", path: "setting", id: "tab-setting" },
    ]

    const currentPath = location.pathname.split("/").pop()

    // 각 탭 생성 및 활성 상태 설정
    tabs.forEach((tab) => {
        const a = document.createElement("a")
        a.href = `/test/group/${tab.path}?id=${groupId}`
        a.textContent = tab.name
        if (tab.id) a.id = tab.id
        if (currentPath === `${tab.path}`) a.classList.add("active")

        tabContainer.appendChild(a)
    })

    document.querySelector("main")?.prepend(tabContainer)
}

// 권한 시스템 초기화
async function initializePermissions() {
    try {
        const permissionsInitialized = await window.GroupPermissions.initialize(groupId)
        if (!permissionsInitialized) {
            console.error("권한 정보를 불러올 수 없습니다.")
            return
        }

        // 설정 탭 표시/숨김 (리더만 접근 가능)
        window.GroupPermissions.updateSettingTabVisibility()
    } catch (e) {
        console.error("권한 초기화 실패:", e.message)
    }
}

// 설정 탭 가시성 업데이트
function updateSettingTabVisibility() {
    const settingTab = document.querySelector("#tab-setting")
    if (settingTab) {
        settingTab.style.display = window.GroupPermissions.isLeader() ? "block" : "none"
    }
}

// 모임 권한 데이터 초기화
async function initializeGroupPermissions(groupId) {
    if (!groupId) {
        console.error("Group ID is required")
        return false
    }

    try {
        // 사용자 멤버십과 권한 정보를 병렬로 로드
        const [membershipResponse, permissionsResponse] = await Promise.all([
            fetchWithAuth(`/api/groups/${groupId}/membership`),
            fetchWithAuth(`/api/groups/${groupId}/permissions`),
        ])

        // 사용자 멤버십 정보 처리
        const membershipData = await membershipResponse.json()
        if (membershipResponse.ok && membershipData.result) {
            currentUserMembership = membershipData.result
            console.log("User membership loaded:", currentUserMembership)
        } else {
            console.error("Failed to load user membership:", membershipData.message)
            return false
        }

        // 권한 정보 처리
        const permissionsData = await permissionsResponse.json()
        if (permissionsResponse.ok && permissionsData.result) {
            groupPermissions = permissionsData.result.permissions || []
            groupInfo = {
                groupId: permissionsData.result.groupId,
                groupName: permissionsData.result.groupName,
            }
            console.log("Group permissions loaded:", groupPermissions)
        } else {
            console.error("Failed to load group permissions:", permissionsData.message)
            return false
        }

        return true
    } catch (error) {
        console.error("Error initializing group permissions:", error)
        return false
    }
}

// 현재 사용자의 역할 반환
function getCurrentUserRole() {
    return currentUserMembership?.role || null
}

// 현재 사용자의 멤버십 정보 반환
function getCurrentUserMembership() {
    return currentUserMembership
}

// 모임 정보 반환
function getGroupInfo() {
    return groupInfo
}

// 모든 권한 정보 반환
function getAllPermissions() {
    return [...groupPermissions]
}

// 역할 레벨 비교
function hasRoleLevel(userRole, requiredRole) {
    if (!userRole || !requiredRole) return false

    const userLevel = ROLE_LEVELS[userRole] || 0
    const requiredLevel = ROLE_LEVELS[requiredRole] || 0

    return userLevel >= requiredLevel
}

// 특정 권한 타입에 대한 접근 권한 확인
function hasPermission(permissionType) {
    if (!currentUserMembership || !groupPermissions.length) {
        console.warn("Permission data not initialized")
        return false
    }

    const userRole = currentUserMembership.role
    const permission = groupPermissions.find((p) => p.type === permissionType)

    if (!permission) {
        console.warn(`Permission type '${permissionType}' not found`)
        return false
    }

    return hasRoleLevel(userRole, permission.role)
}

// 여러 권한에 대한 접근 권한 확인
function hasPermissions(permissionTypes) {
    const result = {}
    permissionTypes.forEach((type) => {
        result[type] = hasPermission(type)
    })
    return result
}

// 사용자가 리더인지 확인
function isLeader() {
    return getCurrentUserRole() === "LEADER"
}

// 사용자가 부리더 이상인지 확인
function isSubLeaderOrAbove() {
    const role = getCurrentUserRole()
    return role === "SUB_LEADER" || role === "LEADER"
}

// 사용자가 일반 멤버 이상인지 확인
function isMemberOrAbove() {
    const role = getCurrentUserRole()
    return role === "MEMBER" || role === "SUB_LEADER" || role === "LEADER"
}

// 특정 기능별 권한 확인 함수들
const PermissionChecker = {
    // 게시글 관련 권한
    canCreatePost: () => hasPermission("CREATE_POST"),
    canDeletePost: () => hasPermission("DELETE_POST"),

    // 일정 관련 권한
    canCreateSchedule: () => hasPermission("CREATE_SCHEDULE"),
    canDeleteSchedule: () => hasPermission("DELETE_SCHEDULE"),

    // 미션 관련 권한
    canCreateMission: () => hasPermission("CREATE_MISSION"),
    canDeleteMission: () => hasPermission("DELETE_MISSION"),

    // 투표 관련 권한
    canCreateVote: () => hasPermission("CREATE_VOTE"),
    canDeleteVote: () => hasPermission("DELETE_VOTE"),

    // 채팅 관련 권한
    canCreateDirectChat: () => hasPermission("CREATE_DIRECT_CHAT_ROOM"),
    canInviteChatParticipant: () => hasPermission("INVITE_CHAT_PARTICIPANT"),
    canKickChatParticipant: () => hasPermission("KICK_CHAT_PARTICIPANT"),

    // 초대 관련 권한
    canCreateInviteLink: () => hasPermission("CREATE_INVITE_LINK"),

    // 복합 권한 체크
    canManagePosts: () => hasPermission("CREATE_POST") || hasPermission("DELETE_POST"),
    canManageSchedules: () => hasPermission("CREATE_SCHEDULE") || hasPermission("DELETE_SCHEDULE"),
    canManageMissions: () => hasPermission("CREATE_MISSION") || hasPermission("DELETE_MISSION"),
    canManageVotes: () => hasPermission("CREATE_VOTE") || hasPermission("DELETE_VOTE"),
    canManageChat: () =>
        hasPermission("CREATE_DIRECT_CHAT_ROOM") ||
        hasPermission("INVITE_CHAT_PARTICIPANT") ||
        hasPermission("KICK_CHAT_PARTICIPANT"),
}

// UI 요소 표시/숨김 처리
function toggleElementByPermission(elementId, hasPermission) {
    const element = document.getElementById(elementId)
    if (element) {
        element.style.display = hasPermission ? "block" : "none"
    }
}

// 여러 UI 요소들을 권한에 따라 일괄 처리
function toggleElementsByPermissions(elementPermissions) {
    Object.entries(elementPermissions).forEach(([elementId, permissionType]) => {
        const hasAccess = hasPermission(permissionType)
        toggleElementByPermission(elementId, hasAccess)
    })
}

// 권한 정보를 콘솔에 출력 (디버깅용)
function debugPermissions() {
    console.group("🔐 Group Permissions Debug")
    console.log("Current User Membership:", currentUserMembership)
    console.log("Group Info:", groupInfo)
    console.log("All Permissions:", groupPermissions)

    console.group("Permission Checks")
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

    allPermissionTypes.forEach((type) => {
        console.log(`${type}: ${hasPermission(type)}`)
    })
    console.groupEnd()

    console.group("Role Checks")
    console.log("Is Leader:", isLeader())
    console.log("Is Sub Leader or Above:", isSubLeaderOrAbove())
    console.log("Is Member or Above:", isMemberOrAbove())
    console.groupEnd()

    console.groupEnd()
}

// 권한 데이터 새로고침
async function refreshPermissions(groupId) {
    console.log("Refreshing permissions...")
    return await window.GroupPermissions.initialize(groupId)
}

// 전역 GroupPermissions 객체 생성
window.GroupPermissions = {
    // 초기화
    initialize: initializeGroupPermissions,
    refresh: refreshPermissions,

    // 데이터 조회
    getCurrentUserRole,
    getCurrentUserMembership,
    getGroupInfo,
    getAllPermissions,

    // 권한 체크
    hasPermission,
    hasPermissions,
    hasRoleLevel,

    // 역할 체크
    isLeader,
    isSubLeaderOrAbove,
    isMemberOrAbove,

    // 기능별 권한 체크
    ...PermissionChecker,

    // UI 유틸리티
    toggleElementByPermission,
    toggleElementsByPermissions,

    // 디버깅
    debug: debugPermissions,

    // 상수
    ROLE_LEVELS,
    updateSettingTabVisibility,
}

// 모듈 방식으로도 사용 가능하도록 내보내기
if (typeof module !== "undefined" && module.exports) {
    module.exports = window.GroupPermissions
}
