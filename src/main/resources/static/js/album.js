/**
 * 모임 사진첩 기능
 * - 이미지/비디오 파일 조회 및 표시
 * - 카테고리별 필터링 (전체, 사진, 영상)
 * - 미디어 모달 뷰어 (확대보기, 재생)
 * - 파일 다운로드 기능
 * - 반응형 그리드 레이아웃
 */

// 전역 변수
let allAttachments = []
let currentCategory = "all"
let currentAttachment = null

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", () => {
    // URL에서 그룹 ID 추출
    groupId = new URLSearchParams(window.location.search).get("id")

    if (!groupId) {
        showError("그룹 ID가 없습니다.")
        return
    }

    // 데이터 로드 및 이벤트 설정
    loadAlbumData()
    setupEventListeners()
})

// 이벤트 리스너 설정
function setupEventListeners() {
    const menuLinks = document.querySelectorAll(".photo-menu-link")
    const modal = document.getElementById("mediaModal")
    const closeModalBtn = document.getElementById("closeModalBtn")
    const downloadBtn = document.getElementById("downloadBtn")

    // 카테고리 메뉴 클릭 이벤트
    menuLinks.forEach((link) => {
        link.addEventListener("click", handleCategoryClick)
    })

    // 모달 관련 이벤트
    closeModalBtn.addEventListener("click", closeModal)
    modal.addEventListener("click", handleModalBackgroundClick)
    downloadBtn.addEventListener("click", handleDownload)

    // 키보드 이벤트 (ESC로 모달 닫기)
    document.addEventListener("keydown", handleKeyDown)
}

// 카테고리 클릭 처리
function handleCategoryClick(e) {
    e.preventDefault()
    const category = this.dataset.category

    if (category === currentCategory) return

    // 활성 메뉴 업데이트
    document.querySelectorAll(".photo-menu-link").forEach((l) => l.classList.remove("active"))
    this.classList.add("active")

    currentCategory = category
    updateCategoryTitle(category)
    filterAndRenderAlbum(category)
}

// 모달 배경 클릭 처리
function handleModalBackgroundClick(e) {
    if (e.target === document.getElementById("mediaModal")) {
        closeModal()
    }
}

// 키보드 이벤트 처리
function handleKeyDown(e) {
    const modal = document.getElementById("mediaModal")
    if (e.key === "Escape" && modal.classList.contains("active")) {
        closeModal()
    }
}

// 앨범 데이터 로드
async function loadAlbumData() {
    try {
        showLoading()
        const url = `/api/groups/${groupId}/albums`
        const response = await fetchWithAuth(url)

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`)
        }

        const data = await response.json()
        if (data.result) {
            allAttachments = data.result
            updateCounts()
            filterAndRenderAlbum(currentCategory)
        } else {
            throw new Error("데이터 형식이 올바르지 않습니다.")
        }
    } catch (error) {
        console.error("앨범 데이터 로드 실패:", error)
        showError("사진을 불러오는데 실패했습니다.")
    }
}

// 카테고리별 필터링 및 렌더링
function filterAndRenderAlbum(category) {
    let filteredAttachments

    switch (category) {
        case "image":
            filteredAttachments = allAttachments.filter((a) => a.type.startsWith("image/"))
            break
        case "video":
            filteredAttachments = allAttachments.filter((a) => a.type.startsWith("video/"))
            break
        default:
            filteredAttachments = allAttachments
    }

    renderAlbum(filteredAttachments)
}

// 앨범 아이템 HTML 생성
function createAlbumItemHTML(attachment) {
    const isVideo = attachment.type.startsWith("video/")
    const imageUrl = isVideo && attachment.thumbnailUrl ? attachment.thumbnailUrl : attachment.url
    const fileSize = formatFileSize(attachment.size)
    const uploadDate = formatDate(attachment.uploadedAt)

    return `
        <div class="photo-item" data-id="${attachment.id}" data-type="${isVideo ? "video" : "image"}">
            <img src="${imageUrl}" alt="${attachment.filename}" loading="lazy">
            ${
        isVideo
            ? `
                <div class="video-indicator">🎬 영상</div>
                <button class="video-play-btn">▶</button>
            `
            : ""
    }
            <div class="photo-overlay">
                <div class="photo-info">
                    <div>${attachment.filename}</div>
                    <div class="photo-date">${uploadDate}</div>
                    <div class="photo-size">${fileSize}</div>
                </div>
            </div>
        </div>
    `
}

// 앨범 렌더링
function renderAlbum(attachments) {
    const photoGrid = document.getElementById("photoGrid")

    if (!attachments || attachments.length === 0) {
        showEmptyState()
        return
    }

    // HTML 생성 및 삽입
    const html = attachments.map(createAlbumItemHTML).join("")
    photoGrid.innerHTML = html

    // 클릭 이벤트 등록
    photoGrid.querySelectorAll(".photo-item").forEach((item) => {
        item.addEventListener("click", handlePhotoItemClick)
    })

    // 애니메이션 효과 적용
    applyFadeInAnimation(photoGrid)
}

// 사진 아이템 클릭 처리
function handlePhotoItemClick() {
    const attachmentId = this.dataset.id
    const attachment = allAttachments.find((a) => a.id == attachmentId)
    if (attachment) {
        openMediaModal(attachment)
    }
}

// 페이드인 애니메이션 적용
function applyFadeInAnimation(photoGrid) {
    setTimeout(() => {
        photoGrid.querySelectorAll(".photo-item").forEach((item, index) => {
            item.style.opacity = "0"
            item.style.transform = "translateY(20px)"
            setTimeout(() => {
                item.style.transition = "all 0.3s ease"
                item.style.opacity = "1"
                item.style.transform = "translateY(0)"
            }, index * 50)
        })
    }, 100)
}

// 미디어 모달 열기
function openMediaModal(attachment) {
    currentAttachment = attachment
    const modal = document.getElementById("mediaModal")
    const modalTitle = document.getElementById("modalTitle")
    const modalMediaContainer = document.getElementById("modalMediaContainer")
    const modalInfoGrid = document.getElementById("modalInfoGrid")
    const downloadBtn = document.getElementById("downloadBtn")

    // 모달 헤더 설정
    modalTitle.textContent = attachment.filename
    downloadBtn.style.display = "flex"
    downloadBtn.disabled = false
    downloadBtn.innerHTML = "📥 다운로드"

    // 미디어 컨테이너 초기화
    modalMediaContainer.innerHTML = ""

    // 미디어 타입에 따른 처리
    const isVideo = attachment.type.startsWith("video/")
    if (isVideo) {
        createVideoElement(attachment, modalMediaContainer)
    } else {
        createImageElement(attachment, modalMediaContainer)
    }

    // 파일 정보 표시
    displayFileInfo(attachment, modalInfoGrid)

    // 모달 표시
    modal.classList.add("active")
}

// 비디오 엘리먼트 생성
function createVideoElement(attachment, container) {
    const video = document.createElement("video")
    video.src = attachment.url
    video.controls = true
    video.autoplay = false
    video.preload = "metadata"

    video.onerror = () => {
        container.innerHTML = createErrorMessage("영상을 불러올 수 없습니다")
    }

    container.appendChild(video)
}

// 이미지 엘리먼트 생성
function createImageElement(attachment, container) {
    const img = document.createElement("img")
    img.src = attachment.url
    img.alt = attachment.filename

    img.onerror = () => {
        container.innerHTML = createErrorMessage("이미지를 불러올 수 없습니다")
    }

    container.appendChild(img)
}

// 에러 메시지 HTML 생성
function createErrorMessage(message) {
    return `
        <div style="color: #64748b; text-align: center; padding: 40px;">
            <div style="font-size: 48px; margin-bottom: 16px;">⚠️</div>
            <div>${message}</div>
        </div>
    `
}

// 파일 정보 표시
function displayFileInfo(attachment, container) {
    const uploadDate = formatDate(attachment.uploadedAt)
    const fileSize = formatFileSize(attachment.size)
    const fileType = attachment.type.startsWith("video/") ? "영상" : "사진"

    container.innerHTML = `
        <div class="modal-info-item">
            <div class="modal-info-label">파일명</div>
            <div class="modal-info-value">${attachment.filename}</div>
        </div>
        <div class="modal-info-item">
            <div class="modal-info-label">타입</div>
            <div class="modal-info-value">${fileType}</div>
        </div>
        <div class="modal-info-item">
            <div class="modal-info-label">크기</div>
            <div class="modal-info-value">${fileSize}</div>
        </div>
        <div class="modal-info-item">
            <div class="modal-info-label">업로드 날짜</div>
            <div class="modal-info-value">${uploadDate}</div>
        </div>
    `
}

// 모달 닫기
function closeModal() {
    const modal = document.getElementById("mediaModal")
    const modalMediaContainer = document.getElementById("modalMediaContainer")

    // 비디오 재생 중지
    const video = modalMediaContainer.querySelector("video")
    if (video) {
        video.pause()
        video.currentTime = 0
    }

    modal.classList.remove("active")
    currentAttachment = null
}

// 다운로드 처리
async function handleDownload() {
    if (!currentAttachment) return

    const downloadBtn = document.getElementById("downloadBtn")
    const originalText = downloadBtn.innerHTML

    try {
        // 다운로드 버튼 상태 변경
        setDownloadButtonState(downloadBtn, true, "⏳ 준비 중...")

        // 다운로드 URL 요청
        const response = await fetchWithAuth(`/api/attachments/${currentAttachment.id}/download`)

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`)
        }

        const data = await response.json()

        if (data.result) {
            // 파일 다운로드 실행
            executeDownload(data.result, currentAttachment.filename)

            // 성공 상태 표시
            setDownloadButtonState(downloadBtn, true, "✅ 완료")
            setTimeout(() => {
                setDownloadButtonState(downloadBtn, false, originalText)
            }, 2000)
        } else {
            throw new Error("다운로드 URL을 받을 수 없습니다.")
        }
    } catch (error) {
        console.error("다운로드 실패:", error)
        alert("다운로드에 실패했습니다. 다시 시도해주세요.")
        setDownloadButtonState(downloadBtn, false, originalText)
    }
}

// 다운로드 버튼 상태 설정
function setDownloadButtonState(button, disabled, text) {
    button.disabled = disabled
    button.innerHTML = text
}

// 파일 다운로드 실행
function executeDownload(url, filename) {
    const link = document.createElement("a")
    link.href = url
    link.download = filename
    link.style.display = "none"
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
}

// 카운트 업데이트
function updateCounts() {
    const totalCount = allAttachments.length
    const photoCount = allAttachments.filter((a) => a.type.startsWith("image/")).length
    const videoCount = allAttachments.filter((a) => a.type.startsWith("video/")).length

    document.getElementById("countAll").textContent = totalCount
    document.getElementById("countPhotos").textContent = photoCount
    document.getElementById("countVideos").textContent = videoCount
}

// 카테고리 제목 업데이트
function updateCategoryTitle(category) {
    const titles = {
        all: "전체 미디어",
        image: "사진",
        video: "영상",
    }
    document.getElementById("categoryTitle").textContent = titles[category] || "전체 미디어"
}

// 로딩 상태 표시
function showLoading() {
    const photoGrid = document.getElementById("photoGrid")
    photoGrid.innerHTML = `
        <div class="loading-container">
            <div class="loading-spinner"></div>
            <div class="loading-text">사진을 불러오는 중...</div>
        </div>
    `
}

// 빈 상태 표시
function showEmptyState() {
    const photoGrid = document.getElementById("photoGrid")
    photoGrid.innerHTML = `
        <div class="empty-state">
            <div class="empty-state-icon">📷</div>
            <p>등록된 파일이 없습니다.</p>
        </div>
    `
}

// 에러 상태 표시
function showError(message) {
    const photoGrid = document.getElementById("photoGrid")
    photoGrid.innerHTML = `
        <div class="error-state">
            <div class="error-state-icon">⚠️</div>
            <h5>오류가 발생했습니다</h5>
            <p>${message}</p>
            <button class="retry-btn" onclick="loadAlbumData()">
                다시 시도
            </button>
        </div>
    `
}

// 파일 크기 포맷팅
function formatFileSize(bytes) {
    if (bytes === 0) return "0 B"
    const k = 1024
    const sizes = ["B", "KB", "MB", "GB"]
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return Number.parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i]
}

// 날짜 포맷팅
function formatDate(dateString) {
    const date = new Date(dateString)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, "0")
    const day = String(date.getDate()).padStart(2, "0")
    return `${year}.${month}.${day}`
}
