/**
 * 모임 검색 페이지
 * - 키워드 기반 모임 검색
 * - 모임 참여 요청 처리
 * - 관심사별 필터링
 */

const groupTypeMap = {
    ONLINE: "온라인",
    OFFLINE: "오프라인",
    HYBRID: "온·오프라인",
}

const groupInterestMap = {
    HOBBY: "취미",
    FAMILY: "가족",
    SCHOOL: "학교",
    BUSINESS: "업무",
    EXERCISE: "운동",
    GAME: "게임",
    STUDY: "스터디",
    FAN: "팬",
    OTHER: "기타",
}

// 현재 검색 상태
let currentKeyword = ""
let currentInterest = ""
let searchCurrentPage = 0

document.addEventListener("DOMContentLoaded", async () => {
    const urlParams = new URLSearchParams(window.location.search)
    currentKeyword = urlParams.get("keyword")
    currentInterest = urlParams.get("interest") || ""
    searchCurrentPage = Number.parseInt(urlParams.get("page")) || 0

    if (!currentKeyword) {
        document.getElementById("keyword-display").textContent = "검색어가 없습니다."
        document.getElementById("result-count").textContent = ""
        showEmptyState("검색어를 입력해주세요.")
        return
    }

    document.getElementById("keyword-display").textContent = `"${currentKeyword}" 검색 결과`

    // 관심사 필터 초기화
    initializeInterestFilter()

    // 관심사 필터 이벤트 리스너 설정
    setupInterestFilterListeners()

    // 검색 실행
    await performSearch()
})

// 관심사 필터 초기화
function initializeInterestFilter() {
    const interestFilters = document.querySelectorAll(".interest-filter")
    interestFilters.forEach((filter) => {
        filter.classList.remove("active")
        if (filter.dataset.interest === currentInterest) {
            filter.classList.add("active")
        }
    })
}

// 관심사 필터 이벤트 리스너 설정
function setupInterestFilterListeners() {
    const interestFilters = document.querySelectorAll(".interest-filter")
    interestFilters.forEach((filter) => {
        filter.addEventListener("click", () => {
            // 활성 상태 변경
            interestFilters.forEach((f) => f.classList.remove("active"))
            filter.classList.add("active")

            // 관심사 필터 변경 및 검색 재실행
            currentInterest = filter.dataset.interest
            searchCurrentPage = 0
            updateURL()
            performSearch()
        })
    })
}

// URL 업데이트
function updateURL() {
    const params = new URLSearchParams()
    params.set("keyword", currentKeyword)
    if (currentInterest) {
        params.set("interest", currentInterest)
    }
    if (searchCurrentPage > 0) {
        params.set("page", searchCurrentPage.toString())
    }

    const newURL = `/test/search?${params.toString()}`
    window.history.replaceState({}, "", newURL)
}

// 검색 실행
async function performSearch() {
    // 로딩 상태 표시
    showLoading()

    try {
        let url = `/api/groups/search?keyword=${encodeURIComponent(currentKeyword)}&page=${searchCurrentPage}`
        if (currentInterest) {
            url += `&interest=${currentInterest}`
        }

        const response = await fetchWithAuth(url)
        const data = await response.json()

        if (!response.ok || !data.result) {
            throw new Error(data.message || "검색 실패")
        }

        const groupList = data.result.content
        const totalPages = data.result.totalPages
        const totalElements = data.result.totalElements
        const currentPageNum = data.result.pageNumber

        document.getElementById("result-count").textContent = `총 ${totalElements}개의 모임을 찾았습니다`

        if (groupList.length === 0) {
            showEmptyState(currentKeyword)
        } else {
            renderGroups(groupList)
            addPagination(currentPageNum, totalPages, currentKeyword, currentInterest)
        }
    } catch (err) {
        document.getElementById("result-count").textContent = "검색 중 오류가 발생했습니다."
        showError("검색 중 오류가 발생했습니다.")
        console.error(err)
    }
}

// main.js와 완전히 동일한 그룹 렌더링 함수
function renderGroups(groups) {
    const container = document.getElementById("groupListContainer")
    container.innerHTML = ""

    groups.forEach((group) => {
        const card = document.createElement("div")
        card.className = "group-card"

        // 이미지
        const img = document.createElement("img")
        img.src = group.groupCoverImageUrl || "https://via.placeholder.com/300x214?text=No+Image"
        img.alt = group.groupName
        card.appendChild(img)

        // 그룹 정보
        const info = document.createElement("div")
        info.className = "group-info"

        const name = document.createElement("h3")
        name.textContent = group.groupName
        info.appendChild(name)

        const meta = document.createElement("div")
        meta.className = "group-meta"
        meta.innerHTML = `
            ${groupTypeMap[group.groupType] || group.groupType} /
            ${groupInterestMap[group.groupInterest] || group.groupInterest} ·
            멤버 ${group.memberCount}명`
        info.appendChild(meta)

        // 가입 상태에 따른 버튼 처리 (main.js와 동일한 인라인 스타일 적용)
        if (group.joinStatus !== "APPROVED") {
            const joinBtn = document.createElement("button")
            // main.js와 동일한 인라인 스타일 적용
            joinBtn.style.marginTop = "8px"
            joinBtn.style.padding = "6px 12px"
            joinBtn.style.border = "none"
            joinBtn.style.borderRadius = "4px"
            joinBtn.style.backgroundColor = "#4CAF50"
            joinBtn.style.color = "#fff"
            joinBtn.style.cursor = "pointer"

            joinBtn.textContent = group.joinStatus === "REQUESTED" ? "가입 요청 중" : "참여하기"

            if (group.joinStatus === "REQUESTED") {
                joinBtn.disabled = true
            }

            joinBtn.addEventListener("click", async (e) => {
                e.stopPropagation()

                if (group.joinStatus === "BLOCKED") {
                    alert("가입 요청할 수 없는 모임입니다.")
                    return
                }

                if (group.joinStatus === "REQUESTED") {
                    alert("이미 가입 요청 중인 모임입니다.")
                    return
                }

                try {
                    const res = await fetchWithAuth(`/api/groups/${group.groupId}/members/requests`, {
                        method: "POST",
                    })
                    const data = await res.json()

                    if (res.ok && data.result) {
                        alert("참여 요청이 완료되었습니다.")
                        joinBtn.disabled = true
                        joinBtn.textContent = "가입 요청 중"
                    } else {
                        alert(data.message || "참여 요청 실패")
                    }
                } catch (err) {
                    console.error("참여 요청 실패", err)
                    alert("참여 요청 중 오류가 발생했습니다.")
                }
            })

            info.appendChild(joinBtn)
        } else {
            // 가입된 그룹은 클릭 시 그룹 페이지로 이동
            card.addEventListener("click", () => {
                window.location.href = `/test/group/main?id=${group.groupId}`
            })
        }

        card.appendChild(info)
        container.appendChild(card)
    })
}

// 로딩 상태 표시
function showLoading() {
    const container = document.getElementById("groupListContainer")
    container.innerHTML = `
        <div class="loading-container">
            <div class="loading-spinner"></div>
            <div class="loading-text">검색 중...</div>
        </div>
    `
}

// 빈 상태 표시
function showEmptyState(keyword) {
    const container = document.getElementById("groupListContainer")
    container.innerHTML = `
        <div class="empty-state">
            <div class="empty-state-icon">🔍</div>
            <h3>"${keyword}"에 대한 검색 결과가 없습니다</h3>
            <p>다른 키워드로 검색해보시거나, 새로운 모임을 만들어보세요!</p>
        </div>
    `
}

// 에러 상태 표시
function showError(message) {
    const container = document.getElementById("groupListContainer")
    container.innerHTML = `
        <div class="empty-state">
            <div class="empty-state-icon">⚠️</div>
            <h3>오류가 발생했습니다</h3>
            <p>${message}</p>
            <button onclick="location.reload()" class="retry-btn">
                다시 시도
            </button>
        </div>
    `
}

// 페이지네이션 (관심사 필터 포함)
function addPagination(currentPageNum, totalPages, keyword, interest) {
    const existing = document.getElementById("pagination")
    if (existing) existing.remove()

    if (totalPages <= 1) return

    const pagination = document.createElement("div")
    pagination.id = "pagination"
    pagination.className = "pagination-container"

    // URL 생성 헬퍼 함수
    const createPageURL = (page) => {
        const params = new URLSearchParams()
        params.set("keyword", keyword)
        if (interest) {
            params.set("interest", interest)
        }
        if (page > 0) {
            params.set("page", page.toString())
        }
        return `/test/search?${params.toString()}`
    }

    // 이전 페이지 버튼
    if (currentPageNum > 0) {
        const prevBtn = createPaginationButton("‹ 이전", () => {
            window.location.href = createPageURL(currentPageNum - 1)
        })
        prevBtn.className = "pagination-btn pagination-prev"
        pagination.appendChild(prevBtn)
    }

    // 페이지 번호 버튼들
    const startPage = Math.max(0, currentPageNum - 2)
    const endPage = Math.min(totalPages - 1, currentPageNum + 2)

    if (startPage > 0) {
        const firstBtn = createPaginationButton("1", () => {
            window.location.href = createPageURL(0)
        })
        firstBtn.className = "pagination-btn"
        pagination.appendChild(firstBtn)

        if (startPage > 1) {
            const dots = document.createElement("span")
            dots.textContent = "..."
            dots.className = "pagination-dots"
            pagination.appendChild(dots)
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        const btn = createPaginationButton(i + 1, () => {
            window.location.href = createPageURL(i)
        })
        btn.className = i === currentPageNum ? "pagination-btn pagination-current" : "pagination-btn"
        pagination.appendChild(btn)
    }

    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            const dots = document.createElement("span")
            dots.textContent = "..."
            dots.className = "pagination-dots"
            pagination.appendChild(dots)
        }

        const lastBtn = createPaginationButton(totalPages, () => {
            window.location.href = createPageURL(totalPages - 1)
        })
        lastBtn.className = "pagination-btn"
        pagination.appendChild(lastBtn)
    }

    // 다음 페이지 버튼
    if (currentPageNum < totalPages - 1) {
        const nextBtn = createPaginationButton("다음 ›", () => {
            window.location.href = createPageURL(currentPageNum + 1)
        })
        nextBtn.className = "pagination-btn pagination-next"
        pagination.appendChild(nextBtn)
    }

    document.querySelector("main").appendChild(pagination)
}

// 페이지네이션 버튼 생성 헬퍼 함수
function createPaginationButton(text, onClick) {
    const btn = document.createElement("button")
    btn.textContent = text
    btn.addEventListener("click", onClick)
    return btn
}
