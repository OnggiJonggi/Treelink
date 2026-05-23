document.addEventListener('DOMContentLoaded', function () {

    /* ── 정규식 ───────────────────────────── */
    const REGEX = {
        userId: /^[A-Za-z0-9]{4,12}$/,
        userPwd: /^[A-Za-z0-9@$!%*#?&]{4,20}$/,
        name: /^[ㄱ-ㅎ가-힣a-zA-Z0-9]{1,10}$/,
        nickname: /^[ㄱ-ㅎ가-힣a-zA-Z0-9]{1,10}$/,
    };

    /* ── 중복 확인 통과 플래그 ───────────────── */
    const checked = { userId: false, nickname: false };

    /* ── DOM 참조 ────────────────────────── */
    const form = document.getElementById('joinForm');
    const inputUserId = document.getElementById('userId');
    const inputPwd = document.getElementById('userPwd');
    const inputName = document.getElementById('name');
    const inputNickname = document.getElementById('nickname');
    const btnCheckId = document.getElementById('btn-check-id');
    const btnCheckNick = document.getElementById('btn-check-nickname');
    const btnEyePwd = document.getElementById('btn-eye-pwd');

    /* ── 피드백 헬퍼 ─────────────────────── */
    function setFeedback(id, msg, isOk) {
        const el = document.getElementById('feedback-' + id);
        const input = document.getElementById(id);
        el.textContent = msg;
        el.className = 'form-feedback ' + (isOk ? 'ok' : 'err');
        input.classList.toggle('is-valid', isOk && msg !== '');
        input.classList.toggle('is-invalid', !isOk && msg !== '');
    }

    function clearFeedback(id) {
        const el = document.getElementById('feedback-' + id);
        const input = document.getElementById(id);
        el.textContent = '';
        el.className = 'form-feedback';
        input.classList.remove('is-valid', 'is-invalid');
    }

    /* ── 실시간 유효성 검사 ───────────────── */
    function validateField(input, regexKey) {
        const val = input.value.trim();
        if (!val) { clearFeedback(input.id); return false; }
        const ok = REGEX[regexKey].test(val);
        if (!ok) {
            const msgs = {
                userId: '아이디: 영문·숫자 4~12자로 입력해 주세요.',
                userPwd: '비밀번호: 영문·숫자·특수문자(@$!%*#?&) 4~20자로 입력해 주세요.',
                name: '이름: 한글·영문·숫자 1~10자로 입력해 주세요.',
                nickname: '닉네임: 한글·영문·숫자 1~10자로 입력해 주세요.',
            };
            setFeedback(input.id, msgs[regexKey], false);
        } else {
            clearFeedback(input.id);
        }
        return ok;
    }

    inputUserId.addEventListener('input', function () {
        checked.userId = false;  // 값 바뀌면 중복 확인 초기화
        validateField(inputUserId, 'userId');
    });

    inputPwd.addEventListener('input', function () {
        validateField(inputPwd, 'userPwd');
    });

    inputName.addEventListener('input', function () {
        validateField(inputName, 'name');
    });

    inputNickname.addEventListener('input', function () {
        checked.nickname = false;  // 값 바뀌면 중복 확인 초기화
        validateField(inputNickname, 'nickname');
    });

    /* ── 비밀번호 눈 토글 ─────────────────── */
    btnEyePwd.addEventListener('click', function () {
        const isHidden = inputPwd.type === 'password';
        inputPwd.type = isHidden ? 'text' : 'password';
        btnEyePwd.innerHTML = isHidden
            ? '<i data-lucide="eye-off"></i>'
            : '<i data-lucide="eye"></i>';
        lucide.createIcons();
    });

    /* ── 아이디 중복 확인 ─────────────────── */
    btnCheckId.addEventListener('click', function () {
        const val = inputUserId.value.trim();
        if (!REGEX.userId.test(val)) {
            setFeedback('userId', '아이디 형식을 먼저 올바르게 입력해 주세요.', false);
            return;
        }

        btnCheckId.disabled = true;
        btnCheckId.textContent = '확인 중…';

        $.ajax({
            url: '/api/member/check-id',
            type: 'GET',
            data: { userId: val },
            success: function () {
                checked.userId = true;
                setFeedback('userId', '사용 가능한 아이디입니다.', true);
            },
            error: function (xhr) {
                checked.userId = false;
                if (xhr.status === 500) {
                    setFeedback('userId', '이미 사용 중인 아이디입니다.', false);
                } else {
                    setFeedback('userId', '확인 중 오류가 발생했습니다. 다시 시도해 주세요.', false);
                }
            },
            complete: function () {
                btnCheckId.disabled = false;
                btnCheckId.textContent = '중복 확인';
            }
        });
    });

    /* ── 닉네임 중복 확인 ─────────────────── */
    btnCheckNick.addEventListener('click', function () {
        const val = inputNickname.value.trim();
        if (!REGEX.nickname.test(val)) {
            setFeedback('nickname', '닉네임 형식을 먼저 올바르게 입력해 주세요.', false);
            return;
        }

        btnCheckNick.disabled = true;
        btnCheckNick.textContent = '확인 중…';

        $.ajax({
            url: '/api/member/check-nickname',
            type: 'GET',
            data: { 'check-nickname': val },
            success: function () {
                checked.nickname = true;
                setFeedback('nickname', '사용 가능한 닉네임입니다.', true);
            },
            error: function (xhr) {
                checked.nickname = false;
                if (xhr.status === 500) {
                    setFeedback('nickname', '이미 사용 중인 닉네임입니다.', false);
                } else {
                    setFeedback('nickname', '확인 중 오류가 발생했습니다. 다시 시도해 주세요.', false);
                }
            },
            complete: function () {
                btnCheckNick.disabled = false;
                btnCheckNick.textContent = '중복 확인';
            }
        });
    });

    /* ── 최종 폼 제출 유효성 검사 ─────────── */
    form.addEventListener('submit', function (e) {
        let valid = true;

        // 각 필드 정규식 검사
        if (!REGEX.userId.test(inputUserId.value.trim())) {
            setFeedback('userId', '아이디 형식을 확인해 주세요.', false);
            valid = false;
        }
        if (!REGEX.userPwd.test(inputPwd.value.trim())) {
            setFeedback('userPwd', '비밀번호 형식을 확인해 주세요.', false);
            valid = false;
        }
        if (!REGEX.name.test(inputName.value.trim())) {
            setFeedback('name', '이름 형식을 확인해 주세요.', false);
            valid = false;
        }
        if (!REGEX.nickname.test(inputNickname.value.trim())) {
            setFeedback('nickname', '닉네임 형식을 확인해 주세요.', false);
            valid = false;
        }

        // 아이디 중복 확인 여부
        if (!checked.userId) {
            setFeedback('userId', '아이디 중복 확인을 완료해 주세요.', false);
            valid = false;
        }

        // 닉네임 중복 확인 여부
        if (!checked.nickname) {
            setFeedback('nickname', '닉네임 중복 확인을 완료해 주세요.', false);
            valid = false;
        }

        if (!valid) {
            e.preventDefault();
            // 첫 번째 에러 필드로 포커스 이동
            const firstErr = form.querySelector('.is-invalid');
            if (firstErr) firstErr.focus();
        }
    });
});