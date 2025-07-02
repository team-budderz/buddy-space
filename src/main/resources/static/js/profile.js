/**
 * 사용자 프로필 관리 페이지
 * - 개인정보 조회 및 수정
 * - 프로필 이미지 업로드/제거
 * - 비밀번호 변경 및 인증
 * - 동네 인증 기능
 * - 회원 탈퇴 처리
 * - 가입 요청 관리 탭
 */

// 전역 변수
let userInfo = null
let isEditing = false
let selectedFile = null
let imageAction = "keep" // 'keep', 'change', 'remove'
let isPasswordVerified = false

// DOM 요소들
const elements = {
    loading: null,
    profileTab: null,
    requestsTab: null,
    navItems: null,
    editBtn: null,
    saveBtn: null,
    cancelBtn: null,
    passwordModal: null,
    deleteModal: null,
    toast: null,
}

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", async () => {
    initializeElements()
    setupEventListeners()
    await fetchUserInfo()
})

// DOM 요소 초기화
function initializeElements() {
    elements.loading = document.getElementById("loading")
    elements.profileTab = document.getElementById("profile-tab")
    elements.requestsTab = document.getElementById("requests-tab")
    elements.navItems = document.querySelectorAll(".nav-item")
    elements.editBtn = document.getElementById("edit-btn")
    elements.saveBtn = document.getElementById("save-btn")
    elements.cancelBtn = document.getElementById("cancel-btn")
    elements.passwordModal = document.getElementById("password-modal")
    elements.deleteModal = document.getElementById("delete-modal")
    elements.toast = document.getElementById("toast")
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 탭 네비게이션
    elements.navItems.forEach((item) => {
        item.addEventListener("click", () => {
            const tab = item.dataset.tab
            switchTab(tab)
        })
    })

    // 편집 관련 버튼
    elements.editBtn.addEventListener("click", toggleEdit)
    elements.saveBtn.addEventListener("click", saveUserInfo)
    elements.cancelBtn.addEventListener("click", cancelEdit)

    // 프로필 이미지 관련
    document.getElementById("profile-file").addEventListener("change", handleImageChange)
    document.getElementById("remove-image-btn").addEventListener("click", removeProfileImage)

    // 모달 관련
    setupModalEventListeners()

    // 주소 검색
    document.getElementById("address-search-btn").addEventListener("click", handleAddressSearch)

    // 전화번호 포맷팅
    document.getElementById("edit-phone").addEventListener("input", formatPhoneInput)

    // 동네 인증
    document.getElementById("neighborhood-verify-btn").addEventListener("click", handleNeighborhoodAuth)

    // 키보드 단축키
    document.addEventListener("keydown", handleKeyboardShortcuts)
}

// 모달 이벤트 리스너 설정
function setupModalEventListeners() {
    // 비밀번호 변경 모달
    document.getElementById("password-btn").addEventListener("click", () => showModal("password"))
    document.getElementById("password-save").addEventListener("click", updatePassword)
    document.getElementById("password-cancel").addEventListener("click", () => hideModal("password"))

    // 회원 탈퇴 모달
    document.getElementById("delete-btn").addEventListener("click", () => showModal("delete"))
    document.getElementById("delete-confirm").addEventListener("click", deleteAccount)
    document.getElementById("delete-cancel").addEventListener("click", () => hideModal("delete"))

    // 비밀번호 인증 모달
    document.getElementById("auth-confirm").addEventListener("click", authenticatePassword)
    document.getElementById("auth-cancel").addEventListener("click", () => hideModal("password-auth"))

    // 모달 닫기 버튼
    document.querySelectorAll(".modal-close").forEach((btn) => {
        btn.addEventListener("click", (e) => {
            const modal = e.target.closest(".modal")
            if (modal) {
                hideModal(getModalType(modal.id))
            }
        })
    })

    // 모달 외부 클릭시 닫기
    setupModalOutsideClick()
}

// 모달 외부 클릭 설정
function setupModalOutsideClick() {
    const modals = [elements.passwordModal, elements.deleteModal]

    modals.forEach((modal) => {
        if (modal) {
            modal.addEventListener("click", (e) => {
                if (e.target === modal) {
                    hideModal(getModalType(modal.id))
                }
            })
        }
    })

    // 비밀번호 인증 모달
    const passwordAuthModal = document.getElementById("password-auth-modal")
    if (passwordAuthModal) {
        passwordAuthModal.addEventListener("click", (e) => {
            if (e.target === passwordAuthModal) {
                hideModal("password-auth")
            }
        })
    }
}

// 키보드 단축키 처리
function handleKeyboardShortcuts(e) {
    // ESC로 모달 닫기
    if (e.key === "Escape") {
        const openModal = document.querySelector('.modal[style*="block"], .modal.show')
        if (openModal) {
            hideModal(getModalType(openModal.id))
        }
    }

    // Enter로 주요 액션 실행
    if (e.key === "Enter") {
        const activeModal = document.querySelector('.modal[style*="block"], .modal.show')
        if (activeModal) {
            const primaryBtn = activeModal.querySelector(".btn-primary, .auth-confirm, .delete-confirm")
            if (primaryBtn && !primaryBtn.disabled) {
                primaryBtn.click()
            }
        } else if (isEditing) {
            // 편집 모드에서 Ctrl+Enter로 저장
            if (e.ctrlKey) {
                saveUserInfo()
            }
        }
    }
}

// 탭 전환
function switchTab(tab) {
    // 네비게이션 활성화
    elements.navItems.forEach((item) => {
        item.classList.toggle("active", item.dataset.tab === tab)
    })

    // 탭 컨텐츠 표시
    elements.profileTab.classList.toggle("active", tab === "profile")
    elements.requestsTab.classList.toggle("active", tab === "requests")
}

// 사용자 정보 조회
async function fetchUserInfo() {
    try {
        showLoading(true)

        const response = await fetchWithAuth("/api/users/me")
        const data = await response.json()

        if (response.ok) {
            userInfo = data.result
            displayUserInfo()
        } else {
            const errorMessage = data.message || data.code || "사용자 정보를 불러올 수 없습니다."
            showToast(errorMessage, "error")
        }
    } catch (error) {
        console.error("사용자 정보 조회 실패:", error)
        showToast("네트워크 오류가 발생했습니다.", "error")
    } finally {
        showLoading(false)
        elements.profileTab.style.display = "block"
    }
}

// 사용자 정보 표시
function displayUserInfo() {
    if (!userInfo) return

    // 사이드바 정보
    updateElement("sidebar-name", userInfo.name)
    updateElement("sidebar-email", userInfo.email)
    updateProfileImage("sidebar-avatar", "sidebar-fallback", userInfo.profileImageUrl, userInfo.name)

    // 기본 정보
    updateElement("user-name", userInfo.name)
    updateElement("user-email", userInfo.email)
    updateElement("user-birthdate", formatDate(userInfo.birthDate))
    updateElement("user-gender", getGenderText(userInfo.gender))

    // 프로바이더 배지
    updateProviderBadge(userInfo.provider)

    // 동네 정보
    updateNeighborhoodDisplay()

    // 개인 정보
    updateElement("user-phone", formatPhone(userInfo.phone) || "미등록")
    updateProfileImage("profile-image", "profile-fallback", userInfo.profileImageUrl, userInfo.name)

    // 편집 폼 초기화
    initializeEditForm()
}

// 동네 정보 표시 업데이트
function updateNeighborhoodDisplay() {
    const addressElement = document.getElementById("neighborhood-address")
    const statusBadge = document.getElementById("neighborhood-status-badge")
    const verifyBtn = document.getElementById("neighborhood-verify-btn")

    if (userInfo.hasNeighborhood && userInfo.address) {
        // 인증 완료 상태
        addressElement.textContent = userInfo.address
        statusBadge.textContent = "인증완료"
        statusBadge.className = "neighborhood-status-badge verified"
        verifyBtn.textContent = "🔄 동네 재인증"
    } else if (userInfo.address) {
        // 주소는 있지만 미인증
        addressElement.textContent = userInfo.address
        statusBadge.textContent = "미인증"
        statusBadge.className = "neighborhood-status-badge unverified"
        verifyBtn.textContent = "📍 동네 인증하기"
    } else {
        // 주소 없음
        addressElement.textContent = "미등록"
        statusBadge.textContent = "미인증"
        statusBadge.className = "neighborhood-status-badge unverified"
        verifyBtn.textContent = "📍 동네 인증하기"
    }
}

// 편집 폼 초기화
function initializeEditForm() {
    document.getElementById("edit-address").value = userInfo.address || ""
    document.getElementById("edit-phone").value = userInfo.phone || ""
}

// 프로바이더 배지 업데이트
function updateProviderBadge(provider) {
    const providerElement = document.getElementById("user-provider")
    const providerInfo = getProviderInfo(provider)
    providerElement.textContent = providerInfo.text
    providerElement.className = `provider-badge ${providerInfo.class}`
}

// 편집 모드 토글
function toggleEdit() {
    isEditing = !isEditing

    if (isEditing) {
        enterEditMode()
    } else {
        exitEditMode()
    }
}

// 편집 모드 진입
function enterEditMode() {
    elements.editBtn.textContent = "취소"
    elements.editBtn.className = "btn btn-secondary"

    // 편집 가능한 필드 표시
    showEditFields()

    // 저장 버튼 표시
    document.getElementById("save-buttons").style.display = "flex"

    // 이미지 액션 초기화
    imageAction = "keep"
}

// 편집 모드 종료
function exitEditMode() {
    elements.editBtn.textContent = "수정"
    elements.editBtn.className = "btn btn-primary"

    // 편집 필드 숨기기
    hideEditFields()

    // 저장 버튼 숨기기
    document.getElementById("save-buttons").style.display = "none"

    // 선택된 파일 초기화
    resetImageSelection()
}

// 편집 필드 표시
function showEditFields() {
    document.querySelector(".neighborhood-display-line").style.display = "none"
    document.getElementById("address-input-group").style.display = "flex"
    document.getElementById("user-phone").style.display = "none"
    document.getElementById("edit-phone").style.display = "block"
    document.getElementById("image-upload").style.display = "block"
}

// 편집 필드 숨기기
function hideEditFields() {
    document.querySelector(".neighborhood-display-line").style.display = "flex"
    document.getElementById("address-input-group").style.display = "none"
    document.getElementById("user-phone").style.display = "block"
    document.getElementById("edit-phone").style.display = "none"
    document.getElementById("image-upload").style.display = "none"
}

// 이미지 선택 초기화
function resetImageSelection() {
    selectedFile = null
    imageAction = "keep"
    document.getElementById("image-preview").style.display = "none"
    document.getElementById("profile-file").value = ""
}

// 편집 취소
function cancelEdit() {
    // 원래 값으로 복원
    initializeEditForm()
    exitEditMode()
    isEditing = false
}

// 이미지 변경 처리
function handleImageChange(e) {
    const file = e.target.files[0]
    if (file) {
        // 파일 크기 검증 (5MB 제한)
        if (file.size > 5 * 1024 * 1024) {
            showToast("파일 크기는 5MB 이하여야 합니다.", "error")
            e.target.value = ""
            return
        }

        // 파일 타입 검증
        if (!file.type.startsWith("image/")) {
            showToast("이미지 파일만 업로드 가능합니다.", "error")
            e.target.value = ""
            return
        }

        selectedFile = file
        imageAction = "change"

        // 미리보기 표시
        const reader = new FileReader()
        reader.onload = (e) => {
            const previewImg = document.getElementById("preview-img")
            previewImg.src = e.target.result
            document.getElementById("image-preview").style.display = "block"
        }
        reader.readAsDataURL(file)
    }
}

// 프로필 이미지 제거
function removeProfileImage() {
    selectedFile = null
    imageAction = "remove"
    document.getElementById("image-preview").style.display = "none"
    document.getElementById("profile-file").value = ""

    // 기본 이미지로 변경
    document.getElementById("profile-image").style.display = "none"
    document.getElementById("profile-fallback").style.display = "flex"
}

// 주소 검색 처리
function handleAddressSearch() {
    if (typeof window.daum === "undefined" || !window.daum.Postcode) {
        showToast("주소 검색 서비스를 불러올 수 없습니다.", "error")
        return
    }

    new window.daum.Postcode({
        oncomplete: (data) => {
            const jibunAddress = data.jibunAddress
            const { sido, sigungu, bname } = data
            const selectedAddress = jibunAddress || `${sido} ${sigungu} ${bname}`
            document.getElementById("edit-address").value = selectedAddress
        },
    }).open()
}

// 전화번호 입력 포맷팅
function formatPhoneInput(e) {
    let value = e.target.value.replace(/[^0-9]/g, "")

    if (value.length <= 3) {
        value = value
    } else if (value.length <= 7) {
        value = value.replace(/(\d{3})(\d{1,4})/, "$1-$2")
    } else {
        value = value.replace(/(\d{3})(\d{4})(\d{1,4})/, "$1-$2-$3")
    }

    e.target.value = value
}

// 사용자 정보 저장
async function saveUserInfo() {
    if (!isPasswordVerified) {
        showPasswordAuthModal("save", "saveUserInfoAfterAuth")
        return
    }

    await saveUserInfoAfterAuth()
}

// 비밀번호 인증 후 사용자 정보 저장
async function saveUserInfoAfterAuth() {
    try {
        showLoading(true)

        const formData = createUserUpdateFormData()

        const response = await fetchWithAuth("/api/users", {
            method: "PATCH",
            body: formData,
        })

        if (response.ok) {
            showToast("정보가 성공적으로 업데이트되었습니다.")
            exitEditMode()
            isEditing = false
            isPasswordVerified = false
            imageAction = "keep"
            await fetchUserInfo()
        } else {
            const data = await response.json()
            const errorMessage = data.message || data.code || "정보 업데이트에 실패했습니다."
            showToast(errorMessage, "error")
        }
    } catch (error) {
        console.error("사용자 정보 업데이트 실패:", error)
        showToast("네트워크 오류가 발생했습니다.", "error")
    } finally {
        showLoading(false)
    }
}

// 사용자 업데이트 FormData 생성
function createUserUpdateFormData() {
    const formData = new FormData()

    let profileAttachmentId = null
    let profileImage = null

    // 이미지 액션에 따른 처리
    if (imageAction === "keep") {
        profileAttachmentId = userInfo.profileAttachmentId
    } else if (imageAction === "change") {
        profileImage = selectedFile
        profileAttachmentId = null
    } else if (imageAction === "remove") {
        profileAttachmentId = null
        profileImage = null
    }

    const requestData = {
        address: document.getElementById("edit-address").value || null,
        phone: document.getElementById("edit-phone").value || null,
        profileAttachmentId: profileAttachmentId,
    }

    formData.append(
        "request",
        new Blob([JSON.stringify(requestData)], {
            type: "application/json",
        }),
    )

    if (profileImage) {
        formData.append("profileImage", profileImage)
    }

    return formData
}

// 비밀번호 변경
async function updatePassword() {
    if (!isPasswordVerified) {
        showPasswordAuthModal("password", "updatePasswordAfterAuth")
        return
    }

    await updatePasswordAfterAuth()
}

// 비밀번호 인증 후 비밀번호 변경
async function updatePasswordAfterAuth() {
    const newPassword = document.getElementById("new-password").value

    if (!newPassword) {
        showToast("새 비밀번호를 입력해주세요.", "error")
        return
    }

    try {
        const response = await fetchWithAuth("/api/users/password", {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ password: newPassword }),
        })

        if (response.ok) {
            showToast("비밀번호가 성공적으로 변경되었습니다.")
            hideModal("password")
            document.getElementById("new-password").value = ""
            isPasswordVerified = false
        } else {
            const data = await response.json()
            const errorMessage = data.message || data.code || "비밀번호 변경에 실패했습니다."
            showToast(errorMessage, "error")
        }
    } catch (error) {
        console.error("비밀번호 변경 실패:", error)
        showToast("네트워크 오류가 발생했습니다.", "error")
    }
}

// 회원 탈퇴
async function deleteAccount() {
    if (!isPasswordVerified) {
        showPasswordAuthModal("delete", "deleteAccountAfterAuth")
        hideModal("delete")
        return
    }

    await deleteAccountAfterAuth()
}

// 비밀번호 인증 후 회원 탈퇴
async function deleteAccountAfterAuth() {
    try {
        const response = await fetchWithAuth("/api/users", {
            method: "DELETE",
        })

        if (response.ok) {
            showToast("그동안 이용해주셔서 감사합니다.")
            localStorage.removeItem("accessToken")
            setTimeout(() => {
                window.location.href = "/test/login"
            }, 2000)
        } else {
            const data = await response.json()
            const errorMessage = data.message || data.code || "회원 탈퇴에 실패했습니다."
            showToast(errorMessage, "error")
        }
    } catch (error) {
        console.error("회원 탈퇴 실패:", error)
        showToast("네트워크 오류가 발생했습니다.", "error")
    }
}

// 비밀번호 인증 모달 표시
function showPasswordAuthModal(action, callback) {
    const modal = document.getElementById("password-auth-modal")
    modal.dataset.action = action
    modal.dataset.callback = callback
    modal.classList.add("show")
}

// 비밀번호 인증
async function authenticatePassword() {
    const password = document.getElementById("auth-password").value

    if (!password) {
        showToast("비밀번호를 입력해주세요.", "error")
        return
    }

    try {
        const response = await fetchWithAuth("/api/users/verify-password", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ password }),
        })

        hideModal("password-auth")

        if (response.ok) {
            isPasswordVerified = true

            // 인증 성공 후 원래 작업 실행
            const modal = document.getElementById("password-auth-modal")
            const callbackName = modal.dataset.callback

            if (callbackName && window[callbackName]) {
                window[callbackName]()
            }

            document.getElementById("auth-password").value = ""
        } else {
            const data = await response.json()
            const errorMessage = data.message || data.code || "비밀번호가 올바르지 않습니다."
            showToast(errorMessage, "error")
        }
    } catch (error) {
        console.error("비밀번호 인증 실패:", error)
        showToast("네트워크 오류가 발생했습니다.", "error")
    }
}

// 동네 인증 처리
async function handleNeighborhoodAuth() {
    if (!navigator.geolocation) {
        showToast("이 브라우저는 위치 정보 API를 지원하지 않습니다.", "error")
        return
    }

    const verifyBtn = document.getElementById("neighborhood-verify-btn")
    const originalText = verifyBtn.textContent

    // 로딩 상태 표시
    setNeighborhoodButtonLoading(verifyBtn, true)

    navigator.geolocation.getCurrentPosition(
        async (position) => {
            const { latitude, longitude } = position.coords

            try {
                const response = await fetchWithAuth("/api/neighborhoods", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        latitude,
                        longitude,
                    }),
                })

                if (response.ok) {
                    const json = await response.json()

                    if (!json?.result) {
                        throw new Error("서버 응답에 result가 없습니다.")
                    }

                    const neighborhood = json.result

                    // 사용자 정보 다시 조회하여 최신 상태 반영
                    await fetchUserInfo()

                    showToast(`동네 인증 완료: ${neighborhood.address}`)
                } else {
                    const data = await response.json()
                    const errorMessage = data.message || data.code || "동네 인증에 실패했습니다."
                    showToast(errorMessage, "error")
                }
            } catch (err) {
                console.error("동네 인증 실패", err)
                showToast(err.message || "동네 인증에 실패했습니다.", "error")
            } finally {
                setNeighborhoodButtonLoading(verifyBtn, false, originalText)
            }
        },
        (err) => {
            console.error("위치 권한 거부됨", err)
            const errorMessage = getGeolocationErrorMessage(err)
            showToast(errorMessage, "error")
            setNeighborhoodButtonLoading(verifyBtn, false, originalText)
        },
        {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 300000, // 5분
        },
    )
}

// 동네 인증 버튼 로딩 상태 설정
function setNeighborhoodButtonLoading(button, isLoading, text = null) {
    if (isLoading) {
        button.innerHTML = '<div class="neighborhood-loading"><div class="loading-spinner"></div>위치 확인 중...</div>'
        button.disabled = true
    } else {
        button.textContent = text || button.textContent
        button.disabled = false
    }
}

// 위치 정보 에러 메시지 생성
function getGeolocationErrorMessage(err) {
    switch (err.code) {
        case err.PERMISSION_DENIED:
            return "위치 권한이 거부되었습니다. 브라우저 설정에서 위치 권한을 허용해주세요."
        case err.POSITION_UNAVAILABLE:
            return "위치 정보를 사용할 수 없습니다."
        case err.TIMEOUT:
            return "위치 정보 요청 시간이 초과되었습니다."
        default:
            return "위치 정보를 가져올 수 없습니다."
    }
}

// 모달 표시/숨기기
function showModal(type) {
    if (type === "password") {
        elements.passwordModal.classList.add("show")
    } else if (type === "delete") {
        elements.deleteModal.classList.add("show")
    }
}

function hideModal(type) {
    if (type === "password") {
        elements.passwordModal.classList.remove("show")
    } else if (type === "delete") {
        elements.deleteModal.classList.remove("show")
    } else if (type === "password-auth") {
        document.getElementById("password-auth-modal").classList.remove("show")
        document.getElementById("auth-password").value = ""
    }
}

// 모달 타입 추출
function getModalType(modalId) {
    const typeMap = {
        "password-modal": "password",
        "delete-modal": "delete",
        "password-auth-modal": "password-auth",
    }
    return typeMap[modalId] || "unknown"
}

// 로딩 표시
function showLoading(show) {
    if (elements.loading) {
        elements.loading.style.display = show ? "flex" : "none"
    }
}

// 토스트 알림
function showToast(message, type = "success") {
    if (elements.toast) {
        elements.toast.textContent = message
        elements.toast.className = `toast ${type}`
        elements.toast.classList.add("show")

        setTimeout(() => {
            elements.toast.classList.remove("show")
        }, 5000)
    }
}

// 유틸리티 함수들
function updateElement(id, value) {
    const element = document.getElementById(id)
    if (element) element.textContent = value
}

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

function getProviderInfo(provider) {
    const providers = {
        LOCAL: { text: "일반", class: "" },
        GOOGLE: { text: "구글", class: "google" },
    }
    return providers[provider] || providers.LOCAL
}

function getGenderText(gender) {
    const genderMap = {
        M: "남성",
        F: "여성",
    }
    return genderMap[gender] || "알 수 없음"
}

function formatDate(dateString) {
    if (!dateString) return "미등록"
    return new Date(dateString).toLocaleDateString("ko-KR")
}

function formatPhone(phone) {
    if (!phone) return ""
    return phone.replace(/(\d{2,3})(\d{3,4})(\d{4})/, "$1-$2-$3")
}
