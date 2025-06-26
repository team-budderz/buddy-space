const groupId = new URLSearchParams(location.search).get("id");

document.addEventListener("DOMContentLoaded", () => {
    window.isEditMode = false;
    window.addEventListener("beforeunload", beforeUnloadHandler);
});

async function submitPost() {
    const previewDiv = document.getElementById("previewContent");
    const saveDiv = document.getElementById("saveContent");

    // 1. 콘텐츠 복제
    const cloned = previewDiv.cloneNode(true);

    // 2. 삭제 버튼 제거 + 미디어 unwrap
    cloned.querySelectorAll(".preview-media").forEach(wrapper => {
        const media = wrapper.querySelector("img, video, a");
        if (!media) return;

        // 삭제 버튼 제거
        const deleteBtn = wrapper.querySelector(".delete-btn");
        if (deleteBtn) deleteBtn.remove();

        // wrapper <div> 제거하고 미디어 요소만 넣기
        wrapper.replaceWith(media);
    });

    // 3. 저장용 HTML 추출
    saveDiv.innerHTML = cloned.innerHTML;

    const postData = {
        content: saveDiv.innerHTML,
        isNotice: document.getElementById("noticeCheck").checked,
        reserveAt: null
    };

    try {
        const res = await fetchWithAuth(`/api/groups/${groupId}/posts`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(postData)
        });

        const data = await res.json();

        if (!res.ok) {
            alert(data.message || "작성 실패");
            return;
        }

        alert("작성 완료!");
        location.href = `/test/group/post.html?groupId=${groupId}&postId=${data.result.postId}`;
    } catch (err) {
        console.error("작성 중 오류", err);
        alert("작성 실패: " + err.message);
    }
}