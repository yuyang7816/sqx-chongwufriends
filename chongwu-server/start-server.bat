@echo off
cd /d F:\work\AI_chongwu\ai-sqx-server\sqx-application
"D:\jdk1.8.6\bin\java.exe" -XX:TieredStopAtLevel=1 -noverify -Dfile.encoding=UTF-8 -jar target\sqx-server.jar
