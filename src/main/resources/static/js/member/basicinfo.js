document.addEventListener('DOMContentLoaded', function () {

  const encryptedMemberNo = document.getElementById('encryptedMemberNo').value;

  // ─── 연락처 초기값 세팅 ───
  const rawPhone = document.querySelector('.info-value[data-phone]');
  // 뷰에서 phone 값을 phone1/2/3에 분리해서 채우기
  const phoneText = (function () {
    const rows = document.querySelectorAll('.info-row');
    for (const row of rows) {
      const label = row.querySelector('.info-label');
      if (label && label.textContent.includes('연락처')) {
        const val = row.querySelector('.info-value');
        return val ? val.textContent.trim() : '';
      }
    }
    return '';
  })();

  function initPhoneFields(phoneStr) {
    const parts = phoneStr.split('-');
    if (parts.length === 3) {
      document.getElementById('phone1').value = parts[0];
      document.getElementById('phone2').value = parts[1];
      document.getElementById('phone3').value = parts[2];
    }
  }
  initPhoneFields(phoneText);

  // ─── 숫자만 입력 ───
  ['phone1', 'phone2', 'phone3'].forEach(id => {
    document.getElementById(id).addEventListener('input', function () {
      this.value = this.value.replace(/\D/g, '');
    });
  });

  // ─── 모달 열기/닫기 헬퍼 ───
  function openModal(id) {
    document.getElementById(id).style.display = 'flex';
  }

  function closeModal(id) {
    document.getElementById(id).style.display = 'none';
  }

  // 배경 클릭 시 닫기
  document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
    backdrop.addEventListener('click', function (e) {
      if (e.target === this) this.style.display = 'none';
    });
  });


  // ===================== 정보 수정 모달 =====================
  document.getElementById('btnOpenUpdateModal').addEventListener('click', () => openModal('updateModal'));
  document.getElementById('btnCloseUpdateModal').addEventListener('click', () => closeModal('updateModal'));
  document.getElementById('btnCancelUpdate').addEventListener('click', () => closeModal('updateModal'));

  // 별명 중복 확인 상태
  let originalNickname = document.getElementById('inputNickname').value.trim();
  let nicknameChecked = true; // 초기값과 동일하므로 통과

  document.getElementById('inputNickname').addEventListener('input', function () {
    const currentVal = this.value.trim();
    if (currentVal === originalNickname) {
      nicknameChecked = true;
      showFeedback('nicknameFeedback', '', '');
    } else {
      nicknameChecked = false;
      showFeedback('nicknameFeedback', '별명 중복 확인이 필요합니다.', 'danger');
    }
  });

  document.getElementById('btnCheckNickname').addEventListener('click', function () {
    const nickname = document.getElementById('inputNickname').value.trim();
    if (!nickname) {
      showFeedback('nicknameFeedback', '별명을 입력해주세요.', 'danger');
      return;
    }
    $.ajax({
      url: '/api/member/check-updatednickname',
      method: 'GET',
      data: { nickname: nickname, encryptedMemberNo: encryptedMemberNo },
      success: function () {
        nicknameChecked = true;
        showFeedback('nicknameFeedback', '사용 가능한 별명입니다.', 'success');
      },
      error: function () {
        nicknameChecked = false;
        showFeedback('nicknameFeedback', '이미 사용 중인 별명입니다.', 'danger');
      }
    });
  });

  // 비밀번호 확인
  document.getElementById('newPwdConfirm').addEventListener('input', validatePwd);
  document.getElementById('newPwd').addEventListener('input', function () {
    if (document.getElementById('newPwdConfirm').value) validatePwd();
  });

  function validatePwd() {
    const p1 = document.getElementById('newPwd').value;
    const p2 = document.getElementById('newPwdConfirm').value;
    if (!p1 && !p2) {
      showFeedback('pwdFeedback', '', '');
      return true;
    }
    if (p1 !== p2) {
      showFeedback('pwdFeedback', '비밀번호가 일치하지 않습니다.', 'danger');
      return false;
    }
    showFeedback('pwdFeedback', '비밀번호가 일치합니다.', 'success');
    return true;
  }

  // 연락처 유효성
  function validatePhone() {
    const p1 = document.getElementById('phone1').value.trim();
    const p2 = document.getElementById('phone2').value.trim();
    const p3 = document.getElementById('phone3').value.trim();

    // 세 칸 모두 비어 있으면 null 반환 (유효성 검사 생략)
    if (!p1 && !p2 && !p3) {
      showFeedback('phoneFeedback', '', '');
      return { value: null, empty: true };
    }

    const combined = p1 + '-' + p2 + '-' + p3;
    const phoneRegex = /^(01[016789]|02|0[3-9][0-9])-\d{3,4}-\d{4}$/;
    if (!phoneRegex.test(combined)) {
      showFeedback('phoneFeedback', '올바른 연락처 형식이 아닙니다. (예: 010-1234-5678)', 'danger');
      return { value: null, empty: false };
    }

    showFeedback('phoneFeedback', '', '');
    return { value: combined, empty: false };
  }

  // 정보 수정 제출
  document.getElementById('btnSubmitUpdate').addEventListener('click', function () {
    let valid = true;

    // 비밀번호 검증
    if (!validatePwd()) {
      valid = false;
    }

    // 이름 검증
    const name = document.getElementById('inputName').value.trim();
    if (!name) {
      showFeedback('nameFeedback', '이름을 입력해주세요.', 'danger');
      valid = false;
    } else {
      showFeedback('nameFeedback', '', '');
    }

    // 별명 중복 확인 검증
    if (!nicknameChecked) {
      showFeedback('nicknameFeedback', '별명 중복 확인이 필요합니다.', 'danger');
      valid = false;
    }

    // 연락처 검증
    const phoneResult = validatePhone();
    if (!phoneResult.empty && phoneResult.value === null) {
      // 일부만 입력했는데 형식이 틀린 경우
      valid = false;
    }

    if (!valid) return;

    const formData = {
      encryptedMemberNo: encryptedMemberNo,
      name: name,
      nickname: document.getElementById('inputNickname').value.trim(),
    };

    // 연락처: 세 칸 모두 비어 있으면 null, 아니면 합쳐진 값
    if (phoneResult.value !== null) {
      formData.phone = phoneResult.value;
    } else if (phoneResult.empty) {
      formData.phone = null;
    }

    const pwd = document.getElementById('newPwd').value;
    if (pwd) {
      formData.userPwd = pwd;
    }

    $.ajax({
      url: '/api/member/' + encryptedMemberNo + '/update',
      method: 'PUT',
      data: formData,
      success: function () {
        location.reload();
      },
      error: function (xhr) {
        alert('정보 수정에 실패했습니다. (' + (xhr.responseText || xhr.status) + ')');
      }
    });
  });


  // ===================== 상태 변경 모달 =====================
  const btnOpenStatus = document.getElementById('btnOpenStatusModal');
  if (btnOpenStatus) {
    btnOpenStatus.addEventListener('click', () => openModal('statusModal'));
  }

  const btnCloseStatus = document.getElementById('btnCloseStatusModal');
  if (btnCloseStatus) {
    btnCloseStatus.addEventListener('click', () => closeModal('statusModal'));
  }

  const btnCancelStatus = document.getElementById('btnCancelStatus');
  if (btnCancelStatus) {
    btnCancelStatus.addEventListener('click', () => closeModal('statusModal'));
  }

  const btnSubmitStatus = document.getElementById('btnSubmitStatus');
  if (btnSubmitStatus) {
    btnSubmitStatus.addEventListener('click', function () {
      const selected = document.querySelector('input[name="statusRadio"]:checked');
      if (!selected) {
        showFeedback('statusFeedback', '변경할 상태를 선택해주세요.', 'danger');
        return;
      }
      $.ajax({
        url: '/api/member/' + encryptedMemberNo + '/update/status',
        method: 'PUT',
        data: {
          status: selected.value,
          encryptedMemberNo: encryptedMemberNo
        },
        success: function () {
          location.reload();
        },
        error: function () {
          showFeedback('statusFeedback', '상태를 변경할 수 없습니다.', 'danger');
        }
      });
    });
  }


  // ===================== 권한 변경 모달 =====================
  const btnOpenRole = document.getElementById('btnOpenRoleModal');
  if (btnOpenRole) {
    btnOpenRole.addEventListener('click', () => openModal('roleModal'));
  }

  const btnCloseRole = document.getElementById('btnCloseRoleModal');
  if (btnCloseRole) {
    btnCloseRole.addEventListener('click', () => closeModal('roleModal'));
  }

  const btnCancelRole = document.getElementById('btnCancelRole');
  if (btnCancelRole) {
    btnCancelRole.addEventListener('click', () => closeModal('roleModal'));
  }

  const btnSubmitRole = document.getElementById('btnSubmitRole');
  if (btnSubmitRole) {
    btnSubmitRole.addEventListener('click', function () {
      const selected = document.querySelector('input[name="roleRadio"]:checked');
      if (!selected) {
        showFeedback('roleFeedback', '변경할 권한을 선택해주세요.', 'danger');
        return;
      }
      $.ajax({
        url: '/api/member/' + encryptedMemberNo + '/update/role',
        method: 'PUT',
        data: {
          role: selected.value,
          encryptedMemberNo: encryptedMemberNo
        },
        success: function () {
          location.reload();
        },
        error: function () {
          showFeedback('roleFeedback', '권한 변경에 실패했습니다.', 'danger');
        }
      });
    });
  }


  // ===================== 회원 탈퇴 =====================
  const btnDeleteAccount = document.getElementById('btnDeleteAccount');
  if (btnDeleteAccount) {
    btnDeleteAccount.addEventListener('click', function () {
      if (confirm('정말로 회원 탈퇴하시겠습니까?\n이 작업은 되돌릴 수 없습니다.')) {
        document.getElementById('deleteForm').submit();
      }
    });
  }


  // ─── 유틸: 피드백 표시 ───
  function showFeedback(elementId, message, type) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.textContent = message;
    el.className = 'form-feedback';
    if (type === 'danger') el.classList.add('text-danger');
    if (type === 'success') el.classList.add('text-success');
  }

});