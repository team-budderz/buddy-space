/**
 * 게시글 작성 페이지
 * - 게시글 콘텐츠 작성 및 미리보기
 * - 파일 첨부 및 미디어 관리
 * - 공지사항 설정 기능
 * - 게시글 제출 및 저장
 * - 작성 취소 시 첨부파일 정리
 */

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", () => {
    groupId = new URLSearchParams(location.search).get("id");

    if (!groupId) {
        alert("모임 ID를 찾을 수 없습니다.");
        return;
    }

    setupEventListeners();
});

// 이벤트 리스너 설정
function setupEventListeners() {
    document.addEventListener("keydown", handleKeyboardShortcuts);
    window.addEventListener("beforeunload", beforeUnloadHandler);
}

// 키보드 단축키 처리
function handleKeyboardShortcuts(e) {
    if (e.ctrlKey && e.key === "Enter") {
        const submitBtn = document.querySelector(".btn-submit");
        if (submitBtn && !submitBtn.disabled) {
            submitPost();
        }
    }

    if (e.key === "Escape") {
        const cancelBtn = document.querySelector(".btn-cancel");
        if (cancelBtn) {
            cancelBtn.click();
        }
    }
}

// 게시글 제출 처리
async function submitPost() {
    const previewDiv = document.getElementById("previewContent");
    const saveDiv = document.getElementById("saveContent");
    const submitBtn = document.querySelector(".btn-submit");

    if (!validatePostContent(previewDiv)) {
        return;
    }

    try {
        setSubmitButtonState(submitBtn, true, "작성 중...");

        const cleanedContent = cleanPostContent(previewDiv);
        saveDiv.innerHTML = cleanedContent;

        const postData = {
            content: saveDiv.innerHTML,
            isNotice: document.getElementById("noticeCheck").checked,
            reserveAt: null,
        };

        const response = await fetchWithAuth(`/api/groups/${groupId}/posts`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(postData),
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "작성 실패");
        }

        alert("게시글이 작성되었습니다!");
        window.removeEventListener("beforeunload", beforeUnloadHandler);
        setTimeout(() => {
            location.href = `/test/group/post?groupId=${groupId}&postId=${data.result.postId}`;
        }, 1000);
    } catch (error) {
        console.error("게시글 작성 실패:", error);
        alert(error.message || "게시글 작성 중 오류가 발생했습니다.");
        setSubmitButtonState(submitBtn, false, "작성 완료");
    }
}

// 게시글 콘텐츠 검증
function validatePostContent(previewDiv) {
    const textContent = previewDiv.textContent.trim();
    const hasMedia = previewDiv.querySelectorAll("img, video, a").length > 0;

    if (!textContent && !hasMedia) {
        alert("게시글 내용을 입력해주세요.");
        return false;
    }

    return true;
}

// 게시글 콘텐츠 정리: media src/href 제거 + data-id만 남김
function cleanPostContent(previewDiv) {
    const cloned = previewDiv.cloneNode(true);

    // .preview-media 래퍼 제거 및 내부 media 추출
    cloned.querySelectorAll(".preview-media").forEach(wrapper => {
        const media = wrapper.querySelector("img, video, a");
        if (!media) return;

        const deleteBtn = wrapper.querySelector(".delete-btn");
        if (deleteBtn) deleteBtn.remove();

        wrapper.replaceWith(media);
    });

    // <img> 정제
    cloned.querySelectorAll("img").forEach(img => {
        const id = img.getAttribute("data-id");
        if (id) {
            const newImg = document.createElement("img");
            newImg.setAttribute("data-id", id);
            img.replaceWith(newImg);
        }
    });

    // <video> 정제
    cloned.querySelectorAll("video").forEach(video => {
        const id = video.getAttribute("data-id");
        if (id) {
            const newVideo = document.createElement("video");
            newVideo.setAttribute("data-id", id);
            video.replaceWith(newVideo);
        }
    });

    // <source> 제거
    cloned.querySelectorAll("source").forEach(source => {
        source.remove();
    });

    // <a> 정제
    cloned.querySelectorAll("a").forEach(a => {
        const id = a.getAttribute("data-id");
        if (id) {
            const newA = document.createElement("a");
            newA.setAttribute("data-id", id);
            a.replaceWith(newA);
        }
    });

    return cloned.innerHTML;
}

// 제출 버튼 상태 설정
function setSubmitButtonState(button, disabled, text) {
    if (button) {
        button.disabled = disabled;
        button.textContent = text;
    }
}

// 페이지 이탈 경고 핸들러
function beforeUnloadHandler(e) {
    const previewDiv = document.getElementById("previewContent");
    const hasContent = previewDiv &&
        (previewDiv.textContent.trim() || previewDiv.querySelectorAll("img, video, a").length > 0);

    if (hasContent) {
        e.preventDefault();
        e.returnValue = "작성 중인 내용이 있습니다. 정말 나가시겠습니까?";
        return e.returnValue;
    }
}
