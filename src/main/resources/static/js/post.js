const groupId = new URLSearchParams(location.search).get("groupId")
const postId = new URLSearchParams(location.search).get("postId")

async function loadPost(loginUser) {
    const res = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}`)
    const data = await res.json()
    const post = data.result

    document.getElementById("user-img").src = post.userImgUrl || "https://via.placeholder.com/48"
    document.getElementById("user-name").textContent = post.userName
    document.getElementById("created-at").textContent = post.createdAt.replace("T", " ").substring(0, 16)
    document.getElementById("post-content").innerHTML = post.renderedContent

    // 개선된 미디어 스타일링 - CSS에서 처리하도록 변경
    const contentElement = document.getElementById("post-content")

    // 이미지에 클릭 이벤트 추가 (확대 보기)
    contentElement.querySelectorAll("img").forEach((img) => {
        img.addEventListener("click", function () {
            openImageModal(this.src, this.alt)
        })

        // 로딩 에러 처리
        img.addEventListener("error", function () {
            this.style.background = "#f1f5f9"
            this.style.display = "flex"
            this.style.alignItems = "center"
            this.style.justifyContent = "center"
            this.innerHTML = '<div style="color: #9ca3af; font-size: 14px;">이미지를 불러올 수 없습니다</div>'
        })
    })

    // 비디오 개선
    contentElement.querySelectorAll("video").forEach((video) => {
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

    const isAuthor = post.userId === loginUser.id
    const permissionRes = await fetchWithAuth(`/api/groups/${groupId}/permissions`)
    const permissionData = await permissionRes.json()
    const deletePermission = permissionData.result.permissions.find((p) => p.type === "DELETE_POST")

    let canDelete = false
    if (deletePermission?.role === "LEADER") {
        canDelete = isAuthor || loginUser.role === "LEADER"
    } else if (deletePermission?.role === "SUB_LEADER") {
        canDelete = isAuthor || loginUser.role === "SUB_LEADER" || loginUser.role === "LEADER"
    } else if (deletePermission?.role === "MEMBER") {
        canDelete = isAuthor
    }

    if (isAuthor || canDelete) {
        const actions = document.getElementById("post-actions")
        actions.style.display = "flex"
        if (!isAuthor) actions.querySelector(".edit-btn").style.display = "none"
        if (!canDelete) actions.querySelector(".delete-btn").style.display = "none"
    }

    renderComments(post.comments, loginUser)
    document.getElementById("comment-count").textContent = `댓글 ${post.commentNum}개`
}

// 이미지 모달 기능 추가
function openImageModal(src, alt) {
    // 기존 모달이 있으면 제거
    const existingModal = document.getElementById("image-modal")
    if (existingModal) {
        existingModal.remove()
    }

    // 모달 생성
    const modal = document.createElement("div")
    modal.id = "image-modal"
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
        animation: fadeIn 0.3s ease-out;
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
        animation: zoomIn 0.3s ease-out;
    `

    modal.appendChild(img)
    document.body.appendChild(modal)

    // 모달 닫기
    modal.addEventListener("click", () => {
        modal.style.animation = "fadeOut 0.3s ease-out"
        setTimeout(() => modal.remove(), 300)
    })

    // ESC 키로 닫기
    const handleEsc = (e) => {
        if (e.key === "Escape") {
            modal.click()
            document.removeEventListener("keydown", handleEsc)
        }
    }
    document.addEventListener("keydown", handleEsc)

    // 애니메이션 CSS 추가
    if (!document.getElementById("modal-animations")) {
        const style = document.createElement("style")
        style.id = "modal-animations"
        style.textContent = `
            @keyframes fadeIn {
                from { opacity: 0; }
                to { opacity: 1; }
            }
            @keyframes fadeOut {
                from { opacity: 1; }
                to { opacity: 0; }
            }
            @keyframes zoomIn {
                from { transform: scale(0.8); opacity: 0; }
                to { transform: scale(1); opacity: 1; }
            }
        `
        document.head.appendChild(style)
    }
}

function renderComments(comments, loginUser) {
    const container = document.getElementById("comment-list")
    container.innerHTML = ""

    comments.forEach((c) => {
        const isAuthor = c.userId === loginUser.id
        const commentEl = document.createElement("div")
        commentEl.className = "comment"

        commentEl.innerHTML = `
                <div class="comment-author">
                    <div class="comment-info">
                        <img src="${c.userImgUrl || "https://via.placeholder.com/32"}" alt="${c.userName}">
                        <strong>${c.userName}</strong>
                        <span class="comment-meta">${c.createdAt.replace("T", " ").substring(0, 16)}</span>
                    </div>
                    <div class="comment-buttons">
                        ${isAuthor ? "<button>수정</button><button>삭제</button>" : ""}
                    </div>
                </div>
                <div>${c.content}</div>
                <div class="replies" id="replies-${c.commentId}"></div>
            `

        container.appendChild(commentEl)

        if (c.commentNum > 0) {
            loadReplies(c.commentId, loginUser)
        }
    })
}

async function loadReplies(commentId, loginUser) {
    const container = document.getElementById(`replies-${commentId}`)
    if (!container) return

    const res = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}/comments/${commentId}`)
    const data = await res.json()
    const replies = data.result || []

    // 기존 내용 제거
    container.innerHTML = ""

    // 대댓글이 있으면 "대댓글 n개 더보기" 버튼만 표시
    if (replies.length > 0) {
        addToggleButton(container, replies, loginUser, false) // false = 완전히 접힌 상태
    }
}

// 토글 버튼 추가 함수
function addToggleButton(container, replies, loginUser, isExpanded) {
    // 기존 버튼 제거
    const existingBtn = container.querySelector(".toggle-replies-btn")
    if (existingBtn) {
        existingBtn.remove()
    }

    const toggleBtn = document.createElement("button")
    toggleBtn.className = "more-replies-btn toggle-replies-btn"

    if (isExpanded) {
        // 펼쳐진 상태 - 접기 버튼
        toggleBtn.textContent = "접기"
        toggleBtn.onclick = () => {
            // 모든 대댓글 제거
            const allReplies = container.querySelectorAll(".reply")
            allReplies.forEach((reply) => reply.remove())

            // 처음 상태로 돌아가기 (대댓글 n개 더보기 버튼만 표시)
            addToggleButton(container, replies, loginUser, false)
        }
    } else {
        // 접힌 상태 - 더보기 버튼
        toggleBtn.textContent = `댓글 ${replies.length}개`
        toggleBtn.onclick = () => {
            // 처음 3개 대댓글 표시
            replies.slice(0, 3).forEach((reply) => {
                const rDiv = document.createElement("div")
                rDiv.className = "reply"
                rDiv.setAttribute("data-reply-id", reply.id)
                const isAuthor = reply.userId === loginUser.id

                rDiv.innerHTML = `
          <div class="reply-header">
            <img src="${reply.userImgUrl || "https://via.placeholder.com/28"}" alt="${reply.userName}">
            <strong>${reply.userName}</strong>
            <span style="font-size:13px;color:#666;">${reply.createdAt.replace("T", " ").substring(0, 16)}</span>
            <div class="reply-buttons">
              ${isAuthor ? "<button>수정</button><button>삭제</button>" : ""}
            </div>
          </div>
          <div>${reply.content}</div>
        `
                container.appendChild(rDiv)
            })

            // 3개보다 많으면 더보기 버튼, 3개 이하면 접기 버튼
            if (replies.length > 3) {
                addMoreButton(container, replies, loginUser)
            } else {
                addToggleButton(container, replies, loginUser, true)
            }
        }
    }

    container.appendChild(toggleBtn)
}

// 추가 대댓글 더보기 버튼 (3개 표시 후 나머지를 위한 버튼)
function addMoreButton(container, replies, loginUser) {
    const existingBtn = container.querySelector(".toggle-replies-btn")
    if (existingBtn) {
        existingBtn.remove()
    }

    const moreBtn = document.createElement("button")
    moreBtn.className = "more-replies-btn toggle-replies-btn"
    // const remainingCount = replies.length - 3
    moreBtn.textContent = `더보기`

    moreBtn.onclick = () => {
        // 나머지 대댓글들 추가
        replies.slice(3).forEach((reply) => {
            const rDiv = document.createElement("div")
            rDiv.className = "reply"
            rDiv.setAttribute("data-reply-id", reply.id)
            const isAuthor = reply.userId === loginUser.id

            rDiv.innerHTML = `
        <div class="reply-header">
          <img src="${reply.userImgUrl || "https://via.placeholder.com/28"}" alt="${reply.userName}">
          <strong>${reply.userName}</strong>
          <span style="font-size:13px;color:#666;">${reply.createdAt.replace("T", " ").substring(0, 16)}</span>
          <div class="reply-buttons">
            ${isAuthor ? "<button>수정</button><button>삭제</button>" : ""}
          </div>
        </div>
        <div>${reply.content}</div>
      `
            container.appendChild(rDiv)
        })

        // 접기 버튼으로 변경
        addToggleButton(container, replies, loginUser, true)
    }

    container.appendChild(moreBtn)
}

function editPost() {
    location.href = `/test/group/post/update.html?groupId=${groupId}&postId=${postId}`
}

async function deletePost() {
    if (!confirm("정말 삭제하시겠습니까?")) return

    try {
        const res = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}`, {
            method: "DELETE",
        })
        const data = await res.json()

        if (res.ok) {
            alert(data.result || "게시글이 삭제되었습니다.")
            window.location.href = `/test/group/main.html?id=${groupId}`
        } else {
            alert(data.message || "삭제에 실패했습니다.")
        }
    } catch (e) {
        console.error("삭제 요청 실패", e)
        alert("삭제 중 오류가 발생했습니다.")
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    while (!window.loggedInUser) await new Promise((r) => setTimeout(r, 50))
    const loginUser = window.loggedInUser
    loadPost(loginUser)
})
