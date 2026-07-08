-- 만료된 파일 FILE_INFO에서 삭제 및 파일 기록 추가
-- TODO : EXPIRE_ON 수정 필요

create or replace procedure PROC_EXPIRE_FILE as
begin
  insert into FILE_HISTORY(
    FILE_NO
    ,ORIGINAL_NAME
    ,CHANGED_NAME
    ,SAVE_PATH
    ,ACTION
    ,ACTION_AT
    ,ACTION_BY
  )
  select
    FILE_NO
    ,ORIGINAL_NAME
    ,CHANGED_NAME
    ,SAVE_PATH
    ,'EXPIRED'
    ,SYSDATE
    ,NULL
  from FILE_INFO
  where STATUS = 'ACTIVE'
  and EXPIRE_ON is not null
  and trunc(EXPIRE_ON) <= trunc(SYSDATE);

  update FILE_INFO
  set STATUS = 'EXPIRED'
  where STATUS = 'ACTIVE'
  and EXPIRE_ON is NOT NULL
  and trunc(EXPIRE_ON) <= trunc(SYSDATE);

  commit;
end PROC_EXPIRE_FILE;
/