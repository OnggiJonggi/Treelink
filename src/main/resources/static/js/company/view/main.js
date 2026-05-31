(function () {
  'use strict';

  /* ── 정규식 ─────────────────────────────────── */
  const REG = {
    BUSINESS_NO: /^[0-9]{10}$/,
    COMPANY_NAME: /^[ㄱ-ㅎ가-힣a-zA-Z0-9]{1,100}$/,
    REPRESENTATIVE_NAME: /^[ㄱ-ㅎ가-힣a-zA-Z0-9]{1,10}$/,
    PHONE: /^[0-9]{1,15}$/,
    EMAIL: /^(?=.{1,100}$)[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/,
    ETC_MEMO: /^.{1,20}$/,
    DOC_TYPE: /^[^&<>"';]{1,20}$/,
  };

  /* ── OPTION 라벨 <-> 숫자 매핑 ─────────────── */
  const OPTION_MAP = {
    '벌초': 1, '운동장 / 스포츠': 2, '공공기관 유지관리': 3,
    '정원': 4, '실내조경': 5, '조경시설물': 6, '기타': 99,
  };

  /* ── CSRF ────────────────────────────────────── */
  function getCsrfHeader() {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (token && header) return { [header]: token };
    return {};
  }

  /* ── 에러 헬퍼 ──────────────────────────────── */
  function showError(id, msg) { const el = document.getElementById(id); if (el) el.textContent = msg || ''; }
  function clearError(id) { showError(id, ''); }
  function markInvalid(el, errId, msg) { el.classList.add('is-invalid'); showError(errId, msg); }
  function markValid(el, errId) { el.classList.remove('is-invalid'); clearError(errId); }

  /* ── 네비 조각 로드 ─────────────────────────── */
  window.basicLoadFragment = function (anchor) {
    const url = anchor.getAttribute('data-url');
    const target = document.getElementById(anchor.getAttribute('data-target'));
    if (!url || !target) return;
    document.querySelectorAll('.basic-nav-item').forEach(a => a.classList.remove('is-active'));
    anchor.classList.add('is-active');
    $.ajax({
      url, method: 'GET',
      success: function (html) { target.innerHTML = html; lucide.createIcons(); },
      error: function () { target.innerHTML = '<p style="padding:2rem;color:#a12c2c;">콘텐츠를 불러오지 못했습니다.</p>'; }
    });
  };

  /* ============================================================
     수정 모드
  ============================================================ */
  window.basicEnterEditMode = function () {
    ['businessNo', 'representativeName', 'phone', 'email', 'createdOn'].forEach(id => {
      document.getElementById('basic-val-' + id).style.display = 'none';
      document.getElementById('basic-inp-' + id).style.display = '';
    });
    const statusVal = document.getElementById('basic-val-status');
    const statusInp = document.getElementById('basic-inp-status');
    if (statusVal) statusVal.style.display = 'none';
    if (statusInp) statusInp.style.display = '';
    document.getElementById('basic-val-option').style.display = 'none';
    document.getElementById('basic-edit-option').style.display = '';
    initOptionCheckboxes();
    document.getElementById('basic-btn-edit').style.display = 'none';
    document.getElementById('basic-edit-controls').style.display = '';
  };

  window.basicCancelEdit = function () { exitEditMode(); };

  function exitEditMode() {
    ['businessNo', 'representativeName', 'phone', 'email', 'createdOn'].forEach(id => {
      document.getElementById('basic-val-' + id).style.display = '';
      const inp = document.getElementById('basic-inp-' + id);
      inp.style.display = 'none'; inp.classList.remove('is-invalid');
      clearError('basic-err-' + id);
    });
    const statusVal = document.getElementById('basic-val-status');
    const statusInp = document.getElementById('basic-inp-status');
    if (statusVal) statusVal.style.display = '';
    if (statusInp) { statusInp.style.display = 'none'; statusInp.classList.remove('is-invalid'); }
    document.getElementById('basic-val-option').style.display = '';
    document.getElementById('basic-edit-option').style.display = 'none';
    document.getElementById('basic-btn-edit').style.display = '';
    document.getElementById('basic-edit-controls').style.display = 'none';
    ['basic-err-option', 'basic-err-etcMemo', 'basic-err-save'].forEach(clearError);
  }

  function initOptionCheckboxes() {
    const hiddenOpts = document.getElementById('basic-hidden-options');
    const currentLabels = hiddenOpts ? hiddenOpts.value.split(',').map(s => s.trim()).filter(Boolean) : [];
    const currentNums = currentLabels.map(label => OPTION_MAP[label]).filter(Boolean);
    document.querySelectorAll('.basic-option-check').forEach(cb => {
      const checked = currentNums.includes(parseInt(cb.value));
      cb.checked = checked;
      cb.closest('.basic-option-chip-edit')?.classList.toggle('is-selected', checked);
    });
    const etcChecked = document.getElementById('basic-opt-etc')?.checked;
    const etcWrap = document.getElementById('basic-etc-memo-wrap');
    const etcInp = document.getElementById('basic-inp-etcMemo');
    if (etcWrap) etcWrap.style.display = etcChecked ? '' : 'none';
    if (etcInp) etcInp.disabled = !etcChecked;
    refreshChipDisabledState();
  }

  function getCheckedCount() { return document.querySelectorAll('.basic-option-check:checked').length; }

  function refreshChipDisabledState() {
    const count = getCheckedCount();
    document.querySelectorAll('.basic-option-chip-edit').forEach(chip => {
      const cb = chip.querySelector('.basic-option-check');
      if (!cb.checked) chip.classList.toggle('is-disabled', count >= 3);
    });
  }

  document.addEventListener('click', function (e) {
    const chip = e.target.closest('.basic-option-chip-edit');
    if (!chip) return;
    const cb = chip.querySelector('.basic-option-check');
    if (!cb) return;
    if (!cb.checked && getCheckedCount() >= 3) {
      e.preventDefault();
      showError('basic-err-option', '주 종목은 최대 3개까지 선택할 수 있어요');
    }
  });

  document.addEventListener('change', function (e) {
    if (!e.target.classList.contains('basic-option-check')) return;
    e.target.closest('.basic-option-chip-edit')?.classList.toggle('is-selected', e.target.checked);
    clearError('basic-err-option');
    refreshChipDisabledState();
    const etcChecked = document.getElementById('basic-opt-etc')?.checked;
    const etcWrap = document.getElementById('basic-etc-memo-wrap');
    const etcInp = document.getElementById('basic-inp-etcMemo');
    if (etcWrap) etcWrap.style.display = etcChecked ? '' : 'none';
    if (etcInp) {
      etcInp.disabled = !etcChecked;
      if (!etcChecked) { etcInp.value = ''; clearError('basic-err-etcMemo'); }
      else etcInp.focus();
    }
  });

  function validateEditForm() {
    let ok = true;
    const checks = [
      { id: 'basic-inp-businessNo', errId: 'basic-err-businessNo', reg: REG.BUSINESS_NO, msg: '사업자 번호는 숫자 10자리여야 해요' },
      { id: 'basic-inp-representativeName', errId: 'basic-err-representativeName', reg: REG.REPRESENTATIVE_NAME, msg: '대표 이름은 한글·영문·숫자 1~10자여야 해요' },
      { id: 'basic-inp-phone', errId: 'basic-err-phone', reg: REG.PHONE, msg: '전화번호는 숫자 1~15자여야 해요' },
    ];
    checks.forEach(({ id, errId, reg, msg }) => {
      const el = document.getElementById(id);
      if (!el || el.style.display === 'none') return;
      if (!reg.test(el.value.trim())) { markInvalid(el, errId, msg); ok = false; }
      else markValid(el, errId);
    });
    const emailEl = document.getElementById('basic-inp-email');
    if (emailEl && emailEl.style.display !== 'none') {
      const v = emailEl.value.trim();
      if (v && !REG.EMAIL.test(v)) { markInvalid(emailEl, 'basic-err-email', '이메일 형식이 올바르지 않아요'); ok = false; }
      else markValid(emailEl, 'basic-err-email');
    }
    const dateEl = document.getElementById('basic-inp-createdOn');
    if (dateEl && dateEl.style.display !== 'none') {
      if (!dateEl.value || new Date(dateEl.value) >= new Date()) {
        markInvalid(dateEl, 'basic-err-createdOn', '유효한 과거 날짜를 선택해 주세요'); ok = false;
      } else markValid(dateEl, 'basic-err-createdOn');
    }
    const etcChecked = document.getElementById('basic-opt-etc')?.checked;
    const etcInp = document.getElementById('basic-inp-etcMemo');
    if (etcChecked && etcInp) {
      if (!REG.ETC_MEMO.test(etcInp.value.trim())) { markInvalid(etcInp, 'basic-err-etcMemo', '기타 종목은 1~20자여야 해요'); ok = false; }
      else markValid(etcInp, 'basic-err-etcMemo');
    }
    return ok;
  }

  window.basicSaveEdit = function () {
    clearError('basic-err-save');
    if (!validateEditForm()) return;
    const companyUuid = document.getElementById('basic-hidden-companyUuid').value;
    const checkedNums = [...document.querySelectorAll('.basic-option-check:checked')].map(cb => parseInt(cb.value));
    const etcChecked = document.getElementById('basic-opt-etc')?.checked;
    const etcMemo = etcChecked ? (document.getElementById('basic-inp-etcMemo')?.value.trim() || '') : '';
    const emailVal = document.getElementById('basic-inp-email')?.value.trim() || null;
    const payload = {
      companyUuid,
      businessNo: document.getElementById('basic-inp-businessNo')?.value.trim() || '',
      representativeName: document.getElementById('basic-inp-representativeName')?.value.trim() || '',
      phone: document.getElementById('basic-inp-phone')?.value.trim() || '',
      email: emailVal || null,
      createdOn: document.getElementById('basic-inp-createdOn')?.value || '',
      option: checkedNums,
      etcMemo: etcMemo || null,
      status: document.getElementById('basic-inp-status')?.value || '',
    };
    $.ajax({
      url: '/api/company/' + companyUuid, method: 'PUT',
      contentType: 'application/json', data: JSON.stringify(payload),
      headers: getCsrfHeader(),
      success: function () {
        const hiddenOpts = document.getElementById('basic-hidden-options');
        if (hiddenOpts) {
          const reverseMap = {};
          Object.entries(OPTION_MAP).forEach(([k, v]) => { reverseMap[v] = k; });
          hiddenOpts.value = checkedNums.map(n => reverseMap[n] || '').filter(Boolean).join(',');
        }
        refreshDisplayValues();
        exitEditMode();
      },
      error: function () { showError('basic-err-save', '수정에 실패했습니다'); }
    });
  };

  function refreshDisplayValues() {
    [['businessNo', v => v.length === 10 ? v.slice(0, 3) + '-' + v.slice(3, 5) + '-' + v.slice(5) : v],
    ['representativeName', v => v], ['phone', v => v],
    ['email', v => v || '-'],
    ['createdOn', v => {
      if (!v) return v; const d = new Date(v);
      return d.getFullYear() + '년 ' + String(d.getMonth() + 1).padStart(2, '0') + '월 ' + String(d.getDate()).padStart(2, '0') + '일';
    }]
    ].forEach(([id, fmt]) => {
      const inp = document.getElementById('basic-inp-' + id);
      const val = document.getElementById('basic-val-' + id);
      if (inp && val) val.textContent = fmt(inp.value.trim());
    });
    const reverseMap = {};
    Object.entries(OPTION_MAP).forEach(([k, v]) => { reverseMap[v] = k; });
    const checkedNums = [...document.querySelectorAll('.basic-option-check:checked')].map(cb => parseInt(cb.value));
    const etcChecked = document.getElementById('basic-opt-etc')?.checked;
    const etcMemo = etcChecked ? (document.getElementById('basic-inp-etcMemo')?.value.trim() || '') : '';
    const valOpt = document.getElementById('basic-val-option');
    if (valOpt) {
      valOpt.innerHTML = '';
      checkedNums.forEach(num => {
        const label = reverseMap[num] || '';
        const chip = document.createElement('span');
        chip.className = 'basic-option-chip';
        chip.textContent = label === '기타' ? (etcMemo ? '기타 - ' + etcMemo : '기타') : label;
        valOpt.appendChild(chip);
      });
    }
    const statusInp = document.getElementById('basic-inp-status');
    const statusVal = document.getElementById('basic-val-status');
    if (statusInp && statusVal) {
      const s = statusInp.value;
      statusVal.className = 'basic-status-badge';
      if (s === 'ACTIVE') { statusVal.classList.add('status-active'); statusVal.textContent = '정상'; }
      else if (s === 'SUSPENDED') { statusVal.classList.add('status-suspended'); statusVal.textContent = '정지'; }
      else { statusVal.classList.add('status-terminated'); statusVal.textContent = '종료'; }
    }
  }

  /* ============================================================
     서류 관련
  ============================================================ */
  window.basicToggleDocUpload = function () {
    const form = document.getElementById('basic-doc-upload-form');
    if (!form) return;
    form.style.display = (form.style.display === 'none' || !form.style.display) ? '' : 'none';
    lucide.createIcons();
  };

  window.basicOnFileSelected = function () {
    const fi = document.getElementById('basic-doc-file-input');
    const ba = document.getElementById('basic-doc-upload-btn-area');
    if (fi && ba) ba.style.display = fi.files.length > 0 ? '' : 'none';
  };

  window.basicUploadDoc = function () {
    ['basic-err-docType', 'basic-err-docExpire', 'basic-err-upload'].forEach(clearError);
    const companyUuid = document.getElementById('basic-hidden-companyUuid').value;
    const docTypeInput = document.getElementById('basic-doc-type-input');
    const expireInput = document.getElementById('basic-doc-expire-input');
    const fileInput = document.getElementById('basic-doc-file-input');
    let valid = true;
    if (!REG.DOC_TYPE.test(docTypeInput.value.trim())) {
      markInvalid(docTypeInput, 'basic-err-docType', '서류 종류는 1~20자, 특수문자(&<>"\'';) 제외'); valid = false;
    } else markValid(docTypeInput, 'basic-err-docType');
    if (expireInput.value) {
      const tomorrow = new Date(); tomorrow.setDate(tomorrow.getDate() + 1); tomorrow.setHours(0, 0, 0, 0);
      if (new Date(expireInput.value) < tomorrow) {
        markInvalid(expireInput, 'basic-err-docExpire', '만료일은 내일 이후여야 해요'); valid = false;
      } else markValid(expireInput, 'basic-err-docExpire');
    }
    if (!fileInput.files[0]) { showError('basic-err-upload', '파일을 선택해 주세요'); valid = false; }
    if (!valid) return;
    const formData = new FormData();
    formData.append('docType', docTypeInput.value.trim());
    formData.append('file', fileInput.files[0]);
    if (expireInput.value) formData.append('expireOn', expireInput.value);
    $.ajax({
      url: '/api/company/' + companyUuid + '/doc', method: 'POST',
      data: formData, processData: false, contentType: false, headers: getCsrfHeader(),
      success: function () { location.href = '/company/' + companyUuid; },
      error: function (xhr) {
        showError('basic-err-upload', xhr.status === 403 ? '파일 혹은 서류 종류가 잘못되었습니다' : '업로드에 실패했습니다');
      }
    });
  };

  window.basicViewDoc = function (btn) {
    const card = btn.closest('.basic-doc-card');
    const encryptedNo = card.dataset.encryptedNo, companyUuid = card.dataset.companyUuid;
    if (!encryptedNo || !companyUuid) return;
    window.open('/api/company/' + companyUuid + '/doc/' + encryptedNo
      + '?companyUuid=' + encodeURIComponent(companyUuid)
      + '&encryptedDocNo=' + encodeURIComponent(encryptedNo), '_blank');
  };

  window.basicDeleteDoc = function (btn) {
    if (!confirm('이 서류를 삭제하시겠습니까?')) return;
    const card = btn.closest('.basic-doc-card');
    const encryptedNo = card.dataset.encryptedNo, companyUuid = card.dataset.companyUuid;
    if (!encryptedNo || !companyUuid) return;
    $.ajax({
      url: '/api/company/' + companyUuid + '/doc/' + encryptedNo, method: 'DELETE',
      data: { companyUuid, encryptedDocNo: encryptedNo }, headers: getCsrfHeader(),
      complete: function (xhr) {
        if (xhr.status === 204) location.href = '/company/' + companyUuid;
        else alert('서류 삭제에 실패했습니다.');
      }
    });
  };

})();