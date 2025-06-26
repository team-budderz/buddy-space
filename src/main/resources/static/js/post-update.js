const groupId = new URLSearchParams(location.search).get("groupId");
const postId = new URLSearchParams(location.search).get("postId");
window.isEditMode = true;

function wrapExistingMedia() {
    const container = document.getElementById("previewContent");

    container.querySelectorAll("img, video, a").forEach(el => {
        if (el.closest(".preview-media")) return;

        const wrapper = document.createElement("div");
        wrapper.className = "preview-media";

        const clonedEl = el.cloneNode(true);

        const deleteBtn = document.createElement("button");
        deleteBtn.className = "delete-btn";
        deleteBtn.textContent = "삭제";
        deleteBtn.setAttribute("type", "button");
        deleteBtn.setAttribute("contenteditable", "false");
        deleteBtn.onclick = function () {
            wrapper.remove();
            markModified();
        };

        wrapper.appendChild(clonedEl);
        wrapper.appendChild(deleteBtn);

        const currentParent = el.parentNode;
        if (
            ["DIV", "P"].includes(currentParent.tagName) &&
            currentParent.childNodes.length === 1
        ) {
            currentParent.replaceWith(wrapper);
        } else {
            el.replaceWith(wrapper);
        }
    });
}

document.addEventListener("DOMContentLoaded", async () => {
    if (!groupId || !postId) {
        console.log("groupId", groupId);
        console.log("postId", postId);
        alert("잘못된 접근입니다.");
        return;
    }

    try {
        const res = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}`);
        const data = await res.json();

        if (!res.ok) {
            alert(data.message || "게시글 조회 실패");
            return;
        }

        const post = data.result;
        document.getElementById("previewContent").innerHTML = post.renderedContent || "";
        document.getElementById("noticeCheck").checked = post.isNotice || false;

        wrapExistingMedia();
        setInitialAttachmentIds();
        window.addEventListener("beforeunload", beforeUnloadHandler);

    } catch (err) {
        console.error(err);
        alert("게시글 정보를 불러오는 중 오류 발생");
    }
});

function setInitialAttachmentIds() {
    initialAttachmentIds = getCurrentAttachmentIds();
}

async function updatePost() {
    window.removeEventListener("beforeunload", beforeUnloadHandler);

    const previewDiv = document.getElementById("previewContent");
    const saveDiv = document.getElementById("saveContent");

    const cloned = previewDiv.cloneNode(true);

    cloned.querySelectorAll(".preview-media").forEach(wrapper => {
        const media = wrapper.querySelector("img, video, a");
        if (!media) return;

        const deleteBtn = wrapper.querySelector(".delete-btn");
        if (deleteBtn) deleteBtn.remove();

        wrapper.replaceWith(media);
    });

    saveDiv.innerHTML = cloned.innerHTML;

    const postData = {
        content: saveDiv.innerHTML,
        isNotice: document.getElementById("noticeCheck").checked,
        reserveAt: null
    };

    try {
        const res = await fetchWithAuth(`/api/groups/${groupId}/posts/${postId}`, {
            method: "PATCH",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(postData)
        });

        const data = await res.json();

        if (!res.ok) {
            alert(data.message || "수정 실패");
            return;
        }

        alert("게시글이 수정되었습니다.");
        location.href = `/test/group/post.html?groupId=${groupId}&postId=${postId}`;
    } catch (err) {
        console.error("수정 중 오류", err);
        alert("게시글 수정 중 오류 발생");
    }
}