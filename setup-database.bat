@echo off
echo Setting up MySQL database for automation testing...

REM Create the database and tables
mysql -u root -p"roshan@2005" -e "DROP DATABASE IF EXISTS automation_tests; CREATE DATABASE automation_tests;"
mysql -u root -p"roshan@2005" automation_tests < src\main\resources\sql\db-init.sql

echo Database setup complete!
pause
