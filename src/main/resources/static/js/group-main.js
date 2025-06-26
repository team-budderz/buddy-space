const groupId = new URLSearchParams(location.search).get('id');

if (!groupId) {
    alert("잘못된 접근입니다.");
    location.href = "/test/main.html";
}

async function checkLeader() {
    try {
        const response = await fetchWithAuth(`/api/groups/${groupId}/membership`);
        const data = await response.json();
        const membership = data.result;

        window.groupMembershipMap = window.groupMembershipMap || {};
        window.groupMembershipMap[groupId] = membership;

        if (!membership || membership.role !== 'LEADER') {
            const settingTab = document.querySelector('#tab-setting');
            if (settingTab) settingTab.style.display = 'none';
        }
    } catch (e) {
        console.error("멤버십 정보 조회 실패:", e.message);
    }
}

function goToPostCreate() {
    location.href = `/test/group/post/create.html?id=${groupId}`;
}

function extractPostPreviewAndThumbnail(contentHtml) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(contentHtml, "text/html");

    let thumbnailUrl = null;
    const firstImg = doc.querySelector("img[src]");
    if (firstImg) {
        thumbnailUrl = firstImg.getAttribute("src");
    } else {
        const firstVideo = doc.querySelector("video[poster]");
        if (firstVideo) {
            thumbnailUrl = firstVideo.getAttribute("poster");
        }
    }

    doc.querySelectorAll("img, video, a").forEach(el => el.remove());
    const rawText = doc.body.textContent.trim().replace(/\s+/g, ' ');
    const textPreview = rawText.slice(0, 100);
    const hasMore = rawText.length > 100;

    return {thumbnailUrl, textPreview, hasMore};
}

function extractNoticePreview(contentHtml) {
    const parser = new DOMParser();
    const doc = parser.parseFromString(contentHtml, "text/html");

    doc.querySelectorAll("img, video, a, br").forEach(el => el.remove());
    const rawText = doc.body.textContent.trim().replace(/\s+/g, ' ');
    const preview = rawText.slice(0, 30);
    const hasMore = rawText.length > 30;

    return hasMore ? preview + "..." : preview;
}

async function loadNotices() {
    try {
        const response = await fetchWithAuth(`/api/groups/${groupId}/posts/notice`);
        const data = await response.json();
        const notices = data.result;

        const container = document.getElementById("notice-list");
        const title = document.querySelector(".notice-preview h2");

        if (!Array.isArray(notices) || notices.length === 0) {
            if (title) title.style.display = "none";
            container.innerHTML = "";
            return;
        }

        container.innerHTML = notices.map(n => {
            const preview = extractNoticePreview(n.content);

            return `
                <div class="notice-card"
                    onclick="location.href='/test/group/post.html?groupId=${groupId}&postId=${n.id}'"
                    style="cursor:pointer;">
                    ${preview}
                </div>
            `;
        }).join("");
    } catch (e) {
        console.error("공지사항 불러오기 실패:", e.message);
    }
}

async function loadPosts() {
    try {
        const response = await fetchWithAuth(`/api/groups/${groupId}/posts`);
        const data = await response.json();
        const posts = data.result;

        const container = document.getElementById("post-list");

        if (!Array.isArray(posts) || posts.length === 0) {
            container.innerHTML = `<div style="color: #777; font-size: 15px;">등록된 게시글이 없습니다.</div>`;
            return;
        }

        container.innerHTML = posts.map(p => {
            const {thumbnailUrl, textPreview, hasMore} = extractPostPreviewAndThumbnail(p.content);

            return `
                <div class="post-card" onclick="location.href='/test/group/post.html?groupId=${groupId}&postId=${p.id}'">
                    ${thumbnailUrl ? `<img src="${thumbnailUrl}" class="post-thumbnail" alt="썸네일">` : ""}
                    <div class="post-content">
                        <div class="post-meta">
                            <img src="${p.userImgUrl || 'https://via.placeholder.com/24'}" alt="프로필">
                            ${p.userName} · ${p.createdAt.replace('T', ' ').substring(0, 16)}
                        </div>
                        <div class="post-text">
                            ${textPreview}${hasMore ? '<span class="more"> ...더보기</span>' : ''}
                        </div>
                        <div class="post-footer">댓글 ${p.commentsNum}개</div>
                    </div>
                </div>
            `;
        }).join("");
    } catch (e) {
        console.error("게시글 불러오기 실패:", e.message);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    checkLeader();
    loadNotices();
    loadPosts();
});
