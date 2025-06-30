/**
 * 모임 메인 페이지
 * - 공지사항 및 게시글 목록 표시
 * - 게시글 작성 권한 관리
 * - 게시글 미리보기 및 썸네일 처리
 * - 게시글 클릭 시 상세 페이지 이동
 */

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", async () => {
    // URL에서 그룹 ID 추출
    groupId = new URLSearchParams(window.location.search).get("id")

    if (!groupId) {
        console.error("그룹 ID를 찾을 수 없습니다.")
        return
    }

    // 권한 초기화 및 UI 업데이트
    await initializePermissions()

    // 데이터 로드
    loadNotices()
    loadPosts()
})

// 권한 초기화
async function initializePermissions() {
    try {
        const permissionsInitialized = await window.GroupPermissions.initialize(groupId)
        if (permissionsInitialized) {
            updateCreatePostButton()
        }
    } catch (error) {
        console.error("권한 초기화 실패:", error)
    }
}

// 게시글 작성 버튼 업데이트
function updateCreatePostButton() {
    const createPostBtn = document.getElementById("create-post-btn")
    if (createPostBtn) {
        createPostBtn.style.display = window.GroupPermissions.canCreatePost() ? "block" : "none"
    }
}

// 게시글 작성 페이지로 이동
function goToPostCreate() {
    location.href = `/test/group/post/create.html?id=${groupId}`
}

// HTML 콘텐츠에서 미리보기 텍스트와 썸네일 추출
function extractPostPreviewAndThumbnail(contentHtml) {
    const parser = new DOMParser()
    const doc = parser.parseFromString(contentHtml, "text/html")

    // 썸네일 URL 추출 (첫 번째 이미지 또는 비디오 포스터)
    let thumbnailUrl = null
    const firstImg = doc.querySelector("img[src]")
    if (firstImg) {
        thumbnailUrl = firstImg.getAttribute("src")
    } else {
        const firstVideo = doc.querySelector("video[poster]")
        if (firstVideo) {
            thumbnailUrl = firstVideo.getAttribute("poster")
        }
    }

    // 미디어 요소 제거 후 텍스트 추출
    doc.querySelectorAll("img, video, a").forEach((el) => el.remove())
    const rawText = doc.body.textContent.trim().replace(/\s+/g, " ")
    const textPreview = rawText.slice(0, 100)
    const hasMore = rawText.length > 100

    return { thumbnailUrl, textPreview, hasMore }
}

// 공지사항 미리보기 텍스트 추출
function extractNoticePreview(contentHtml) {
    const parser = new DOMParser()
    const doc = parser.parseFromString(contentHtml, "text/html")

    // 모든 HTML 요소 제거
    doc.querySelectorAll("img, video, a, br").forEach((el) => el.remove())
    const rawText = doc.body.textContent.trim().replace(/\s+/g, " ")
    const preview = rawText.slice(0, 30)
    const hasMore = rawText.length > 30

    return hasMore ? preview + "..." : preview
}

// 공지사항 카드 HTML 생성
function createNoticeCardHTML(notice) {
    const preview = extractNoticePreview(notice.content)

    return `
        <div class="notice-card"
            onclick="location.href='/test/group/post.html?groupId=${groupId}&postId=${notice.id}'"
            style="cursor:pointer;">
            ${preview}
        </div>
    `
}

// 공지사항 로드
async function loadNotices() {
    try {
        const response = await fetchWithAuth(`/api/groups/${groupId}/posts/notice`)
        const data = await response.json()
        const notices = data.result

        const container = document.getElementById("notice-list")
        const title = document.querySelector(".notice-preview h2")

        // 공지사항이 없는 경우 숨김 처리
        if (!Array.isArray(notices) || notices.length === 0) {
            if (title) title.style.display = "none"
            container.innerHTML = ""
            return
        }

        // 공지사항 카드 렌더링
        const noticesHTML = notices.map(createNoticeCardHTML).join("")
        container.innerHTML = noticesHTML

        if (title) title.style.display = "block"
    } catch (error) {
        console.error("공지사항 불러오기 실패:", error.message)
        showNoticeError()
    }
}

// 게시글 카드 HTML 생성
function createPostCardHTML(post) {
    const { thumbnailUrl, textPreview, hasMore } = extractPostPreviewAndThumbnail(post.content)

    return `
        <div class="post-card" onclick="location.href='/test/group/post.html?groupId=${groupId}&postId=${post.id}'">
            ${thumbnailUrl ? `<img src="${thumbnailUrl}" class="post-thumbnail" alt="썸네일">` : ""}
            <div class="post-content">
                <div class="post-meta">
                    <img src="${post.userImgUrl || "https://via.placeholder.com/24"}" alt="프로필">
                    ${post.userName} · ${formatPostDate(post.createdAt)}
                </div>
                <div class="post-text">
                    ${textPreview}${hasMore ? '<span class="more"> ...더보기</span>' : ""}
                </div>
                <div class="post-footer">댓글 ${post.commentsNum}개</div>
            </div>
        </div>
    `
}

// 게시글 목록 로드
async function loadPosts() {
    try {
        const response = await fetchWithAuth(`/api/groups/${groupId}/posts`)
        const data = await response.json()
        const posts = data.result

        const container = document.getElementById("post-list")

        // 게시글이 없는 경우
        if (!Array.isArray(posts) || posts.length === 0) {
            container.innerHTML = `<div style="color: #777; font-size: 15px;">등록된 게시글이 없습니다.</div>`
            return
        }

        // 게시글 카드 렌더링
        const postsHTML = posts.map(createPostCardHTML).join("")
        container.innerHTML = postsHTML
    } catch (error) {
        console.error("게시글 불러오기 실패:", error.message)
        showPostError()
    }
}

// 게시글 날짜 포맷팅
function formatPostDate(dateString) {
    return dateString.replace("T", " ").substring(0, 16)
}

// 공지사항 에러 표시
function showNoticeError() {
    const container = document.getElementById("notice-list")
    container.innerHTML = `<div class="error-message">공지사항을 불러올 수 없습니다.</div>`
}

// 게시글 에러 표시
function showPostError() {
    const container = document.getElementById("post-list")
    container.innerHTML = `<div class="error-message">게시글을 불러올 수 없습니다.</div>`
}

// 엔터키 이벤트 처리 (게시글 작성 버튼)
document.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && e.ctrlKey) {
        const createPostBtn = document.getElementById("create-post-btn")
        if (createPostBtn && createPostBtn.style.display !== "none") {
            goToPostCreate()
        }
    }
})
