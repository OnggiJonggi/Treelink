(function () {
  'use strict';

  // 정규식
  const REG = {
    COMPANY_NAME: /^[ㄱ-ㅎ가-힣a-zA-Z0-9]{1,100}$/,
    REPRESENTATIVE_NAME: /^[ㄱ-ㅎ가-힣a-zA-Z0-9]{1,10}$/,
    PHONE: /^[0-9]{1,15}$/,
    EMAIL: /^(?=.{1,100}$)[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/,
    ETC_MEMO: /^.{1,20}$/,
    BUSINESS_NO_DIGIT: /^\d{10}$/,
  };

  // csrf
  function getCsrfHeader() {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (token && header) return { [header]: token };
    return {};
  }

  /* ── 에러 표시 헬퍼 ───────────────────────────────── */
  function showError(id, msg) {
    const el = document.getElementById(id);
    if (el) el.textContent = msg || '';
  }
  function clearError(id) { showError(id, ''); }

  function markInvalid(el, errId, msg) {
    el.classList.add('is-invalid');
    showError(errId, msg);
    return false;
  }
  function markValid(el, errId) {
    el.classList.remove('is-invalid');
    clearError(errId);
    return true;
  }

  // 자동 다음 칸 이동 / 숫자만 입력 허용 / 백스페이스 이전 칸 이동
  function initSplitInputs(ids, opts = {}) {
    const inputs = ids.map(id => document.getElementById(id));

    inputs.forEach((inp, idx) => {
      inp.addEventListener('input', function () {
        const filtered = this.value.replace(/\D/g, '');
        this.value = filtered;

        if (filtered.length >= parseInt(this.maxLength, 10) && idx < inputs.length - 1) {
          inputs[idx + 1].focus();
        }
        if (opts.onChange) opts.onChange();
      });

      inp.addEventListener('keydown', function (e) {
        if (e.key === 'Backspace' && this.value === '' && idx > 0) {
          inputs[idx - 1].focus();
        }
      });

      inp.addEventListener('focus', function () { this.select(); });
    });
  }

  // 사업자번호 확인
  initSplitInputs(['bno1', 'bno2', 'bno3']);
  initSplitInputs(['cd-year', 'cd-month', 'cd-day']);

  function validateVerifyForm() {
    let ok = true;

    // 사업자번호
    const bno = (
      document.getElementById('bno1').value +
      document.getElementById('bno2').value +
      document.getElementById('bno3').value
    );
    if (!REG.BUSINESS_NO_DIGIT.test(bno)) {
      ['bno1', 'bno2', 'bno3'].forEach(id => document.getElementById(id).classList.add('is-invalid'));
      showError('err-businessNo', '사업자 번호는 숫자 10자리여야 해요');
      ok = false;
    } else {
      ['bno1', 'bno2', 'bno3'].forEach(id => document.getElementById(id).classList.remove('is-invalid'));
      clearError('err-businessNo');
    }

    // 대표 이름 
    const repEl = document.getElementById('representativeName-verify');
    if (!REG.REPRESENTATIVE_NAME.test(repEl.value.trim())) {
      markInvalid(repEl, 'err-representativeName', '대표 이름은 한글·영문·숫자 1~10자여야 해요');
      ok = false;
    } else {
      markValid(repEl, 'err-representativeName');
    }

    // 창립일 
    const y = document.getElementById('cd-year').value;
    const m = document.getElementById('cd-month').value;
    const d = document.getElementById('cd-day').value;
    let dateOk = false;
    if (y.length === 4 && m.length >= 1 && d.length >= 1) {
      const parsed = new Date(`${y}-${m.padStart(2, '0')}-${d.padStart(2, '0')}`);
      if (!isNaN(parsed.getTime()) && parsed < new Date()) dateOk = true;
    }
    if (!dateOk) {
      ['cd-year', 'cd-month', 'cd-day'].forEach(id => document.getElementById(id).classList.add('is-invalid'));
      showError('err-createdOn', '유효한 과거 날짜를 입력해 주세요');
      ok = false;
    } else {
      ['cd-year', 'cd-month', 'cd-day'].forEach(id => document.getElementById(id).classList.remove('is-invalid'));
      clearError('err-createdOn');
    }

    return ok;
  }

  // 버튼 누르기
  document.getElementById('btn-verify').addEventListener('click', function () {
    if (!validateVerifyForm()) return;

    const bno = (
      document.getElementById('bno1').value +
      document.getElementById('bno2').value.padStart(2, '0') +
      document.getElementById('bno3').value.padStart(2, '0')
    );
    const rep = document.getElementById('representativeName-verify').value.trim();
    const y = document.getElementById('cd-year').value;
    const m = document.getElementById('cd-month').value.padStart(2, '0');
    const d = document.getElementById('cd-day').value.padStart(2, '0');
    const dateStr = `${y}-${m}-${d}`;

    $.ajax({
      url: '/api/company/check-businessno',
      method: 'GET',
      data: {
        businessNo: bno,
        representativeName: rep,
        createdOn: dateStr
      },
      headers: getCsrfHeader(),
      success: function () {
        lockVerifyForm();
        showVerifySuccess();
        transferVerifyDataToMainForm(bno, rep, dateStr);
        revealMainForm();
      },
      error: function (xhr) {
        const msg = xhr.responseJSON?.message || '사업자 번호 확인에 실패했어요. 입력값을 다시 확인해 주세요.';
        showError('err-businessNo', msg);
      }
    });
  });

  function lockVerifyForm() {
    ['bno1', 'bno2', 'bno3'].forEach(id => {
      const el = document.getElementById(id);
      el.setAttribute('readonly', true);
      el.setAttribute('disabled', true);
    });
    document.getElementById('representativeName-verify').setAttribute('disabled', true);
    ['cd-year', 'cd-month', 'cd-day'].forEach(id => {
      const el = document.getElementById(id);
      el.setAttribute('readonly', true);
      el.setAttribute('disabled', true);
    });
  }

  function showVerifySuccess() {
    document.getElementById('verify-pending').style.display = 'none';
    document.getElementById('verify-done').style.display = 'flex';
    lucide.createIcons();
  }

  function transferVerifyDataToMainForm(bno, rep, dateStr) {
    document.getElementById('hidden-businessNo').value = bno;
    document.getElementById('hidden-representativeName').value = rep;
    document.getElementById('hidden-createdOn').value = dateStr;
  }

  function revealMainForm() {
    const mainSection = document.getElementById('section-main');
    mainSection.style.display = '';
    mainSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  /* ================================================================
     사업체 상세 정보
  ================================================================ */

  // 전화번호
  initSplitInputs(['ph1', 'ph2', 'ph3'], { onChange: syncPhone });
  document.getElementById('ph1').addEventListener('input', syncPhone);
  document.getElementById('ph2').addEventListener('input', syncPhone);
  document.getElementById('ph3').addEventListener('input', syncPhone);

  function syncPhone() {
    const v = document.getElementById('ph1').value +
      document.getElementById('ph2').value +
      document.getElementById('ph3').value;
    document.getElementById('hidden-phone').value = v;
  }

  // hidden input 동기화
  function syncEmail() {
    const local = document.getElementById('email-local').value.trim();
    const domain = document.getElementById('email-domain').value.trim();
    document.getElementById('hidden-email').value = (local && domain) ? `${local}@${domain}` : '';
  }
  document.getElementById('email-local').addEventListener('input', syncEmail);
  document.getElementById('email-domain').addEventListener('input', syncEmail);

  // 주 종목
  const MAX_OPTIONS = 3;
  const optionGrid = document.getElementById('option-grid');
  const chips = optionGrid.querySelectorAll('.option-chip');

  function getCheckedCount() {
    return optionGrid.querySelectorAll('.option-check:checked').length;
  }

  chips.forEach(chip => {
    chip.addEventListener('click', function (e) {
      const cb = chip.querySelector('.option-check');
      if (!cb.checked && getCheckedCount() >= MAX_OPTIONS) {
        e.preventDefault();
        showError('err-option', '주 종목은 최대 3개까지 선택할 수 있어요');
        return;
      }
    });

    chip.querySelector('.option-check').addEventListener('change', function () {
      const checked = this.checked;
      chip.classList.toggle('is-selected', checked);
      clearError('err-option');

      const count = getCheckedCount();
      chips.forEach(c => {
        const cCb = c.querySelector('.option-check');
        if (!cCb.checked) {
          c.classList.toggle('is-disabled', count >= MAX_OPTIONS);
        }
      });

      /* 기타(12) 선택 시 etcMemo 활성화 */
      const isEtc = document.getElementById('opt-etc').checked;
      const etcWrap = document.getElementById('etc-memo-wrap');
      const etcInput = document.getElementById('etcMemo');
      if (isEtc) {
        etcWrap.style.display = '';
        etcInput.disabled = false;
        etcInput.focus();
      } else {
        etcWrap.style.display = 'none';
        etcInput.disabled = true;
        etcInput.value = '';
        clearError('err-etcMemo');
      }
    });
  });

  /* ── STEP2 검증 ─────────────────────────────────── */
  function validateMainForm() {
    let ok = true;

    /* 회사 이름 */
    const companyEl = document.getElementById('companyName');
    if (!REG.COMPANY_NAME.test(companyEl.value.trim())) {
      markInvalid(companyEl, 'err-companyName', '회사 이름은 한글·영문·숫자 1~100자여야 해요');
      ok = false;
    } else {
      markValid(companyEl, 'err-companyName');
    }

    /* 전화번호 */
    syncPhone();
    const phoneVal = document.getElementById('hidden-phone').value;
    const ph1El = document.getElementById('ph1');
    if (!REG.PHONE.test(phoneVal)) {
      markInvalid(ph1El, 'err-phone', '전화번호는 숫자 1~15자여야 해요');
      ok = false;
    } else {
      markValid(ph1El, 'err-phone');
    }

    /* 이메일 (선택항목) */
    syncEmail();
    const emailVal = document.getElementById('hidden-email').value;
    const emailLocalEl = document.getElementById('email-local');
    if (emailVal) {
      if (!REG.EMAIL.test(emailVal)) {
        markInvalid(emailLocalEl, 'err-email', '이메일 형식이 올바르지 않아요');
        ok = false;
      } else {
        markValid(emailLocalEl, 'err-email');
      }
    } else {
      markValid(emailLocalEl, 'err-email');
    }

    /* 기타 메모 */
    const isEtc = document.getElementById('opt-etc').checked;
    const etcInput = document.getElementById('etcMemo');
    if (isEtc) {
      if (!REG.ETC_MEMO.test(etcInput.value.trim())) {
        markInvalid(etcInput, 'err-etcMemo', '기타 종목은 1~20자여야 해요');
        ok = false;
      } else {
        markValid(etcInput, 'err-etcMemo');
      }
    }

    return ok;
  }

  /* ── 폼 제출 ────────────────────────────────────── */
  document.getElementById('main-form').addEventListener('submit', function (e) {
    if (!validateMainForm()) {
      e.preventDefault();
      const firstInvalid = this.querySelector('.is-invalid');
      if (firstInvalid) firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  });

})();