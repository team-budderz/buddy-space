const coverImage = document.getElementById('coverImage');
const preview = document.getElementById('preview');
const form = document.getElementById('groupForm');
const errorMessage = document.getElementById('errorMessage');

coverImage.addEventListener('change', function () {
    const file = this.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            preview.src = e.target.result;
            preview.style.display = 'block';
        }
        reader.readAsDataURL(file);
    } else {
        preview.src = "#";
        preview.style.display = 'none';
    }
});

form.addEventListener('submit', async function (e) {
    e.preventDefault();
    errorMessage.textContent = '';

    const formData = new FormData();
    const cover = coverImage.files[0];
    const name = document.getElementById('groupName').value;
    const access = document.querySelector('input[name="visibility"]:checked')?.value;
    const type = document.querySelector('input[name="type"]:checked')?.value;
    const interest = document.querySelector('input[name="interest"]:checked')?.value;

    if (!name || !access || !type || !interest) {
        errorMessage.textContent = '모든 필수 항목을 입력해주세요.';
        return;
    }

    const request = new Blob([JSON.stringify({
        name,
        access,
        type,
        interest
    })], {type: 'application/json'});

    formData.append('request', request);
    if (cover) formData.append('coverImage', cover);

    try {
        const res = await fetchWithAuth(`/api/groups`, {
            method: 'POST',
            body: formData
        });

        const data = await res.json();

        if (res.status === 200 && data.result) {
            window.location.href = '/test/main.html';
        } else {
            errorMessage.textContent = data.message || '모임 생성 중 오류가 발생했습니다.';
        }
    } catch (err) {
        errorMessage.textContent = '서버와 통신 중 문제가 발생했습니다.';
    }
});
