@echo off
setlocal enabledelayedexpansion
REM Load environment variables from .env file and build merged Docker image

REM Read .env file and set variables (handle values with spaces)
for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
    set "%%a=%%b"
)

REM Build the merged Docker image with build args
docker build ^
  --build-arg "VITE_API_URL=!VITE_API_URL!" ^
  --build-arg "VITE_CLOUDINARY_CLOUD_NAME=!VITE_CLOUDINARY_CLOUD_NAME!" ^
  --build-arg "VITE_CLOUDINARY_UPLOAD_PRESET=!VITE_CLOUDINARY_UPLOAD_PRESET!" ^
  --build-arg "VITE_GOOGLE_CLIENT_ID=!VITE_GOOGLE_CLIENT_ID!" ^
  -t nexus-feed-merged:latest ^
  -f Dockerfile .

echo.
echo Build complete! Run the container with: run-merged.cmd
