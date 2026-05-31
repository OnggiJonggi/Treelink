create or replace procedure PROC_EXPIRE_FILE as
begin
  insert into FILE_HISTORY(
    FILE_NO
    ,ORIGINAL_NAME
    ,SAVE_PATH
    ,ACTION
    ,ACTION_AT
    ,ACTION_BY
  )
  select
    FILE_NO
    ,ORIGINAL_NAME
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