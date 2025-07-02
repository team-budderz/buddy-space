/**
 * 게시글 수정 페이지
 * - 기존 게시글 내용 로드 및 편집
 * - 미디어 파일 래핑 및 삭제 기능
 * - 수정 내용 저장 및 검증
 * - 페이지 이탈 시 변경사항 경고
 */

// 전역 변수
let postId = null
let initialAttachmentIds = []

// 편집 모드 플래그
window.isEditMode = true

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", async () => {
    // URL 파라미터 추출
    groupId = new URLSearchParams(location.search).get("groupId")
    postId = new URLSearchParams(location.search).get("postId")

    if (!groupId || !postId) {
        alert("잘못된 접근입니다.")
        return
    }

    try {
        // 게시글 데이터 로드
        await loadPostForEdit()

        // 이벤트 리스너 설정
        setupEventListeners()
    } catch (error) {
        console.error("게시글 수정 페이지 초기화 실패:", error)
        alert("게시글 정보를 불러오는 중 오류가 발생했습니다.")
    }
})

// 게시글 데이터 로드
async function loadPostForEdit() {
    const response = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}`)
    const data = await response.json()

    if (!response.ok) {
        throw new Error(data.message || "게시글 조회 실패")
    }

    const post = data.result

    // 게시글 내용 설정
    document.getElementById("previewContent").innerHTML = post.renderedContent || ""
    document.getElementById("noticeCheck").checked = post.isNotice || false

    // 기존 미디어 래핑
    wrapExistingMedia()

    // 초기 첨부파일 ID 저장
    setInitialAttachmentIds()
}

// 기존 미디어 요소 래핑
function wrapExistingMedia() {
    const container = document.getElementById("previewContent")

    container.querySelectorAll("img, video, a").forEach((element) => {
        // 이미 래핑된 요소는 건너뛰기
        if (element.closest(".preview-media")) return

        // 래퍼 생성
        const wrapper = createMediaWrapper(element)

        // 요소 교체
        replaceElementWithWrapper(element, wrapper)
    })
}

// 미디어 래퍼 생성
function createMediaWrapper(originalElement) {
    const wrapper = document.createElement("div")
    wrapper.className = "preview-media"

    // 원본 요소 복제
    const clonedElement = originalElement.cloneNode(true)

    // 삭제 버튼 생성
    const deleteBtn = createDeleteButton(wrapper)

    wrapper.appendChild(clonedElement)
    wrapper.appendChild(deleteBtn)

    return wrapper
}

// 삭제 버튼 생성
function createDeleteButton(wrapper) {
    const deleteBtn = document.createElement("button")
    deleteBtn.className = "delete-btn"
    deleteBtn.textContent = "삭제"
    deleteBtn.setAttribute("type", "button")
    deleteBtn.setAttribute("contenteditable", "false")

    deleteBtn.onclick = () => {
        wrapper.remove()
        markModified()
    }

    return deleteBtn
}

// 요소를 래퍼로 교체
function replaceElementWithWrapper(element, wrapper) {
    const currentParent = element.parentNode

    // 부모가 DIV나 P 태그이고 자식이 하나뿐인 경우
    if (["DIV", "P"].includes(currentParent.tagName) && currentParent.childNodes.length === 1) {
        currentParent.replaceWith(wrapper)
    } else {
        element.replaceWith(wrapper)
    }
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 페이지 이탈 경고
    window.addEventListener("beforeunload", beforeUnloadHandler)

    // 키보드 단축키
    document.addEventListener("keydown", handleKeyboardShortcuts)
}

// 키보드 단축키 처리
function handleKeyboardShortcuts(e) {
    // Ctrl+Enter로 수정 완료
    if (e.ctrlKey && e.key === "Enter") {
        const updateBtn = document.querySelector(".btn-update")
        if (updateBtn && !updateBtn.disabled) {
            updatePost()
        }
    }

    // ESC로 수정 취소
    if (e.key === "Escape") {
        const cancelBtn = document.querySelector(".btn-cancel")
        if (cancelBtn) {
            cancelBtn.click()
        }
    }
}

// 초기 첨부파일 ID 설정
function setInitialAttachmentIds() {
    initialAttachmentIds = getCurrentAttachmentIds()
}

// 게시글 수정 처리
async function updatePost() {
    const updateBtn = document.querySelector(".btn-update")

    try {
        // 페이지 이탈 경고 제거
        window.removeEventListener("beforeunload", beforeUnloadHandler)

        // 버튼 상태 변경
        setUpdateButtonState(updateBtn, true, "수정 중...")

        // 콘텐츠 정리
        const cleanedContent = cleanPostContent()

        // 수정 데이터 준비
        const postData = {
            content: cleanedContent,
            isNotice: document.getElementById("noticeCheck").checked,
            reserveAt: null,
        }

        // API 요청
        const response = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(postData),
        })

        const data = await response.json()

        if (!response.ok) {
            throw new Error(data.message || "수정 실패")
        }

        // 성공 처리
        alert("게시글이 수정되었습니다.")

        setTimeout(() => {
            location.href = `/test/group/post?groupId=${groupId}&postId=${postId}`
        }, 1000)
    } catch (error) {
        console.error("게시글 수정 실패:", error)
        alert(error.message || "게시글 수정 중 오류가 발생했습니다.")

        // 버튼 상태 복원
        setUpdateButtonState(updateBtn, false, "수정 완료")

        // 페이지 이탈 경고 다시 설정
        window.addEventListener("beforeunload", beforeUnloadHandler)
    }
}

// 게시글 콘텐츠 정리
function cleanPostContent() {
    const previewDiv = document.getElementById("previewContent")
    const saveDiv = document.getElementById("saveContent")

    // 콘텐츠 복제
    const cloned = previewDiv.cloneNode(true)

    // 미디어 래퍼 정리
    cloned.querySelectorAll(".preview-media").forEach((wrapper) => {
        const media = wrapper.querySelector("img, video, a")
        if (!media) return

        // 삭제 버튼 제거
        const deleteBtn = wrapper.querySelector(".delete-btn")
        if (deleteBtn) deleteBtn.remove()

        // 래퍼 제거하고 미디어만 유지
        wrapper.replaceWith(media)
    })

    // 정리된 HTML 저장
    saveDiv.innerHTML = cloned.innerHTML
    return saveDiv.innerHTML
}

// 수정 버튼 상태 설정
function setUpdateButtonState(button, disabled, text) {
    if (button) {
        button.disabled = disabled
        button.textContent = text
    }
}

// 변경사항 표시
function markModified() {
    // post-file.js의 markModified 함수 호출
    if (typeof window.markModified === "function") {
        window.markModified()
    }
}

// 현재 첨부파일 ID 목록 반환
function getCurrentAttachmentIds() {
    // post-file.js의 getCurrentAttachmentIds 함수 호출
    if (typeof window.getCurrentAttachmentIds === "function") {
        return window.getCurrentAttachmentIds()
    }
    return []
}

// 페이지 이탈 경고 핸들러
function beforeUnloadHandler(e) {
    e.preventDefault()
    e.returnValue = "수정 중인 내용이 있습니다. 정말 나가시겠습니까?"
    return e.returnValue
}
