// const groupId = new URLSearchParams(location.search).get("id");
let selectedFile = null;

document.getElementById("fileInput").addEventListener("change", e => {
    selectedFile = e.target.files[0];
    const modal = document.getElementById("fileModal");
    const modalPreview = document.getElementById("modalPreview");

    if (!selectedFile) return;

    const url = URL.createObjectURL(selectedFile);
    if (selectedFile.type.startsWith("image/")) {
        modalPreview.innerHTML = `<img src="${url}" alt="미리보기">`;
    } else if (selectedFile.type.startsWith("video/")) {
        modalPreview.innerHTML = `<video src="${url}" controls></video>`;
    } else {
        modalPreview.innerHTML = `<span>${selectedFile.name}</span>`;
    }

    modal.style.display = "flex";
});

function cancelAttachFile() {
    document.getElementById("fileModal").style.display = "none";
    document.getElementById("fileInput").value = "";
    document.getElementById("modalPreview").innerHTML = "";
    selectedFile = null;
}

async function confirmAttachFile() {
    if (!selectedFile) return;

    const formData = new FormData();
    formData.append("file", selectedFile);

    try {
        const res = await fetchWithAuth(`/api/groups/${groupId}/post-files`, {
            method: "POST",
            body: formData
        });

        const data = await res.json();

        if (!res.ok) {
            alert(data.message || "업로드 실패");
            return;
        }

        const { id, url, type, filename, thumbnailUrl } = data.result;
        let element;

        if (type.startsWith("image/")) {
            element = `
                <div class="preview-media">
                    <img data-id="${id}" src="${url}" />
                    <button type="button" class="delete-btn" onclick="this.parentElement.remove()">삭제</button>
                </div>`;
        } else if (type.startsWith("video/")) {
            element = `
                <div class="preview-media">
                    <video data-id="${id}" controls poster="${thumbnailUrl || ''}">
                        <source src="${url}" type="${type}" />
                    </video>
                    <button type="button" class="delete-btn" onclick="this.parentElement.remove()">삭제</button>
                </div>`;
        } else {
            element = `
                <div class="preview-media">
                    <a data-id="${id}" href="${url}" target="_blank">${filename}</a>
                    <button type="button" class="delete-btn" onclick="this.parentElement.remove()">삭제</button>
                </div>`;
        }

        document.getElementById("previewContent").insertAdjacentHTML("beforeend", element);
        cancelAttachFile();
    } catch (err) {
        console.error("업로드 실패", err);
        alert("업로드 중 오류가 발생했습니다: " + err.message);
    }
}

function getCurrentAttachmentIds() {
    const ids = new Set();
    document.querySelectorAll('#previewContent img[data-id], video[data-id], a[data-id]').forEach(el => {
        const id = el.getAttribute("data-id");
        if (id) ids.add(Number(id));
    });
    return Array.from(ids);
}

async function deleteAttachments(ids) {
    if (!ids || ids.length === 0) return;

    try {
        await fetchWithAuth("/api/attachments", {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(ids)
        });
        console.log("고아 파일 삭제 완료", ids);
    } catch (err) {
        console.warn("첨부파일 삭제 실패", err);
    }
}

let isModified = false;
let initialAttachmentIds = [];

function markModified() {
    isModified = true;
}

document.addEventListener("input", markModified);
document.getElementById("fileInput").addEventListener("change", markModified);

function beforeUnloadHandler(e) {
    if (!isModified) return;
    e.preventDefault();
    e.returnValue = "";
}

// document.addEventListener("DOMContentLoaded", () => {
//     window.addEventListener("beforeunload", beforeUnloadHandler);
// });

document.querySelector(".btn-cancel").addEventListener("click", async () => {
    window.removeEventListener("beforeunload", beforeUnloadHandler);

    const confirmed = confirm("게시글 작성을 취소하시겠습니까?");
    if (!confirmed) return;

    const currentIds = getCurrentAttachmentIds();
    const idsToDelete = window.isEditMode
        ? currentIds.filter(id => !initialAttachmentIds.includes(id))
        : currentIds;

    await deleteAttachments(idsToDelete);

    window.location.href = `/test/group/main.html?id=${groupId}`;
});
