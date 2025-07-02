/**
 * 게시글 파일 첨부 기능
 * - 이미지, 비디오, 일반 파일 업로드
 * - 파일 미리보기 및 관리
 * - 첨부파일 삭제 및 정리
 * - 업로드 진행상태 표시
 * - 파일 크기 및 타입 검증
 */

// 전역 변수
let selectedFile = null
let isModified = false

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", () => {
    setupFileEventListeners()
    setupModificationTracking()
})

// 파일 관련 이벤트 리스너 설정
function setupFileEventListeners() {
    const fileInput = document.getElementById("fileInput")
    const cancelBtn = document.querySelector(".btn-cancel")

    if (fileInput) {
        fileInput.addEventListener("change", handleFileSelection)
    }

    if (cancelBtn) {
        cancelBtn.addEventListener("click", handleCancelClick)
    }
}

// 수정사항 추적 설정
function setupModificationTracking() {
    // 입력 변경 감지
    document.addEventListener("input", markModified)

    // 파일 입력 변경 감지
    const fileInput = document.getElementById("fileInput")
    if (fileInput) {
        fileInput.addEventListener("change", markModified)
    }

    // 페이지 이탈 경고 설정
    window.addEventListener("beforeunload", beforeUnloadHandler)
}

// 파일 선택 처리
function handleFileSelection(e) {
    selectedFile = e.target.files[0]

    if (!selectedFile) return

    // 파일 검증
    if (!validateFile(selectedFile)) {
        resetFileSelection()
        return
    }

    // 미리보기 표시
    showFilePreview(selectedFile)

    // 모달 표시
    showFileModal()
}

// 파일 검증
function validateFile(file) {
    // 파일 크기 검증 (예: 50MB 제한)
    const maxSize = 50 * 1024 * 1024
    if (file.size > maxSize) {
        alert("파일 크기는 50MB 이하여야 합니다.")
        return false
    }

    // 파일 타입 검증 (필요시 추가)
    const allowedTypes = [
        "image/",
        "video/",
        "audio/",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument",
        "text/",
    ]

    const isAllowed = allowedTypes.some((type) => file.type.startsWith(type))
    if (!isAllowed) {
        alert("지원하지 않는 파일 형식입니다.")
        return false
    }

    return true
}

// 파일 미리보기 표시
function showFilePreview(file) {
    const modalPreview = document.getElementById("modalPreview")
    const url = URL.createObjectURL(file)

    if (file.type.startsWith("image/")) {
        modalPreview.innerHTML = `<img src="${url}" alt="미리보기" style="max-width: 100%; max-height: 300px; object-fit: contain;">`
    } else if (file.type.startsWith("video/")) {
        modalPreview.innerHTML = `<video src="${url}" controls style="max-width: 100%; max-height: 300px;"></video>`
    } else {
        modalPreview.innerHTML = `
      <div class="file-preview">
        <div class="file-icon">📄</div>
        <div class="file-info">
          <div class="file-name">${file.name}</div>
          <div class="file-size">${formatFileSize(file.size)}</div>
        </div>
      </div>
    `
    }
}

// 파일 모달 표시
function showFileModal() {
    const modal = document.getElementById("fileModal")
    if (modal) {
        modal.style.display = "flex"
    }
}

// 파일 첨부 취소
function cancelAttachFile() {
    hideFileModal()
    resetFileSelection()
    clearModalPreview()
}

// 파일 첨부 확인
async function confirmAttachFile() {
    if (!selectedFile) return

    try {
        // 업로드 시작
        showUploadProgress(true)

        // FormData 생성
        const formData = new FormData()
        formData.append("file", selectedFile)

        // API 요청
        const response = await fetchWithAuth(`/api/groups/${groupId}/post-files`, {
            method: "POST",
            body: formData,
        })

        const data = await response.json()

        if (!response.ok) {
            throw new Error(data.message || "업로드 실패")
        }

        // 성공 처리
        const { id, url, type, filename, thumbnailUrl } = data.result
        addMediaToContent(id, url, type, filename, thumbnailUrl)

        // 모달 닫기
        cancelAttachFile()

        console.log("파일이 성공적으로 첨부되었습니다.")
    } catch (error) {
        console.error("파일 업로드 실패:", error)
        alert(error.message || "파일 업로드 중 오류가 발생했습니다.")
    } finally {
        showUploadProgress(false)
    }
}

// 콘텐츠에 미디어 추가
function addMediaToContent(id, url, type, filename, thumbnailUrl) {
    const previewContent = document.getElementById("previewContent")
    let element

    if (type.startsWith("image/")) {
        element = createImageElement(id, url)
    } else if (type.startsWith("video/")) {
        element = createVideoElement(id, url, type, thumbnailUrl)
    } else {
        element = createFileElement(id, url, filename)
    }

    previewContent.insertAdjacentHTML("beforeend", element)
}

// 이미지 요소 생성
function createImageElement(id, url) {
    return `
    <div class="preview-media">
      <img data-id="${id}" src="${url}" alt="첨부 이미지" />
      <button type="button" class="delete-btn" onclick="removeMediaElement(this)">삭제</button>
    </div>
  `
}

// 비디오 요소 생성
function createVideoElement(id, url, type, thumbnailUrl) {
    const poster = thumbnailUrl ? `poster="${thumbnailUrl}"` : ""
    return `
    <div class="preview-media">
      <video data-id="${id}" controls ${poster}>
        <source src="${url}" type="${type}" />
      </video>
      <button type="button" class="delete-btn" onclick="removeMediaElement(this)">삭제</button>
    </div>
  `
}

// 파일 요소 생성
function createFileElement(id, url, filename) {
    return `
    <div class="preview-media">
      <a data-id="${id}" href="${url}" target="_blank" class="file-link">
        <span class="file-icon">📎</span>
        <span class="file-name">${filename}</span>
      </a>
      <button type="button" class="delete-btn" onclick="removeMediaElement(this)">삭제</button>
    </div>
  `
}

// 미디어 요소 제거
function removeMediaElement(button) {
    const mediaWrapper = button.closest(".preview-media")
    if (mediaWrapper) {
        mediaWrapper.remove()
        markModified()
    }
}

// 현재 첨부파일 ID 목록 반환
function getCurrentAttachmentIds() {
    const ids = new Set()
    const elements = document.querySelectorAll(
        "#previewContent img[data-id], #previewContent video[data-id], #previewContent a[data-id]",
    )

    elements.forEach((el) => {
        const id = el.getAttribute("data-id")
        if (id) ids.add(Number(id))
    })

    return Array.from(ids)
}

// 첨부파일 삭제 (고아 파일 정리)
async function deleteAttachments(ids) {
    if (!ids || ids.length === 0) return

    try {
        await fetchWithAuth("/api/attachments", {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(ids),
        })
        console.log("고아 파일 삭제 완료", ids)
    } catch (error) {
        console.warn("첨부파일 삭제 실패", error)
    }
}

// 수정사항 표시
function markModified() {
    isModified = true
}

// 페이지 이탈 경고 핸들러
function beforeUnloadHandler(e) {
    if (!isModified) return
    e.preventDefault()
    e.returnValue = "작성 중인 내용이 있습니다. 정말 나가시겠습니까?"
    return e.returnValue
}

// 취소 버튼 클릭 처리
async function handleCancelClick() {
    // 페이지 이탈 경고 제거
    window.removeEventListener("beforeunload", beforeUnloadHandler)

    const confirmed = confirm("게시글 작성을 취소하시겠습니까?")
    if (!confirmed) {
        // 취소하지 않으면 경고 다시 설정
        window.addEventListener("beforeunload", beforeUnloadHandler)
        return
    }

    // 고아 파일 정리
    await cleanupOrphanedFiles()

    // 메인 페이지로 이동
    window.location.href = `/test/group/main?id=${groupId}`
}

// 고아 파일 정리
async function cleanupOrphanedFiles() {
    const currentIds = getCurrentAttachmentIds()
    const idsToDelete = window.isEditMode ? currentIds.filter((id) => !initialAttachmentIds.includes(id)) : currentIds

    await deleteAttachments(idsToDelete)
}

// 업로드 진행상태 표시
function showUploadProgress(show) {
    const confirmBtn = document.querySelector("#fileModal .confirm-btn")
    const cancelBtn = document.querySelector("#fileModal .cancel-btn")

    if (confirmBtn) {
        confirmBtn.disabled = show
        confirmBtn.textContent = show ? "업로드 중..." : "첨부"
    }

    if (cancelBtn) {
        cancelBtn.disabled = show
    }
}

// 파일 모달 숨기기
function hideFileModal() {
    const modal = document.getElementById("fileModal")
    if (modal) {
        modal.style.display = "none"
    }
}

// 파일 선택 초기화
function resetFileSelection() {
    const fileInput = document.getElementById("fileInput")
    if (fileInput) {
        fileInput.value = ""
    }
    selectedFile = null
}

// 모달 미리보기 초기화
function clearModalPreview() {
    const modalPreview = document.getElementById("modalPreview")
    if (modalPreview) {
        modalPreview.innerHTML = ""
    }
}

// 파일 크기 포맷팅
function formatFileSize(bytes) {
    if (bytes === 0) return "0 B"
    const k = 1024
    const sizes = ["B", "KB", "MB", "GB"]
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return Number.parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i]
}

// 전역 함수로 노출 (다른 파일에서 사용)
window.getCurrentAttachmentIds = getCurrentAttachmentIds
window.deleteAttachments = deleteAttachments
window.markModified = markModified
