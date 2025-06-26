const groupId = new URLSearchParams(location.search).get('groupId');
const postId = new URLSearchParams(location.search).get('postId');

async function loadPost(loginUser) {
    const res = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}`);
    const data = await res.json();
    const post = data.result;

    document.getElementById("user-img").src = post.userImgUrl || "https://via.placeholder.com/48";
    document.getElementById("user-name").textContent = post.userName;
    document.getElementById("created-at").textContent = post.createdAt.replace('T', ' ').substring(0, 16);
    document.getElementById("post-content").innerHTML = post.renderedContent;

    const contentElement = document.getElementById("post-content");
    contentElement.querySelectorAll("img").forEach(img => {
        img.style.maxWidth = "100%";
        img.style.height = "auto";
        img.style.borderRadius = "6px";
        img.style.margin = "10px 0";
    });
    contentElement.querySelectorAll("video").forEach(video => {
        video.style.maxWidth = "100%";
        video.style.height = "auto";
        video.style.borderRadius = "6px";
        video.style.margin = "10px 0";
        video.setAttribute("controls", "true");
    });

    const isAuthor = post.userId === loginUser.id;
    const permissionRes = await fetchWithAuth(`/api/groups/${groupId}/permissions`);
    const permissionData = await permissionRes.json();
    const deletePermission = permissionData.result.permissions.find(p => p.type === 'DELETE_POST');

    let canDelete = false;
    if (deletePermission?.role === 'LEADER') {
        canDelete = isAuthor || loginUser.role === 'LEADER';
    } else if (deletePermission?.role === 'SUB_LEADER') {
        canDelete = isAuthor || loginUser.role === 'SUB_LEADER' || loginUser.role === 'LEADER';
    } else if (deletePermission?.role === 'MEMBER') {
        canDelete = isAuthor;
    }

    if (isAuthor || canDelete) {
        const actions = document.getElementById("post-actions");
        actions.style.display = "flex";
        if (!isAuthor) actions.querySelector(".btn-edit").style.display = "none";
        if (!canDelete) actions.querySelector(".btn-delete").style.display = "none";
    }

    renderComments(post.comments, loginUser);
    document.getElementById("comment-count").textContent = `댓글 ${post.commentNum}개`;
}

function renderComments(comments, loginUser) {
    const container = document.getElementById("comment-list");
    container.innerHTML = "";

    comments.forEach(c => {
        const isAuthor = c.userId === loginUser.id;
        const commentEl = document.createElement("div");
        commentEl.className = "comment";

        commentEl.innerHTML = `
                <div class="comment-header">
                    <div class="comment-info">
                        <img src="${c.userImgUrl || 'https://via.placeholder.com/32'}">
                        <strong>${c.userName}</strong>
                        <span class="comment-meta">${c.createdAt.replace('T', ' ').substring(0, 16)}</span>
                    </div>
                    <div class="comment-buttons">
                        ${isAuthor ? '<button>수정</button><button>삭제</button>' : ''}
                    </div>
                </div>
                <div>${c.content}</div>
                <div class="replies" id="replies-${c.commentId}"></div>
            `;

        container.appendChild(commentEl);

        if (c.commentNum > 0) {
            loadReplies(c.commentId, loginUser);
        }
    });
}

async function loadReplies(commentId, loginUser) {
    const container = document.getElementById(`replies-${commentId}`);
    if (!container) return;

    const res = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}/comments/${commentId}`);
    const data = await res.json();
    const replies = data.result || [];

    replies.slice(0, 3).forEach(reply => {
        const rDiv = document.createElement("div");
        rDiv.className = "reply";
        const isAuthor = reply.userId === loginUser.id;

        rDiv.innerHTML = `
                <div class="reply-header">
                    <img src="${reply.userImgUrl || 'https://via.placeholder.com/28'}">
                    <strong>${reply.userName}</strong>
                    <span style="font-size:13px;color:#666;">${reply.createdAt.replace('T', ' ').substring(0, 16)}</span>
                    <div class="reply-buttons">
                        ${isAuthor ? '<button>수정</button><button>삭제</button>' : ''}
                    </div>
                </div>
                <div>${reply.content}</div>
            `;
        container.appendChild(rDiv);
    });

    if (replies.length > 3) {
        const moreBtn = document.createElement("button");
        moreBtn.className = "more-replies-btn";
        moreBtn.textContent = "대댓글 더보기";
        moreBtn.onclick = () => {
            replies.slice(3).forEach(reply => {
                const rDiv = document.createElement("div");
                rDiv.className = "reply";
                const isAuthor = reply.userId === loginUser.id;

                rDiv.innerHTML = `
                        <div class="reply-header">
                            <img src="${reply.userImgUrl || 'https://via.placeholder.com/28'}">
                            <strong>${reply.userName}</strong>
                            <span style="font-size:13px;color:#666;">${reply.createdAt.replace('T', ' ').substring(0, 16)}</span>
                            <div class="reply-buttons">
                                ${isAuthor ? '<button>수정</button><button>삭제</button>' : ''}
                            </div>
                        </div>
                        <div>${reply.content}</div>
                    `;
                container.appendChild(rDiv);
            });
            moreBtn.remove();
        };
        container.appendChild(moreBtn);
    }
}

function editPost() {
    location.href = `/test/group/post/update.html?groupId=${groupId}&postId=${postId}`;
}

async function deletePost() {
    if (!confirm("정말 삭제하시겠습니까?")) return;

    try {
        const res = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}`, {
            method: "DELETE"
        });
        const data = await res.json();

        if (res.ok) {
            alert(data.result || "게시글이 삭제되었습니다.");
            window.location.href = `/test/group/main.html?id=${groupId}`;
        } else {
            alert(data.message || "삭제에 실패했습니다.");
        }
    } catch (e) {
        console.error("삭제 요청 실패", e);
        alert("삭제 중 오류가 발생했습니다.");
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    while (!window.loggedInUser) await new Promise(r => setTimeout(r, 50));
    const loginUser = window.loggedInUser;
    loadPost(loginUser);
});
