(function () {
  console.error('[에러 페이지 진입]');
  console.error('상태 코드:', window.serverStatus);
  console.error('에러명:', window.serverError);
  console.error('메시지:', window.serverMessage);
  console.error('요청 경로:', window.serverPath);

  document.getElementById('btnBack').addEventListener('click', function () {
    if (window.history.length > 1) {
      history.back();
    } else {
      location.href = '/';
    }
  });
})();