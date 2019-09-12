#!/bin/sh
cp /opt/oracle/scripts/setup/tnsnames.ora /opt/oracle/product/19c/dbhome_1/network/admin/tnsnames.ora
cp /opt/oracle/scripts/setup/listener.ora /opt/oracle/product/19c/dbhome_1/network/admin/listener.ora
cp /opt/oracle/scripts/setup/init.ora /opt/oracle/product/19c/dbhome_1/srvm/admin/init.ora

cp /opt/oracle/scripts/setup/init.sql /opt/oracle/scripts/startup/init.sql