--------------------------- 로컬 ---------------------------
-- 데이터스페이스 생성
CREATE TABLESPACE TREELINK
DATAFILE 'treelink_data01.dbf'
SIZE 100M -- 100mb쓸거에요
AUTOEXTEND ON NEXT 10M -- 부족하면 조금씩 땅겨올거에요
MAXSIZE UNLIMITED; -- 무한정 땅겨올거에요

--------------------------- RDS ---------------------------
CREATE TABLESPACE TREELINK;

--------------------------- 계정 생성 ---------------------------
-- DDL 계정 생성 / 권한 부여
CREATE USER TREELINK IDENTIFIED BY TREELINK
DEFAULT TABLESPACE TREELINK
QUOTA UNLIMITED ON TREELINK;
GRANT CONNECT, RESOURCE TO TREELINK;

--------------------------- 계정 삭제 ---------------------------
-- 계정 날리기
DROP USER TREELINK CASCADE;
DROP USER APP_USER CASCADE;
-- 데이터스페이스 날리기
DROP TABLESPACE TREELINK INCLUDING CONTENTS AND DATAFILES;


-- 계정이 안 날려진다구요? 어떤 세션이 멋대로 접속중인지 확인하세요!
SELECT 'ALTER SYSTEM KILL SESSION ''' || SID || ',' || SERIAL# || ''' IMMEDIATE;'
FROM V$SESSION
WHERE USERNAME = 'APP_USER';