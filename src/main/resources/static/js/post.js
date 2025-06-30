/**
 * 게시글 상세 보기 페이지
 * - 게시글 내용 및 미디어 표시
 * - 댓글 및 대댓글 시스템
 * - 이미지 확대 보기 모달
 * - 게시글 수정/삭제 권한 관리
 * - 미디어 에러 처리
 */

// 전역 변수
let postId = null

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", async () => {
    // URL 파라미터 추출
    groupId = new URLSearchParams(location.search).get("groupId")
    postId = new URLSearchParams(location.search).get("postId")

    if (!groupId || !postId) {
        alert("잘못된 접근입니다.")
        return
    }

    // 사용자 정보 대기
    await waitForUserInfo()

    // 게시글 로드
    await loadPost(window.loggedInUser)

    setupEventListeners()
})

// 사용자 정보 로드 대기
async function waitForUserInfo() {
    while (!window.loggedInUser) {
        await new Promise((resolve) => setTimeout(resolve, 50))
    }
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // ESC 키로 이미지 모달 닫기
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            const modal = document.getElementById("image-modal")
            if (modal) {
                modal.click()
            }
        }
    })
}

// 게시글 로드
async function loadPost(loginUser) {
    try {
        // 권한 데이터 초기화
        const permissionsInitialized = await window.GroupPermissions.initialize(groupId)
        if (!permissionsInitialized) {
            console.error("권한 정보를 불러올 수 없습니다.")
        }

        // 게시글 데이터 로드
        const response = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}`)
        const data = await response.json()

        if (!response.ok) {
            throw new Error(data.message || "게시글을 불러올 수 없습니다.")
        }

        const post = data.result

        // 게시글 정보 표시
        displayPostInfo(post)

        // 게시글 콘텐츠 처리
        processPostContent(post.renderedContent)

        // 권한 기반 액션 버튼 표시
        updateActionButtons(post, loginUser)

        // 댓글 렌더링
        renderComments(post.comments, loginUser)

        // 댓글 수 업데이트
        updateCommentCount(post.commentNum)
    } catch (error) {
        console.error("게시글 로드 실패:", error)
        alert(error.message || "게시글을 불러오는데 실패했습니다.")
    }
}

// 게시글 정보 표시
function displayPostInfo(post) {
    updateElement("user-img", post.userImgUrl || "https://via.placeholder.com/48", "src")
    updateElement("user-name", post.userName)
    updateElement("created-at", formatDateTime(post.createdAt))
}

// 게시글 콘텐츠 처리
function processPostContent(content) {
    const contentElement = document.getElementById("post-content")
    contentElement.innerHTML = content

    // 이미지 처리
    processImages(contentElement)

    // 비디오 처리
    processVideos(contentElement)
}

// 이미지 처리 및 이벤트 추가
function processImages(container) {
    container.querySelectorAll("img").forEach((img) => {
        // 클릭 이벤트 추가 (확대 보기)
        img.addEventListener("click", function () {
            openImageModal(this.src, this.alt)
        })

        // 로딩 에러 처리
        img.addEventListener("error", function () {
            this.style.cssText = `
        background: #f1f5f9;
        display: flex;
        align-items: center;
        justify-content: center;
        min-height: 200px;
        border: 2px dashed #d1d5db;
        border-radius: 8px;
      `
            this.innerHTML = '<div style="color: #9ca3af; font-size: 14px;">이미지를 불러올 수 없습니다</div>'
        })

        // 로딩 상태 표시
        img.addEventListener("load", function () {
            this.style.opacity = "1"
        })

        img.style.opacity = "0"
        img.style.transition = "opacity 0.3s ease"
    })
}

// 비디오 처리
function processVideos(container) {
    container.querySelectorAll("video").forEach((video) => {
        video.setAttribute("controls", "true")
        video.setAttribute("preload", "metadata")

        // 비디오 로딩 에러 처리
        video.addEventListener("error", function () {
            const errorDiv = document.createElement("div")
            errorDiv.style.cssText = `
        background: #f1f5f9;
        border: 2px dashed #d1d5db;
        border-radius: 8px;
        padding: 24px;
        text-align: center;
        color: #9ca3af;
        font-size: 14px;
        margin: 16px 0;
      `
            errorDiv.innerHTML = "🎬 비디오를 불러올 수 없습니다"
            this.parentNode.replaceChild(errorDiv, this)
        })
    })
}

// 이미지 모달 열기
function openImageModal(src, alt) {
    // 기존 모달 제거
    const existingModal = document.getElementById("image-modal")
    if (existingModal) {
        existingModal.remove()
    }

    // 모달 생성
    const modal = createImageModal(src, alt)
    document.body.appendChild(modal)

    // 애니메이션 적용
    setTimeout(() => {
        modal.classList.add("show")
    }, 10)

    // 모달 닫기 이벤트
    modal.addEventListener("click", closeImageModal)
}

// 이미지 모달 생성
function createImageModal(src, alt) {
    const modal = document.createElement("div")
    modal.id = "image-modal"
    modal.className = "image-modal"
    modal.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.9);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 10000;
    cursor: pointer;
    opacity: 0;
    transition: opacity 0.3s ease;
  `

    const img = document.createElement("img")
    img.src = src
    img.alt = alt
    img.style.cssText = `
    max-width: 90%;
    max-height: 90%;
    object-fit: contain;
    border-radius: 8px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
    transform: scale(0.8);
    transition: transform 0.3s ease;
  `

    modal.appendChild(img)

    // 이미지 로드 후 애니메이션
    img.addEventListener("load", () => {
        img.style.transform = "scale(1)"
    })

    return modal
}

// 이미지 모달 닫기
function closeImageModal(e) {
    const modal = e.currentTarget
    modal.style.opacity = "0"

    setTimeout(() => {
        if (modal.parentNode) {
            modal.parentNode.removeChild(modal)
        }
    }, 300)
}

// 액션 버튼 업데이트
function updateActionButtons(post, loginUser) {
    const isAuthor = post.userId === loginUser.id
    const canDelete = isAuthor || window.GroupPermissions.hasPermission("DELETE_POST")

    const actionsContainer = document.getElementById("post-actions")

    if (isAuthor || canDelete) {
        actionsContainer.style.display = "flex"

        // 수정 버튼 (작성자만)
        const editBtn = actionsContainer.querySelector(".edit-btn")
        if (editBtn) {
            editBtn.style.display = isAuthor ? "inline-block" : "none"
        }

        // 삭제 버튼 (작성자 또는 삭제 권한 있는 사용자)
        const deleteBtn = actionsContainer.querySelector(".delete-btn")
        if (deleteBtn) {
            deleteBtn.style.display = canDelete ? "inline-block" : "none"
        }
    } else {
        actionsContainer.style.display = "none"
    }
}

// 댓글 렌더링
function renderComments(comments, loginUser) {
    const container = document.getElementById("comment-list")
    container.innerHTML = ""

    comments.forEach((comment) => {
        const commentElement = createCommentElement(comment, loginUser)
        container.appendChild(commentElement)

        // 대댓글이 있으면 로드
        if (comment.commentNum > 0) {
            loadReplies(comment.commentId, loginUser)
        }
    })
}

// 댓글 요소 생성
function createCommentElement(comment, loginUser) {
    const isAuthor = comment.userId === loginUser.id
    const commentEl = document.createElement("div")
    commentEl.className = "comment"

    commentEl.innerHTML = `
    <div class="comment-author">
      <div class="comment-info">
        <img src="${comment.userImgUrl || "https://via.placeholder.com/32"}" alt="${comment.userName}">
        <strong>${comment.userName}</strong>
        <span class="comment-meta">${formatDateTime(comment.createdAt)}</span>
      </div>
      <div class="comment-buttons">
        ${isAuthor ? '<button onclick="editComment(this)">수정</button><button onclick="deleteComment(this)">삭제</button>' : ""}
      </div>
    </div>
    <div class="comment-content">${comment.content}</div>
    <div class="replies" id="replies-${comment.commentId}"></div>
  `

    return commentEl
}

// 대댓글 로드
async function loadReplies(commentId, loginUser) {
    try {
        const container = document.getElementById(`replies-${commentId}`)
        if (!container) return

        const response = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}/comments/${commentId}`)
        const data = await response.json()

        if (!response.ok) {
            throw new Error(data.message || "대댓글 로드 실패")
        }

        const replies = data.result || []

        if (replies.length > 0) {
            addToggleButton(container, replies, loginUser, false)
        }
    } catch (error) {
        console.error("대댓글 로드 실패:", error)
    }
}

// 대댓글 토글 버튼 추가
function addToggleButton(container, replies, loginUser, isExpanded) {
    // 기존 버튼 제거
    const existingBtn = container.querySelector(".toggle-replies-btn")
    if (existingBtn) {
        existingBtn.remove()
    }

    const toggleBtn = document.createElement("button")
    toggleBtn.className = "more-replies-btn toggle-replies-btn"

    if (isExpanded) {
        // 접기 버튼
        toggleBtn.textContent = "접기"
        toggleBtn.onclick = () => {
            // 모든 대댓글 제거
            container.querySelectorAll(".reply").forEach((reply) => reply.remove())
            addToggleButton(container, replies, loginUser, false)
        }
    } else {
        // 펼치기 버튼
        toggleBtn.textContent = `댓글 ${replies.length}개`
        toggleBtn.onclick = () => {
            // 처음 3개 대댓글 표시
            const initialReplies = replies.slice(0, 3)
            initialReplies.forEach((reply) => {
                const replyElement = createReplyElement(reply, loginUser)
                container.appendChild(replyElement)
            })

            // 더 많은 대댓글이 있으면 더보기 버튼, 아니면 접기 버튼
            if (replies.length > 3) {
                addMoreButton(container, replies, loginUser)
            } else {
                addToggleButton(container, replies, loginUser, true)
            }
        }
    }

    container.appendChild(toggleBtn)
}

// 더보기 버튼 추가
function addMoreButton(container, replies, loginUser) {
    const existingBtn = container.querySelector(".toggle-replies-btn")
    if (existingBtn) {
        existingBtn.remove()
    }

    const moreBtn = document.createElement("button")
    moreBtn.className = "more-replies-btn toggle-replies-btn"
    moreBtn.textContent = "더보기"

    moreBtn.onclick = () => {
        // 나머지 대댓글 추가
        const remainingReplies = replies.slice(3)
        remainingReplies.forEach((reply) => {
            const replyElement = createReplyElement(reply, loginUser)
            container.appendChild(replyElement)
        })

        // 접기 버튼으로 변경
        addToggleButton(container, replies, loginUser, true)
    }

    container.appendChild(moreBtn)
}

// 대댓글 요소 생성
function createReplyElement(reply, loginUser) {
    const isAuthor = reply.userId === loginUser.id
    const replyDiv = document.createElement("div")
    replyDiv.className = "reply"
    replyDiv.setAttribute("data-reply-id", reply.id)

    replyDiv.innerHTML = `
    <div class="reply-header">
      <img src="${reply.userImgUrl || "https://via.placeholder.com/28"}" alt="${reply.userName}">
      <strong>${reply.userName}</strong>
      <span style="font-size:13px;color:#666;">${formatDateTime(reply.createdAt)}</span>
      <div class="reply-buttons">
        ${isAuthor ? '<button onclick="editReply(this)">수정</button><button onclick="deleteReply(this)">삭제</button>' : ""}
      </div>
    </div>
    <div class="reply-content">${reply.content}</div>
  `

    return replyDiv
}

// 게시글 수정 페이지로 이동
function editPost() {
    location.href = `/test/group/post/update.html?groupId=${groupId}&postId=${postId}`
}

// 게시글 삭제
async function deletePost() {
    if (!confirm("정말 삭제하시겠습니까?")) return

    try {
        const response = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}`, {
            method: "DELETE",
        })

        if (response.ok) {
            const data = await response.json()
            alert(data.result || "게시글이 삭제되었습니다.")

            setTimeout(() => {
                window.location.href = `/test/group/main.html?id=${groupId}`
            }, 1000)
        } else {
            const data = await response.json()
            throw new Error(data.message || "삭제에 실패했습니다.")
        }
    } catch (error) {
        console.error("게시글 삭제 실패:", error)
        alert(error.message || "삭제 중 오류가 발생했습니다.")
    }
}

// 댓글 수 업데이트
function updateCommentCount(count) {
    const countElement = document.getElementById("comment-count")
    if (countElement) {
        countElement.textContent = `댓글 ${count}개`
    }
}

// 유틸리티 함수들
function updateElement(id, value, attribute = "textContent") {
    const element = document.getElementById(id)
    if (element) {
        if (attribute === "textContent") {
            element.textContent = value
        } else {
            element.setAttribute(attribute, value)
        }
    }
}

function formatDateTime(dateString) {
    return dateString.replace("T", " ").substring(0, 16)
}

// 댓글 관련 함수들 (향후 구현)
function editComment(button) {
    // 댓글 수정 기능 구현
    console.log("댓글 수정 기능 준비 중")
}

function deleteComment(button) {
    // 댓글 삭제 기능 구현
    console.log("댓글 삭제 기능 준비 중")
}

function editReply(button) {
    // 대댓글 수정 기능 구현
    console.log("대댓글 수정 기능 준비 중")
}

function deleteReply(button) {
    // 대댓글 삭제 기능 구현
    console.log("대댓글 삭제 기능 준비 중")
}
