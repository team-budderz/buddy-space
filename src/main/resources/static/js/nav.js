document.addEventListener("DOMContentLoaded", () => {
    const groupId = new URLSearchParams(location.search).get('id') || new URLSearchParams(location.search).get('groupId');
    if (!groupId) return;

    const tabContainer = document.createElement("nav");
    tabContainer.className = "group-nav";

    const tabs = [
        { name: "게시글", path: "main" },
        { name: "일정", path: "schedule" },
        { name: "미션", path: "mission" },
        { name: "투표", path: "vote" },
        { name: "사진첩", path: "album" },
        { name: "멤버", path: "members" },
        { name: "설정", path: "setting", id: "tab-setting" }
    ];

    const currentPath = location.pathname.split("/").pop();

    tabs.forEach(tab => {
        const a = document.createElement("a");
        a.href = `/test/group/${tab.path}.html?id=${groupId}`;
        a.textContent = tab.name;
        if (tab.id) a.id = tab.id;
        if (currentPath === `${tab.path}.html`) a.classList.add("active");

        tabContainer.appendChild(a);
    });

    document.querySelector("main")?.prepend(tabContainer);
});