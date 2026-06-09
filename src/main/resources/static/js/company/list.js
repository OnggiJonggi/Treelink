/* =============================================
   업체 목록 페이지 JS
============================================= */

// ADMIN 권한 여부 (Thymeleaf 에서 data 속성으로 주입된 값 재활용)
const isAdmin = document.getElementById('isAdminFlag').dataset.admin === 'true';

// 주 종목 라디오 변경 → 기타 메모 입력칸 토글
document.querySelectorAll('input[name="option"]').forEach(function (radio) {
  radio.addEventListener('change', function () {
    toggleEtcMemo(this.value);
  });
});

function toggleEtcMemo(value) {
  const group = document.getElementById('etcMemoGroup');
  if (value === '기타') {
    group.style.display = 'flex';
  } else {
    group.style.display = 'none';
    document.getElementById('etcMemo').value = '';
  }
}

// 페이지 진입 시 기타 선택 상태라면 즉시 표시
(function () {
  const checked = document.querySelector('input[name="option"]:checked');
  if (checked && checked.value === '기타') {
    toggleEtcMemo('기타');
  }
})();

// 폼 제출 → 검색 (page=1 초기화)
document.getElementById('searchForm').addEventListener('submit', function (e) {
  e.preventDefault();
  document.getElementById('pageInput').value = '1';
  fetchCompanies(buildParams());
});

function buildParams(page) {
  const form = document.getElementById('searchForm');
  const formData = new FormData(form);
  const params = {};
  for (const [key, value] of formData.entries()) {
    if (value !== null && value !== '') {
      params[key] = value;
    }
  }
  if (page !== undefined) params.page = page;
  return params;
}

function movePage(pageNum) {
  document.getElementById('pageInput').value = pageNum;
  fetchCompanies(buildParams(pageNum));
}

function goToDetail(card) {
  const encNo = card.dataset.enc;
  if (encNo) window.location.href = '/company/' + encNo;
}

function fetchCompanies(params) {
  $.ajax({
    url: '/api/company',
    method: 'GET',
    data: params,
    success: function (result) {
      renderCompanyGrid(result.list);
      renderTotalCount(result.totalCount);
      renderPagination(result, params.page || 1);
      lucide.createIcons();
    },
    error: function (xhr) {
      console.error('업체 목록 조회 실패', xhr);
    }
  });
}

/* =============================================
   렌더링 함수
============================================= */

function getStatusBadge(status) {
  const map = {
    'ACTIVE': { label: '정상', cls: 'badge-active' },
    'SUSPENDED': { label: '정지', cls: 'badge-suspended' },
    'TERMINATED': { label: '종료', cls: 'badge-terminated' }
  };
  const s = map[status];
  if (!s) return '';
  return `<span class="badge ${s.cls}">${s.label}</span>`;
}

function formatDate(dateStr) {
  if (!dateStr) return '-';
  // ISO 형식 "yyyy-MM-dd" 또는 배열 [yyyy, MM, dd] 대응
  let d;
  if (Array.isArray(dateStr)) {
    d = new Date(dateStr[0], dateStr[1] - 1, dateStr[2]);
  } else {
    d = new Date(dateStr);
  }
  if (isNaN(d)) return dateStr;
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yyyy}년 ${mm}월 ${dd}일`;
}

function getOptionBadges(options, etcMemo) {
  if (!options || options.length === 0) {
    return `<span class="option-badge option-badge-empty">종목 없음</span>`;
  }
  return options.slice(0, 3).map(function (opt) {
    const label = (opt === '기타' && etcMemo) ? `기타 - ${escapeHtml(etcMemo)}` : escapeHtml(opt);
    return `<span class="option-badge">${label}</span>`;
  }).join('');
}

function renderCompanyGrid(list) {
  const grid = document.getElementById('companyGrid');
  grid.innerHTML = '';

  if (!list || list.length === 0) {
    grid.innerHTML = `
      <div class="empty-state">
        <i data-lucide="building-2"></i>
        <p>검색 결과가 없습니다.</p>
      </div>`;
    return;
  }

  list.forEach(function (company) {
    const card = document.createElement('div');
    card.className = 'company-card';
    card.dataset.enc = company.encryptedCompanyNo;
    card.setAttribute('onclick', 'goToDetail(this)');

    const statusHtml = isAdmin ? `<div class="company-status">${getStatusBadge(company.status)}</div>` : '';

    card.innerHTML = `
      <div class="company-card-top">
        <div class="company-avatar">
          <i data-lucide="building-2"></i>
        </div>
        <div class="company-info">
          <span class="company-name">${escapeHtml(company.companyName)}</span>
          <span class="company-rep">
            <i data-lucide="user" style="width:12px;height:12px;"></i>
            ${escapeHtml(company.representativeName)}
          </span>
          <span class="company-bizno">${escapeHtml(company.businessNo)}</span>
        </div>
        ${statusHtml}
      </div>
      <div class="company-card-mid">
        <div class="company-meta-item">
          <i data-lucide="phone"></i>
          <span>${escapeHtml(company.phone || '-')}</span>
        </div>
        <div class="company-meta-item">
          <i data-lucide="mail"></i>
          <span>${escapeHtml(company.email || '-')}</span>
        </div>
        <div class="company-meta-item">
          <i data-lucide="calendar"></i>
          <span>${formatDate(company.createdOn)}</span>
        </div>
      </div>
      <div class="company-card-bottom">
        ${getOptionBadges(company.option, company.etcMemo)}
      </div>`;

    grid.appendChild(card);
  });
}

function renderTotalCount(totalCount) {
  const el = document.querySelector('.result-count strong');
  if (el) el.textContent = totalCount;
}

function renderPagination(result, currentPage) {
  const nav = document.getElementById('pagination');
  if (!nav) return;
  if (result.totalPage <= 0) { nav.innerHTML = ''; return; }

  currentPage = parseInt(currentPage) || 1;
  let html = '';

  html += `<button class="page-btn page-nav" ${!result.hasPrev ? 'disabled' : ''}
              onclick="movePage(${result.startPage - 1})">
              <i data-lucide="chevron-left"></i>
           </button>`;

  for (let i = result.startPage; i <= result.endPage; i++) {
    const isActive = i === currentPage ? ' active' : '';
    html += `<button class="page-btn${isActive}" onclick="movePage(${i})">${i}</button>`;
  }

  html += `<button class="page-btn page-nav" ${!result.hasNext ? 'disabled' : ''}
              onclick="movePage(${result.endPage + 1})">
              <i data-lucide="chevron-right"></i>
           </button>`;

  nav.innerHTML = html;
}

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}