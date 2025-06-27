// 전역 변수
let groupId = null;
let allAttachments = [];
let currentCategory = 'all';
let currentAttachment = null;

// 사진첩 기능 JavaScript
document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    groupId = urlParams.get('id');

    if (!groupId) {
        showError('그룹 ID가 없습니다.');
        return;
    }

    loadAlbumData();
    setupEventListeners();
});

// 이벤트 리스너 설정
function setupEventListeners() {
    const menuLinks = document.querySelectorAll('.photo-menu-link');
    const modal = document.getElementById('mediaModal');
    const closeModalBtn = document.getElementById('closeModalBtn');
    const downloadBtn = document.getElementById('downloadBtn');

    menuLinks.forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            const category = this.dataset.category;
            if (category === currentCategory) return;

            menuLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
            currentCategory = category;
            updateCategoryTitle(category);
            filterAndRenderAlbum(category);
        });
    });

    closeModalBtn.addEventListener('click', closeModal);
    modal.addEventListener('click', function (e) {
        if (e.target === modal) closeModal();
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modal.classList.contains('active')) {
            closeModal();
        }
    });

    downloadBtn.addEventListener('click', handleDownload);
}

// 앨범 데이터 로드
async function loadAlbumData() {
    try {
        showLoading();
        const url = `/api/groups/${groupId}/albums`;
        const response = await fetchWithAuth(url);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        if (data.result) {
            allAttachments = data.result;
            updateCounts();
            filterAndRenderAlbum(currentCategory);
        } else {
            throw new Error('데이터 형식이 올바르지 않습니다.');
        }
    } catch (error) {
        console.error('앨범 데이터 로드 실패:', error);
        showError('사진을 불러오는데 실패했습니다.');
    }
}

// 클라이언트 사이드 필터링 및 렌더링
function filterAndRenderAlbum(category) {
    let filteredAttachments;
    if (category === 'all') {
        filteredAttachments = allAttachments;
    } else if (category === 'image') {
        filteredAttachments = allAttachments.filter(a => a.type.startsWith('image/'));
    } else if (category === 'video') {
        filteredAttachments = allAttachments.filter(a => a.type.startsWith('video/'));
    } else {
        filteredAttachments = allAttachments;
    }
    renderAlbum(filteredAttachments);
}

// 앨범 렌더링
function renderAlbum(attachments) {
    const photoGrid = document.getElementById('photoGrid');

    if (!attachments || attachments.length === 0) {
        showEmptyState();
        return;
    }

    const html = attachments.map(attachment => {
        const isVideo = attachment.type.startsWith('video/');
        const imageUrl = isVideo && attachment.thumbnailUrl ? attachment.thumbnailUrl : attachment.url;
        const fileSize = formatFileSize(attachment.size);
        const uploadDate = formatDate(attachment.uploadedAt);

        return `
            <div class="photo-item" data-id="${attachment.id}" data-type="${isVideo ? 'video' : 'image'}">
                <img src="${imageUrl}" alt="${attachment.filename}" loading="lazy">
                ${isVideo ? `
                    <div class="video-indicator">🎬 영상</div>
                    <button class="video-play-btn">▶</button>
                ` : ''}
                <div class="photo-overlay">
                    <div class="photo-info">
                        <div>${attachment.filename}</div>
                        <div class="photo-date">${uploadDate}</div>
                        <div class="photo-size">${fileSize}</div>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    photoGrid.innerHTML = html;

    photoGrid.querySelectorAll('.photo-item').forEach(item => {
        item.addEventListener('click', function () {
            const attachmentId = this.dataset.id;
            const attachment = allAttachments.find(a => a.id == attachmentId);
            if (attachment) {
                openMediaModal(attachment);
            }
        });
    });

    setTimeout(() => {
        photoGrid.querySelectorAll('.photo-item').forEach((item, index) => {
            item.style.opacity = '0';
            item.style.transform = 'translateY(20px)';
            setTimeout(() => {
                item.style.transition = 'all 0.3s ease';
                item.style.opacity = '1';
                item.style.transform = 'translateY(0)';
            }, index * 50);
        });
    }, 100);
}

// 미디어 모달 열기
function openMediaModal(attachment) {
    currentAttachment = attachment;
    const modal = document.getElementById('mediaModal');
    const modalTitle = document.getElementById('modalTitle');
    const modalMediaContainer = document.getElementById('modalMediaContainer');
    const modalInfoGrid = document.getElementById('modalInfoGrid');
    const downloadBtn = document.getElementById('downloadBtn');

    modalTitle.textContent = attachment.filename;
    downloadBtn.style.display = 'flex';
    downloadBtn.disabled = false;
    downloadBtn.innerHTML = '📥 다운로드';

    modalMediaContainer.innerHTML = '';

    const isVideo = attachment.type.startsWith('video/');

    if (isVideo) {
        const video = document.createElement('video');
        video.src = attachment.url;
        video.controls = true;
        video.autoplay = false;
        video.preload = 'metadata';

        video.onerror = function () {
            modalMediaContainer.innerHTML = `
                    <div style="color: #64748b; text-align: center; padding: 40px;">
                        <div style="font-size: 48px; margin-bottom: 16px;">⚠️</div>
                        <div>영상을 불러올 수 없습니다</div>
                    </div>
                `;
        };
        modalMediaContainer.appendChild(video);
    } else {
        const img = document.createElement('img');
        img.src = attachment.url;
        img.alt = attachment.filename;

        img.onerror = function () {
            modalMediaContainer.innerHTML = `
                    <div style="color: #64748b; text-align: center; padding: 40px;">
                        <div style="font-size: 48px; margin-bottom: 16px;">⚠️</div>
                        <div>이미지를 불러올 수 없습니다</div>
                    </div>
                `;
        };
        modalMediaContainer.appendChild(img);
    }

    const uploadDate = formatDate(attachment.uploadedAt);
    const fileSize = formatFileSize(attachment.size);
    const fileType = isVideo ? '영상' : '사진';

    modalInfoGrid.innerHTML = `
            <div class="modal-info-item">
                <div class="modal-info-label">파일명</div>
                <div class="modal-info-value">${attachment.filename}</div>
            </div>
            <div class="modal-info-item">
                <div class="modal-info-label">타입</div>
                <div class="modal-info-value">${fileType}</div>
            </div>
            <div class="modal-info-item">
                <div class="modal-info-label">크기</div>
                <div class="modal-info-value">${fileSize}</div>
            </div>
            <div class="modal-info-item">
                <div class="modal-info-label">업로드 날짜</div>
                <div class="modal-info-value">${uploadDate}</div>
            </div>
        `;

    modal.classList.add('active');
    // body 스크롤 제한 제거 - 모달 자체에서 스크롤 처리
}

// 모달 닫기
function closeModal() {
    const modal = document.getElementById('mediaModal');
    const modalMediaContainer = document.getElementById('modalMediaContainer');

    const video = modalMediaContainer.querySelector('video');
    if (video) {
        video.pause();
        video.currentTime = 0;
    }

    modal.classList.remove('active');
    currentAttachment = null;
}

// 다운로드 처리
async function handleDownload() {
    if (!currentAttachment) return;

    const downloadBtn = document.getElementById('downloadBtn');
    const originalText = downloadBtn.innerHTML;

    try {
        // 버튼 비활성화 및 로딩 표시
        downloadBtn.disabled = true;
        downloadBtn.innerHTML = '⏳ 준비 중...';

        // 다운로드 URL 요청
        const response = await fetchWithAuth(`/api/attachments/${currentAttachment.id}/download`);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();

        if (data.result) {
            // 다운로드 실행
            const link = document.createElement('a');
            link.href = data.result;
            link.download = currentAttachment.filename;
            link.style.display = 'none';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            // 성공 표시
            downloadBtn.innerHTML = '✅ 완료';
            setTimeout(() => {
                downloadBtn.innerHTML = originalText;
                downloadBtn.disabled = false;
            }, 2000);
        } else {
            throw new Error('다운로드 URL을 받을 수 없습니다.');
        }

    } catch (error) {
        console.error('다운로드 실패:', error);
        alert('다운로드에 실패했습니다. 다시 시도해주세요.');

        // 버튼 복원
        downloadBtn.innerHTML = originalText;
        downloadBtn.disabled = false;
    }
}

// 카운트 업데이트
function updateCounts() {
    const totalCount = allAttachments.length;
    const photoCount = allAttachments.filter(a => a.type.startsWith('image/')).length;
    const videoCount = allAttachments.filter(a => a.type.startsWith('video/')).length;

    document.getElementById('countAll').textContent = totalCount;
    document.getElementById('countPhotos').textContent = photoCount;
    document.getElementById('countVideos').textContent = videoCount;
}

// 카테고리 제목 업데이트
function updateCategoryTitle(category) {
    const titles = {
        'all': '전체 미디어',
        'image': '사진',
        'video': '영상'
    };
    document.getElementById('categoryTitle').textContent = titles[category] || '전체 미디어';
}

// 로딩 상태 표시
function showLoading() {
    const photoGrid = document.getElementById('photoGrid');
    photoGrid.innerHTML = `
            <div class="loading-container">
                <div class="loading-spinner"></div>
                <div class="loading-text">사진을 불러오는 중...</div>
            </div>
        `;
}

// 빈 상태 표시
function showEmptyState() {
    const photoGrid = document.getElementById('photoGrid');
    photoGrid.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">📷</div>
                <p>등록된 파일이 없습니다.</p>
            </div>
        `;
}

// 에러 상태 표시
function showError(message) {
    const photoGrid = document.getElementById('photoGrid');
    photoGrid.innerHTML = `
        <div class="error-state">
            <div class="error-state-icon">⚠️</div>
            <h5>오류가 발생했습니다</h5>
            <p>${message}</p>
            <button class="retry-btn" onclick="loadAlbumData()">
                다시 시도
            </button>
        </div>
    `;
}

// 파일 크기 포맷팅
function formatFileSize(bytes) {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}

// 날짜 포맷팅
function formatDate(dateString) {
    const date = new Date(dateString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
}