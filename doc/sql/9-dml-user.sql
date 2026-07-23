-- 시스템 계정에서 전체 실행

-- 조회용 계정 생성
CREATE USER TL_DML IDENTIFIED BY TL_DML;
GRANT CREATE SESSION TO TL_DML;

-- TREELINK 테이블 DML 권한 부여
BEGIN
  FOR table_rec IN (SELECT table_name FROM all_tables WHERE owner = 'TREELINK') LOOP
    EXECUTE IMMEDIATE 'GRANT SELECT, INSERT, UPDATE, DELETE ON TREELINK.' 
      || table_rec.table_name || ' TO TL_DML';
  END LOOP;
END;
/

-- TL_DML 시노님 생성
BEGIN
  FOR tbl IN (SELECT table_name FROM all_tables WHERE owner = 'TREELINK') LOOP
    BEGIN
      EXECUTE IMMEDIATE 'CREATE SYNONYM TL_DML.' || tbl.table_name 
        || ' FOR TREELINK.' || tbl.table_name;
    EXCEPTION
      WHEN OTHERS THEN
        NULL;
    END;
  END LOOP;
END;
/