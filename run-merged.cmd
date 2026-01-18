@echo off
setlocal enabledelayedexpansion
REM Load environment variables from .env file and run merged Docker container

REM Read .env file and set variables (handle values with spaces)
for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
    set "%%a=%%b"
)

REM Run the merged Docker container with environment variables
docker run -d ^
  --name nexus-feed-merged ^
  -p 5173:80 ^
  -p 10008:10008 ^
  -e "DB_URL=!DB_URL!" ^
  -e "DB_USERNAME=!DB_USERNAME!" ^
  -e "DB_PASSWORD=!DB_PASSWORD!" ^
  -e "JWT_SECRET=!JWT_SECRET!" ^
  -e "MAIL_USERNAME=!MAIL_USERNAME!" ^
  -e "MAIL_PASSWORD=!MAIL_PASSWORD!" ^
  -e "GOOGLE_CLIENT_ID=!GOOGLE_CLIENT_ID!" ^
  nexus-feed-merged:latest

echo.
echo Container started!
echo Frontend: http://localhost:5173
echo Backend API: http://localhost:10008
echo.
echo View logs: docker logs -f nexus-feed-merged
echo Stop container: docker stop nexus-feed-merged
echo Remove container: docker rm nexus-feed-merged
