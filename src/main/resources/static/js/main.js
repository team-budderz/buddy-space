const tabs = document.querySelectorAll('.tab');
const container = document.getElementById('groupListContainer');
const locationDiv = document.getElementById('user-location');

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

function extractDong(address) {
    if (!address) return "";
    const parts = address.split(" ");
    return parts.length > 0 ? parts[parts.length - 1] : address;
}

function updateUserLocation(tabType) {
    if (tabType === "offline" && window.loggedInUser?.address) {
        const dong = extractDong(window.loggedInUser.address);
        locationDiv.textContent = `📍${dong}`;
        locationDiv.style.display = "block";
    } else {
        locationDiv.style.display = "none";
    }
}

async function fetchGroups(tabType) {
    let url = "";
    let includeCreate = false;

    if (tabType === "my") {
        url = `/api/groups/my`;
        includeCreate = true;
    } else if (tabType === "online") {
        url = `/api/groups/on?sort=popular`;
    } else if (tabType === "offline") {
        url = `/api/groups/off?sort=popular`;
    }

    try {
        const res = await fetchWithAuth(url);
        const data = await res.json();
        renderGroups(data.result.content, includeCreate, tabType);
    } catch (err) {
        console.error("모임 불러오기 실패", err);
    }
}

function renderGroups(groups, includeCreate, tabType) {
    container.innerHTML = "";

    if (includeCreate) {
        const createCard = document.createElement("div");
        createCard.className = "create-card";
        createCard.innerHTML = `
            <div class="plus-icon">＋</div>
            <div>만들기</div>`;
        createCard.addEventListener("click", () => {
            window.location.href = "/test/group/create.html";
        });
        container.appendChild(createCard);
    }

    groups.forEach(group => {
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

        if (group.joinStatus !== "APPROVED") {
            const joinBtn = document.createElement("button");
            joinBtn.style.marginTop = "8px";
            joinBtn.style.padding = "6px 12px";
            joinBtn.style.border = "none";
            joinBtn.style.borderRadius = "4px";
            joinBtn.style.backgroundColor = "#4CAF50";
            joinBtn.style.color = "#fff";
            joinBtn.style.cursor = "pointer";

            joinBtn.textContent = group.joinStatus === "REQUESTED" ? "가입 요청 중" : "참여하기";

            joinBtn.addEventListener("click", async () => {
                if (group.joinStatus === "BLOCKED") {
                    alert("가입 요청할 수 없는 모임입니다.");
                    return;
                }

                if (group.joinStatus === "REQUESTED") {
                    alert("이미 가입 요청 중인 모임입니다.");
                    return;
                }

                try {
                    const res = await fetchWithAuth(`/api/groups/${group.groupId}/members/requests`, {
                        method: "POST"
                    });
                    const data = await res.json();

                    if (res.ok && data.result) {
                        alert("참여 요청이 완료되었습니다.");
                        joinBtn.disabled = true;
                        joinBtn.textContent = "가입 요청 중";
                    } else {
                        alert(data.message || "참여 요청 실패");
                    }
                } catch (err) {
                    console.error("참여 요청 실패", err);
                    alert("참여 요청 중 오류가 발생했습니다.");
                }
            });

            info.appendChild(joinBtn);
        } else {
            card.addEventListener("click", () => {
                window.location.href = `/test/group/main.html?id=${group.groupId}`;
            });
        }

        card.appendChild(info);
        container.appendChild(card);
    });
}

tabs.forEach(tab => {
    tab.addEventListener("click", () => {
        tabs.forEach(t => t.classList.remove("active"));
        tab.classList.add("active");
        const selectedTab = tab.dataset.tab;
        fetchGroups(selectedTab);
        updateUserLocation(selectedTab);
    });
});

document.addEventListener("DOMContentLoaded", () => {
    fetchGroups("my");
    updateUserLocation("my");
});
