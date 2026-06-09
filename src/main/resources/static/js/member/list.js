/* =============================================
   회원 목록 페이지 JS
============================================= */

// 현재 검색 파라미터 저장
let currentParams = {};

/**
 * 폼 제출 이벤트 → 검색 실행 (page=1 초기화)
 */
document.getElementById('searchForm').addEventListener('submit', function (e) {
  e.preventDefault();
  document.getElementById('pageInput').value = '1';
  fetchMembers(buildParams());
});

/**
 * 폼에서 파라미터 객체 생성 (빈 값 제외)
 */
function buildParams(page) {
  const form = document.getElementById('searchForm');
  const formData = new FormData(form);
  const params = {};

  for (const [key, value] of formData.entries()) {
    if (value !== null && value !== '') {
      params[key] = value;
    }
  }

  if (page !== undefined) {
    params.page = page;
  }

  return params;
}

/**
 * 페이지 이동
 */
function movePage(pageNum) {
  document.getElementById('pageInput').value = pageNum;
  const params = buildParams(pageNum);
  fetchMembers(params);
}

/**
 * 회원 상세 페이지 이동
 */
function goToDetail(card) {
  const encNo = card.dataset.enc;
  if (encNo) {
    window.location.href = '/member/' + encNo;
  }
}

/**
 * Ajax 요청 → 회원 목록 조회
 */
function fetchMembers(params) {
  currentParams = params;

  $.ajax({
    url: '/api/member',
    method: 'GET',
    data: params,
    success: function (result) {
      renderMemberGrid(result.list);
      renderTotalCount(result.totalCount);
      renderPagination(result, params.page || 1);
      lucide.createIcons();
    },
    error: function (xhr) {
      console.error('회원 목록 조회 실패', xhr);
    }
  });
}

/* =============================================
   렌더링 함수
============================================= */

/**
 * 상태 → 한글 + CSS 클래스
 */
function getStatusBadge(status) {
  const map = {
    'ACTIVE': { label: '정상', cls: 'badge-active' },
    'SUSPENDED': { label: '정지', cls: 'badge-suspended' },
    'DELETED': { label: '탈퇴', cls: 'badge-deleted' }
  };
  const s = map[status];
  if (!s) return '';
  return `<span class="badge ${s.cls}">${s.label}</span>`;
}

/**
 * 권한 배열 → role-badge HTML (ROLE_ prefix 제거)
 */
function getRoleBadges(roles) {
  if (!roles || roles.length === 0) return '';
  return roles.map(r => {
    const label = r.replace('ROLE_', '');
    return `<span class="role-badge">${label}</span>`;
  }).join('');
}

/**
 * 회원 카드 그리드 렌더링
 */
function renderMemberGrid(list) {
  const grid = document.getElementById('memberGrid');
  grid.innerHTML = '';

  if (!list || list.length === 0) {
    grid.innerHTML = `
            <div class="empty-state">
                <i data-lucide="users"></i>
                <p>검색 결과가 없습니다.</p>
            </div>`;
    return;
  }

  list.forEach(function (member) {
    const phone = (member.phone && member.phone.trim() !== '') ? member.phone : '-';
    const card = document.createElement('div');
    card.className = 'member-card';
    card.dataset.enc = member.encryptedMemberNo;
    card.setAttribute('onclick', 'goToDetail(this)');

    card.innerHTML = `
            <div class="member-card-top">
                <div class="member-avatar">
                    <i data-lucide="user"></i>
                </div>
                <div class="member-info">
                    <span class="member-userid">${escapeHtml(member.userId)}</span>
                    <span class="member-name">${escapeHtml(member.name)}</span>
                    <span class="member-nickname">@${escapeHtml(member.nickname)}</span>
                </div>
                <div class="member-status">
                    ${getStatusBadge(member.status)}
                </div>
            </div>
            <div class="member-card-bottom">
                <div class="member-phone">
                    <i data-lucide="phone"></i>
                    <span>${escapeHtml(phone)}</span>
                </div>
                <div class="member-roles">
                    ${getRoleBadges(member.role)}
                </div>
            </div>`;

    grid.appendChild(card);
  });
}

/**
 * 총 검색 수 업데이트
 */
function renderTotalCount(totalCount) {
  const el = document.querySelector('.result-count strong');
  if (el) el.textContent = totalCount;
}

/**
 * 페이징 바 렌더링
 */
function renderPagination(result, currentPage) {
  const nav = document.getElementById('pagination');
  if (!nav) return;

  if (result.totalPage <= 0) {
    nav.innerHTML = '';
    return;
  }

  currentPage = parseInt(currentPage) || 1;
  let html = '';

  // 이전 블록 버튼
  html += `<button class="page-btn page-nav" ${!result.hasPrev ? 'disabled' : ''}
                onclick="movePage(${result.startPage - 1})">
                <i data-lucide="chevron-left"></i>
             </button>`;

  // 페이지 번호
  for (let i = result.startPage; i <= result.endPage; i++) {
    const isActive = i === currentPage ? ' active' : '';
    html += `<button class="page-btn${isActive}" onclick="movePage(${i})">${i}</button>`;
  }

  // 다음 블록 버튼
  html += `<button class="page-btn page-nav" ${!result.hasNext ? 'disabled' : ''}
                onclick="movePage(${result.endPage + 1})">
                <i data-lucide="chevron-right"></i>
             </button>`;

  nav.innerHTML = html;
}

/**
 * XSS 방지용 HTML 이스케이프
 */
function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}