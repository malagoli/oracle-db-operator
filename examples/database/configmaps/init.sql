-- ORDS USER
CREATE USER C##DBAPI_CDB_ADMIN IDENTIFIED BY "##ORDS_PASSWORD";
GRANT SYSDBA TO C##DBAPI_CDB_ADMIN CONTAINER = ALL;
GRANT connect  TO C##DBAPI_CDB_ADMIN CONTAINER = ALL;

-- CONNECTION TO CONNECTION MANAGER
alter system  set remote_listener='oracle-db-connection-manager-service:1521' scope=both sid='*';
alter system register;

