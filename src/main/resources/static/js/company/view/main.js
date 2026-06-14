/* =============================================
   상수
============================================= */
const SPECIALTY_MAP = [
  { no: 1, field: '벌초' },
  { no: 2, field: '운동장 / 스포츠' },
  { no: 3, field: '공공기관 유지관리' },
  { no: 4, field: '정원' },
  { no: 5, field: '실내조경' },
  { no: 6, field: '조경시설물' },
  { no: 99, field: '기타' },
];

const REGEXP = {
  businessNo: /^[0-9]{10}$/,
  companyName: /^[ㄱ-ㅎ가-힣a-zA-Z0-9]{1,100}$/,
  representativeName: /^[ㄱ-ㅎ가-힣a-zA-Z0-9]{1,10}$/,
  phone: /^[0-9]{1,15}$/,
  email: /^(?=.{1,100}$)[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/,
  etcMemo: /^.{1,20}$/,
};

/* =============================================
   페이지 초기화
============================================= */
document.addEventListener('DOMContentLoaded', function () {
  loadCompanyLogo();
  initNavBar();
  initLogoUpload();
  initEditModal();
  initDocRegister();
  initDocList();
  lucide.createIcons();
});

/* =============================================
   업체 로고 로드
============================================= */
function loadCompanyLogo() {
  const img = document.getElementById('cv-company-logo');
  if (!img) return;

  const logoUrl = img.getAttribute('data-logo-url');
  const fallbackUrl = img.getAttribute('data-fallback-url');

  fetch(logoUrl)
    .then(function (res) {
      if (!res.ok) throw new Error('not found');
      return res.blob();
    })
    .then(function (blob) {
      img.src = URL.createObjectURL(blob);
    })
    .catch(function () {
      img.src = fallbackUrl;
    });
}

/* =============================================
   네비 바
============================================= */
function initNavBar() {
  const basicBtn = document.getElementById('cv-nav-basic');
  if (basicBtn) {
    basicBtn.addEventListener('click', function () {
      location.reload();
    });
  }

  document.querySelectorAll('.cv-nav-item[data-url]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      const url = btn.getAttribute('data-url');
      fetchFragment(url);
    });
  });
}

function fetchFragment(url) {
  $.ajax({
    url: url,
    type: 'GET',
    success: function (html) {
      document.getElementById('cv-content-area').innerHTML = html;
      lucide.createIcons();
    },
    error: function (xhr) {
      alert('페이지를 불러오지 못했습니다. (' + xhr.status + ')');
    }
  });
}

/* =============================================
   로고 업로드 (ADMIN)
============================================= */
function initLogoUpload() {
  const uploadBtn = document.getElementById('cv-logo-upload-btn');
  const fileInput = document.getElementById('cv-logo-file-input');
  const confirmArea = document.getElementById('cv-logo-confirm-area');
  const fileNameSpan = document.getElementById('cv-logo-file-name');
  const confirmBtn = document.getElementById('cv-logo-confirm-btn');
  const cancelBtn = document.getElementById('cv-logo-cancel-btn');

  if (!uploadBtn) return;

  uploadBtn.addEventListener('click', function () {
    fileInput.click();
  });

  fileInput.addEventListener('change', function () {
    const file = fileInput.files[0];
    if (!file) return;

    const allowedMime = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    const allowedExt = ['.jpg', '.jpeg', '.png', '.gif', '.webp'];
    const ext = file.name.toLowerCase().substring(file.name.lastIndexOf('.'));
    const maxSize = 10 * 1024 * 1024;

    if (!allowedMime.includes(file.type) && !allowedExt.includes(ext)) {
      alert('jpeg, png, gif, webp 형식의 이미지만 업로드할 수 있습니다.');
      fileInput.value = '';
      return;
    }
    if (file.size > maxSize) {
      alert('10MB 이하의 파일만 업로드할 수 있습니다.');
      fileInput.value = '';
      return;
    }

    fileNameSpan.textContent = file.name;
    confirmArea.style.display = 'flex';
  });

  cancelBtn.addEventListener('click', function () {
    fileInput.value = '';
    confirmArea.style.display = 'none';
    fileNameSpan.textContent = '';
  });

  confirmBtn.addEventListener('click', function () {
    const file = fileInput.files[0];
    if (!file) return;

    const encryptedNo = confirmBtn.getAttribute('data-encrypted-no');
    const formData = new FormData();
    formData.append('file', file);

    $.ajax({
      url: '/file/company/' + encryptedNo + '/logo',
      type: 'POST',
      data: formData,
      processData: false,
      contentType: false,
      success: function () {
        location.reload();
      },
      error: function (xhr) {
        alert('로고 업로드에 실패했습니다. (' + xhr.status + ')');
      }
    });
  });
}

/* =============================================
   업체 정보 수정 모달 (ADMIN)
============================================= */
function initEditModal() {
  const modal = document.getElementById('cv-edit-modal');
  const openBtn = document.getElementById('cv-edit-open-btn');
  const closeBtn = document.getElementById('cv-edit-modal-close');
  const cancelBtn = document.getElementById('cv-edit-cancel-btn');
  const submitBtn = document.getElementById('cv-edit-submit-btn');
  const form = document.getElementById('cv-edit-form');

  if (!modal) return;

  openBtn.addEventListener('click', function () {
    modal.style.display = 'flex';
  });

  function closeModal() {
    modal.style.display = 'none';
  }

  closeBtn.addEventListener('click', closeModal);
  cancelBtn.addEventListener('click', closeModal);
  modal.addEventListener('click', function (e) {
    if (e.target === modal) closeModal();
  });

  // 기타 체크 시 etcMemo 표시
  document.querySelectorAll('.cv-specialty-check').forEach(function (chk) {
    chk.addEventListener('change', function () {
      handleSpecialtyChange();
    });
  });

  function handleSpecialtyChange() {
    const checked = document.querySelectorAll('.cv-specialty-check:checked');

    // 최대 3개 제한
    if (checked.length > 3) {
      this.checked = false;
      alert('주 종목은 최대 3개까지 선택할 수 있습니다.');
      return;
    }

    const etcWrap = document.getElementById('cv-edit-etcMemo-wrap');
    const hasEtc = Array.from(document.querySelectorAll('.cv-specialty-check:checked'))
      .some(function (c) { return c.getAttribute('data-field') === '기타'; });
    etcWrap.style.display = hasEtc ? '' : 'none';
    if (!hasEtc) {
      document.getElementById('cv-edit-etcMemo').value = '';
    }
  }

  // 3개 초과 방지를 이벤트마다 처리 (handleSpecialtyChange에서 this 문제 방지)
  document.querySelectorAll('.cv-specialty-check').forEach(function (chk) {
    chk.addEventListener('change', function () {
      const allChecked = document.querySelectorAll('.cv-specialty-check:checked');
      if (allChecked.length > 3) {
        chk.checked = false;
        alert('주 종목은 최대 3개까지 선택할 수 있습니다.');
      }
      const etcWrap = document.getElementById('cv-edit-etcMemo-wrap');
      const hasEtc = Array.from(document.querySelectorAll('.cv-specialty-check:checked'))
        .some(function (c) { return c.getAttribute('data-field') === '기타'; });
      etcWrap.style.display = hasEtc ? '' : 'none';
      if (!hasEtc) {
        document.getElementById('cv-edit-etcMemo').value = '';
      }
    });
  });

  submitBtn.addEventListener('click', function () {
    if (!validateEditForm()) return;

    const encryptedNo = form.getAttribute('data-encrypted-no');

    const year = document.getElementById('cv-edit-year').value.padStart(4, '0');
    const month = String(document.getElementById('cv-edit-month').value).padStart(2, '0');
    const day = String(document.getElementById('cv-edit-day').value).padStart(2, '0');
    const createdOn = year + '-' + month + '-' + day;

    const selectedOptions = Array.from(document.querySelectorAll('.cv-specialty-check:checked'))
      .map(function (c) { return c.value; });

    const params = new URLSearchParams();
    params.append('companyName', document.getElementById('cv-edit-companyName').value.trim());
    params.append('businessNo', document.getElementById('cv-edit-businessNo').value.trim());
    params.append('representativeName', document.getElementById('cv-edit-representativeName').value.trim());
    params.append('phone', document.getElementById('cv-edit-phone').value.trim());
    params.append('email', document.getElementById('cv-edit-email').value.trim());
    params.append('createdOn', createdOn);
    params.append('status', document.getElementById('cv-edit-status').value);
    selectedOptions.forEach(function (opt) {
      params.append('option', opt);
    });

    const etcMemoWrap = document.getElementById('cv-edit-etcMemo-wrap');
    if (etcMemoWrap.style.display !== 'none') {
      params.append('etcMemo', document.getElementById('cv-edit-etcMemo').value.trim());
    }

    $.ajax({
      url: '/api/company/' + encryptedNo,
      type: 'PUT',
      data: params.toString(),
      contentType: 'application/x-www-form-urlencoded',
      success: function () {
        location.reload();
      },
      error: function (xhr) {
        alert('수정에 실패했습니다. (' + xhr.status + ')');
      }
    });
  });
}

function validateEditForm() {
  let valid = true;

  function setError(id, msg) {
    const el = document.getElementById(id);
    if (el) el.textContent = msg;
  }

  function clearErrors() {
    ['cv-err-companyName', 'cv-err-businessNo', 'cv-err-representativeName',
      'cv-err-phone', 'cv-err-email', 'cv-err-createdOn', 'cv-err-option', 'cv-err-etcMemo']
      .forEach(function (id) { setError(id, ''); });
    document.querySelectorAll('.cv-form-control.is-invalid')
      .forEach(function (el) { el.classList.remove('is-invalid'); });
  }

  function markInvalid(inputId, errId, msg) {
    const input = document.getElementById(inputId);
    if (input) input.classList.add('is-invalid');
    setError(errId, msg);
    valid = false;
  }

  clearErrors();

  const companyName = document.getElementById('cv-edit-companyName').value.trim();
  const businessNo = document.getElementById('cv-edit-businessNo').value.trim();
  const repName = document.getElementById('cv-edit-representativeName').value.trim();
  const phone = document.getElementById('cv-edit-phone').value.trim();
  const email = document.getElementById('cv-edit-email').value.trim();
  const year = document.getElementById('cv-edit-year').value;
  const month = document.getElementById('cv-edit-month').value;
  const day = document.getElementById('cv-edit-day').value;

  if (!REGEXP.companyName.test(companyName))
    markInvalid('cv-edit-companyName', 'cv-err-companyName', '업체명은 한글, 영문, 숫자 1~100자여야 합니다.');
  if (!REGEXP.businessNo.test(businessNo))
    markInvalid('cv-edit-businessNo', 'cv-err-businessNo', '사업자 등록번호는 숫자 10자리여야 합니다.');
  if (!REGEXP.representativeName.test(repName))
    markInvalid('cv-edit-representativeName', 'cv-err-representativeName', '대표 이름은 한글, 영문, 숫자 1~10자여야 합니다.');
  if (!REGEXP.phone.test(phone))
    markInvalid('cv-edit-phone', 'cv-err-phone', '연락처는 숫자 1~15자리여야 합니다.');
  if (email && !REGEXP.email.test(email))
    markInvalid('cv-edit-email', 'cv-err-email', '올바른 이메일 형식이 아닙니다.');

  // 창립일 검증
  const dateStr = year + '-' + String(month).padStart(2, '0') + '-' + String(day).padStart(2, '0');
  const dateObj = new Date(dateStr);
  if (!year || !month || !day || isNaN(dateObj.getTime()) || dateObj >= new Date()) {
    setError('cv-err-createdOn', '올바른 과거 날짜를 입력해주세요.');
    valid = false;
  }

  // 기타 메모
  const etcMemoWrap = document.getElementById('cv-edit-etcMemo-wrap');
  if (etcMemoWrap.style.display !== 'none') {
    const etcMemo = document.getElementById('cv-edit-etcMemo').value.trim();
    if (!REGEXP.etcMemo.test(etcMemo))
      markInvalid('cv-edit-etcMemo', 'cv-err-etcMemo', '기타 내용은 1~20자여야 합니다.');
  }

  return valid;
}

/* =============================================
   서류 등록 (ADMIN)
============================================= */
function initDocRegister() {
  const registerBtn = document.getElementById('cv-doc-register-btn');
  const formArea = document.getElementById('cv-doc-form-area');
  const uploadBtn = document.getElementById('cv-doc-upload-btn');
  const cancelBtn = document.getElementById('cv-doc-cancel-btn');
  const docTypeSelect = document.getElementById('cv-doc-type-select');
  const docTypeInput = document.getElementById('cv-doc-type-input');

  if (!registerBtn) return;

  registerBtn.addEventListener('click', function () {
    formArea.style.display = formArea.style.display === 'none' ? '' : 'none';
  });

  docTypeSelect.addEventListener('change', function () {
    if (docTypeSelect.value === '') {
      // 직접 입력 선택 시 입력칸 표시
      docTypeInput.style.display = '';
      docTypeInput.value = '';
    } else {
      // 선택지 선택 시 입력칸 숨김
      docTypeInput.style.display = 'none';
      docTypeInput.value = '';
    }
  });

  cancelBtn.addEventListener('click', function () {
    formArea.style.display = 'none';
    resetDocForm();
  });

  uploadBtn.addEventListener('click', function () {
    const fileInput = document.getElementById('cv-doc-file-input');
    const docTypeSelect = document.getElementById('cv-doc-type-select');
    const docTypeInput = document.getElementById('cv-doc-type-input');
    const expireYear = document.getElementById('cv-doc-expire-year').value.trim();
    const expireMonth = document.getElementById('cv-doc-expire-month').value.trim();
    const expireDay = document.getElementById('cv-doc-expire-day').value.trim();

    const file = fileInput.files[0];

    if (!file) {
      alert('파일을 선택해주세요.');
      return;
    }

    let docType = '';
    if (docTypeSelect.value !== '') {
      // 드롭다운 선택지 사용
      docType = docTypeSelect.value;
    } else {
      // 직접 입력 사용
      docType = docTypeInput.value.trim();
      if (!docType) {
        alert('서류 종류를 입력해주세요.');
        return;
      }
      if (docType.length > 20) {
        alert('서류 종류는 20자 이내로 입력해주세요.');
        return;
      }
    }

    const encryptedNo = uploadBtn.getAttribute('data-encrypted-no');
    const formData = new FormData();
    formData.append('file', file);
    formData.append('docType', docType);

    if (expireYear || expireMonth || expireDay) {
      if (!expireYear || !expireMonth || !expireDay) {
        alert('만료일의 년/월/일을 모두 입력해주세요.');
        return;
      }
      const y = expireYear;
      const m = String(expireMonth).padStart(2, '0');
      const d = String(expireDay).padStart(2, '0');
      const dateObj = new Date(`${y}-${m}-${d}`);
      if (isNaN(dateObj.getTime())) {
        alert('올바른 만료일을 입력해주세요.');
        return;
      }
      formData.append('expireOn', `${y}-${m}-${d}`);
    }

    $.ajax({
      url: '/file/company/' + encryptedNo + '/doc',
      type: 'POST',
      data: formData,
      processData: false,
      contentType: false,
      success: function () {
        location.reload();
      },
      error: function (xhr) {
        alert('서류 업로드에 실패했습니다. (' + xhr.status + ')');
      }
    });
  });
}

function parseKoreanDate(str) {
  const match = str.match(/(\d{4})년\s*(\d{1,2})월\s*(\d{1,2})일/);
  if (!match) return null;
  const y = match[1];
  const m = String(match[2]).padStart(2, '0');
  const d = String(match[3]).padStart(2, '0');
  const date = new Date(y + '-' + m + '-' + d);
  if (isNaN(date.getTime())) return null;
  return y + '-' + m + '-' + d;
}

function resetDocForm() {
  const fileInput = document.getElementById('cv-doc-file-input');
  const docTypeSelect = document.getElementById('cv-doc-type-select');
  const docTypeInput = document.getElementById('cv-doc-type-input');
  const expireInput = document.getElementById('cv-doc-expire-input');
  if (fileInput) fileInput.value = '';
  if (docTypeSelect) { docTypeSelect.value = ''; }
  if (docTypeInput) docTypeInput.value = '';
  if (expireInput) expireInput.value = '';
}

/* =============================================
   서류 목록 (클릭 → 열기/다운로드, 삭제)
============================================= */
function initDocList() {
  // 서류 항목 클릭 (아이템 영역 클릭 시 파일 열기/다운로드)
  document.querySelectorAll('.cv-doc-item').forEach(function (item) {
    item.addEventListener('click', function (e) {
      // 삭제 버튼 클릭은 이 이벤트가 처리하지 않음
      if (e.target.closest('.cv-doc-delete-btn')) return;

      const encryptedFileNo = item.getAttribute('data-encrypted-file-no');
      const encryptedNo = item.getAttribute('data-encrypted-no');
      openOrDownloadDoc(encryptedNo, encryptedFileNo);
    });
  });

  // 삭제 버튼
  document.querySelectorAll('.cv-doc-delete-btn').forEach(function (btn) {
    btn.addEventListener('click', function (e) {
      e.stopPropagation();
      const encryptedFileNo = btn.getAttribute('data-encrypted-file-no');
      const encryptedNo = btn.getAttribute('data-encrypted-no');

      if (!confirm('이 서류를 삭제하시겠습니까?')) return;

      $.ajax({
        url: '/file/company/' + encryptedNo + '/doc/' + encryptedFileNo,
        type: 'DELETE',
        success: function () {
          location.reload();
        },
        error: function (xhr) {
          alert('서류 삭제에 실패했습니다. (' + xhr.status + ')');
        }
      });
    });
  });
}

function openOrDownloadDoc(encryptedNo, encryptedFileNo) {
  fetch('/file/company/' + encryptedNo + '/doc/' + encryptedFileNo)
    .then(function (res) {
      if (!res.ok) throw new Error('request failed');

      const disposition = res.headers.get('Content-Disposition') || '';
      const contentType = res.headers.get('Content-Type') || '';
      const isAttachment = disposition.toLowerCase().includes('attachment');

      // Content-Disposition에서 파일명 파싱
      let filename = 'download';
      const fnMatch = disposition.match(/filename\*?=['"]?(?:UTF-\d['"]*)?([^;\r\n"']+)['"]?/i);
      if (fnMatch) {
        filename = decodeURIComponent(fnMatch[1].trim());
        console.log(filename);
      }

      return res.blob().then(function (blob) {
        const blobUrl = URL.createObjectURL(new Blob([blob], { type: contentType }));

        if (isAttachment) {
          // 다운로드: <a download="파일명"> 사용
          const a = document.createElement('a');
          a.href = blobUrl;
          a.download = filename;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
        } else {
          // 새창 열기: <a> 태그를 통해 열어야 탭 제목에 파일명 반영 안 되지만,
          // blob URL 자체에 파일명을 심을 수 없으므로
          // 파일명을 URL fragment로 힌트를 주거나, 새 창에서 직접 파일 URL 방식으로 처리
          const a = document.createElement('a');
          a.href = blobUrl;
          a.target = '_blank';
          a.rel = 'noopener noreferrer';
          // download 속성 없이 클릭 → 새창에서 열림
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
        }

        setTimeout(function () { URL.revokeObjectURL(blobUrl); }, 30000);
      });
    })
    .catch(function () {
      alert('파일을 불러오지 못했습니다.');
    });
}