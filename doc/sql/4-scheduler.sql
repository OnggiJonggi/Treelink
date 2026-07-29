-- TODO : 스케쥴러 변경된 테이블에 맞게 수정
BEGIN
  DBMS_SCHEDULER.CREATE_JOB(
    job_name        => 'JOB_EXPIRE_FILES',
    job_type        => 'STORED_PROCEDURE',
    job_action      => 'PROC_EXPIRE_FILES',
    start_date      => TRUNC(SYSDATE + 1),
    repeat_interval => 'FREQ=DAILY; BYHOUR=0; BYMINUTE=0; BYSECOND=0',
    enabled         => TRUE,
    comments        => '만료일 도달 파일 자동 비활성화'
  );
END;
/