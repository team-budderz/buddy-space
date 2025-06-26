const groupTypeMap = {
    ONLINE: "온라인",
    OFFLINE: "오프라인",
    HYBRID: "온/오프라인"
};

const groupInterestMap = {
    HOBBY: "취미",
    FAMILY: "가족",
    SCHOOL: "학교",
    BUSINESS: "업무",
    EXERCISE: "운동",
    GAME: "게임",
    STUDY: "스터디",
    FAN: "팬",
    OTHER: "기타"
};

document.addEventListener("DOMContentLoaded", async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const keyword = urlParams.get("keyword");
    const page = parseInt(urlParams.get("page")) || 0;

    if (!keyword) {
        document.getElementById("keyword-display").textContent = "검색어가 없습니다.";
        document.getElementById("result-count").textContent = "";
        return;
    }

    document.getElementById("keyword-display").textContent = `검색 키워드 '${keyword}'`;

    try {
        const response = await fetchWithAuth(`/api/groups/search?keyword=${encodeURIComponent(keyword)}&page=${page}`);
        const data = await response.json();

        if (!response.ok || !data.result) {
            throw new Error(data.message || "검색 실패");
        }

        const groupList = data.result.content;
        const totalPages = data.result.totalPages;
        const totalElements = data.result.totalElements;
        const currentPage = data.result.pageNumber;

        document.getElementById("result-count").textContent = `검색 결과 ${totalElements}건`;

        const container = document.getElementById("groupListContainer");
        container.innerHTML = "";

        groupList.forEach(group => {
            const card = document.createElement("div");
            card.className = "group-card";

            const img = document.createElement("img");
            img.src = group.groupCoverImageUrl || "https://via.placeholder.com/300x214?text=No+Image";
            card.appendChild(img);

            const info = document.createElement("div");
            info.className = "group-info";

            const name = document.createElement("h3");
            name.textContent = group.groupName;
            info.appendChild(name);

            if (group.groupDescription) {
                const desc = document.createElement("p");
                desc.textContent = group.groupDescription;
                info.appendChild(desc);
            }

            const meta = document.createElement("div");
            meta.className = "group-meta";
            meta.innerHTML = `
                ${groupTypeMap[group.groupType] || group.groupType} /
                ${groupInterestMap[group.groupInterest] || group.groupInterest} ·
                멤버 ${group.memberCount}명`;
            info.appendChild(meta);

            card.appendChild(info);

            const joinBtn = document.createElement("button");
            joinBtn.className = "join-btn";

            if (group.joinStatus === null) {
                joinBtn.textContent = "참여하기";
                joinBtn.addEventListener("click", async () => {
                    try {
                        const res = await fetchWithAuth(`/api/groups/${group.groupId}/members/requests`, {
                            method: "POST"
                        });
                        const resData = await res.json();

                        if (res.ok && resData.result) {
                            alert("참여 요청이 완료되었습니다.");
                            joinBtn.disabled = true;
                            joinBtn.textContent = "가입 요청 중";
                        } else {
                            alert(resData.message || "참여 요청 실패");
                        }
                    } catch (err) {
                        alert("참여 요청 실패");
                        console.error(err);
                    }
                });
                card.appendChild(joinBtn);
            } else if (group.joinStatus === "REQUESTED") {
                joinBtn.textContent = "가입 요청 중";
                joinBtn.disabled = true;
                card.appendChild(joinBtn);
            } else if (group.joinStatus === "BLOCKED") {
                joinBtn.textContent = "참여하기";
                joinBtn.addEventListener("click", () => {
                    alert("가입 요청할 수 없는 모임입니다.");
                });
                card.appendChild(joinBtn);
            } else if (group.joinStatus === "APPROVED") {
                card.style.cursor = "pointer";
                card.addEventListener("click", () => {
                    window.location.href = `/test/group/main.html?id=${group.groupId}`;
                });
            }

            container.appendChild(card);
        });

        addPagination(currentPage, totalPages, keyword);

    } catch (err) {
        document.getElementById("result-count").textContent = "검색 중 오류가 발생했습니다.";
        console.error(err);
    }
});

function addPagination(currentPage, totalPages, keyword) {
    let existing = document.getElementById("pagination");
    if (existing) existing.remove();

    const pagination = document.createElement("div");
    pagination.id = "pagination";
    pagination.style.textAlign = "center";
    pagination.style.marginTop = "30px";

    for (let i = 0; i < totalPages; i++) {
        const btn = document.createElement("button");
        btn.textContent = i + 1;
        btn.style.margin = "0 4px";
        btn.style.padding = "6px 12px";
        btn.style.border = "1px solid #ccc";
        btn.style.borderRadius = "4px";
        btn.style.backgroundColor = i === currentPage ? "#4a90e2" : "#fff";
        btn.style.color = i === currentPage ? "#fff" : "#333";
        btn.style.cursor = "pointer";

        btn.addEventListener("click", () => {
            window.location.href = `/test/search.html?keyword=${encodeURIComponent(keyword)}&page=${i}`;
        });

        pagination.appendChild(btn);
    }

    document.querySelector("main").appendChild(pagination);
}