/**
 * 모임 생성 페이지
 * - 모임 정보 입력 폼 처리
 * - 커버 이미지 업로드 및 미리보기
 * - 폼 유효성 검사
 * - 모임 생성 API 호출
 * - 엔터키 제출 지원
 */

// DOM 요소 참조
const coverImage = document.getElementById("coverImage")
const preview = document.getElementById("preview")
const form = document.getElementById("groupForm")
const errorMessage = document.getElementById("errorMessage")

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", () => {
    setupEventListeners()
    setupInputValidation()
    setupRadioValidation()
})

// 이벤트 리스너 설정
function setupEventListeners() {
    // 커버 이미지 변경 이벤트
    coverImage.addEventListener("change", handleCoverImageChange)

    // 폼 제출 이벤트
    form.addEventListener("submit", handleFormSubmit)

    // 엔터키 제출 이벤트
    document.addEventListener("keydown", handleEnterKeySubmit)
}

// 커버 이미지 변경 처리
function handleCoverImageChange() {
    const file = this.files[0]

    if (file) {
        // 파일 크기 검증 (예: 5MB 제한)
        if (file.size > 5 * 1024 * 1024) {
            showError("파일 크기는 5MB 이하여야 합니다.")
            this.value = ""
            return
        }

        // 파일 타입 검증
        if (!file.type.startsWith("image/")) {
            showError("이미지 파일만 업로드 가능합니다.")
            this.value = ""
            return
        }

        // 미리보기 표시
        const reader = new FileReader()
        reader.onload = (e) => {
            preview.src = e.target.result
            preview.style.display = "block"
        }
        reader.readAsDataURL(file)
    } else {
        // 파일이 선택되지 않은 경우 미리보기 숨김
        hidePreview()
    }
}

// 미리보기 숨김
function hidePreview() {
    preview.src = "#"
    preview.style.display = "none"
}

// 폼 제출 처리
async function handleFormSubmit(e) {
    e.preventDefault()
    clearError()

    // 폼 데이터 수집 및 검증
    const formData = collectFormData()
    if (!formData) {
        return // 검증 실패
    }

    // 제출 버튼 비활성화
    const submitBtn = form.querySelector('button[type="submit"]')
    setSubmitButtonState(submitBtn, true, "생성 중...")

    try {
        // API 요청
        const response = await submitGroupCreation(formData)

        if (response.ok) {
            const data = await response.json()
            if (data.result) {
                // 성공 시 메인 페이지로 이동
                showSuccess("모임이 성공적으로 생성되었습니다!")
                setTimeout(() => {
                    window.location.href = "/test/main.html"
                }, 1500)
            } else {
                throw new Error(data.message || "모임 생성에 실패했습니다.")
            }
        } else {
            const data = await response.json()
            throw new Error(data.message || "모임 생성 중 오류가 발생했습니다.")
        }
    } catch (error) {
        console.error("모임 생성 실패:", error)
        showError(error.message || "서버와 통신 중 문제가 발생했습니다.")
    } finally {
        // 제출 버튼 복원
        setSubmitButtonState(submitBtn, false, "모임 만들기")
    }
}

// 폼 데이터 수집 및 검증
function collectFormData() {
    const cover = coverImage.files[0]
    const name = document.getElementById("groupName").value.trim()
    const access = document.querySelector('input[name="visibility"]:checked')?.value
    const type = document.querySelector('input[name="type"]:checked')?.value
    const interest = document.querySelector('input[name="interest"]:checked')?.value

    // 필수 항목 검증
    if (!name) {
        showError("모임 이름을 입력해주세요.")
        document.getElementById("groupName").focus()
        return null
    }

    if (!access) {
        showError("공개 설정을 선택해주세요.")
        return null
    }

    if (!type) {
        showError("모임 유형을 선택해주세요.")
        return null
    }

    if (!interest) {
        showError("관심사를 선택해주세요.")
        return null
    }

    // FormData 생성
    const formData = new FormData()

    // JSON 요청 데이터
    const request = new Blob(
        [
            JSON.stringify({
                name,
                access,
                type,
                interest,
            }),
        ],
        { type: "application/json" },
    )

    formData.append("request", request)

    // 커버 이미지 추가 (선택사항)
    if (cover) {
        formData.append("coverImage", cover)
    }

    return formData
}

// 모임 생성 API 호출
async function submitGroupCreation(formData) {
    return await fetchWithAuth("/api/groups", {
        method: "POST",
        body: formData,
    })
}

// 제출 버튼 상태 설정
function setSubmitButtonState(button, disabled, text) {
    if (button) {
        button.disabled = disabled
        button.textContent = text
    }
}

// 엔터키 제출 처리
function handleEnterKeySubmit(e) {
    if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) {
        const submitBtn = form.querySelector('button[type="submit"]')
        if (submitBtn && !submitBtn.disabled) {
            form.dispatchEvent(new Event("submit"))
        }
    }
}

// 에러 메시지 표시
function showError(message) {
    errorMessage.textContent = message
    errorMessage.className = "error-message show"
    errorMessage.scrollIntoView({ behavior: "smooth", block: "nearest" })
}

// 성공 메시지 표시
function showSuccess(message) {
    errorMessage.textContent = message
    errorMessage.className = "success-message show"
}

// 에러 메시지 숨김
function clearError() {
    errorMessage.textContent = ""
    errorMessage.className = "error-message"
}

// 입력 필드 실시간 검증
function setupInputValidation() {
    const inputs = form.querySelectorAll("input[required]")

    inputs.forEach((input) => {
        input.addEventListener("blur", function () {
            if (!this.value.trim()) {
                this.classList.add("error")
            } else {
                this.classList.remove("error")
            }
        })

        input.addEventListener("input", function () {
            if (this.classList.contains("error") && this.value.trim()) {
                this.classList.remove("error")
            }
            // 에러 메시지가 표시된 상태에서 입력 시 숨김
            if (errorMessage.classList.contains("show")) {
                clearError()
            }
        })
    })
}

// 라디오 버튼 그룹 검증
function setupRadioValidation() {
    const radioGroups = ["visibility", "type", "interest"]

    radioGroups.forEach((groupName) => {
        const radios = document.querySelectorAll(`input[name="${groupName}"]`)
        radios.forEach((radio) => {
            radio.addEventListener("change", () => {
                // 선택 시 에러 메시지 숨김
                if (errorMessage.classList.contains("show")) {
                    clearError()
                }
            })
        })
    })
}
