# Merged Dockerfile with supervisord
FROM eclipse-temurin:21-jdk-alpine AS backend-builder

WORKDIR /app/backend

# Copy backend files
COPY backend/pom.xml .
COPY backend/.mvn .mvn
COPY backend/mvnw .

# Download dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:resolve -B

# Copy source and build
COPY backend/src ./src
RUN ./mvnw clean package -DskipTests

# --- Frontend Builder ---
FROM node:20-alpine AS frontend-builder

WORKDIR /app/frontend

# Install pnpm
RUN npm install -g pnpm

# Copy frontend dependency files
COPY frontend/package.json frontend/pnpm-lock.yaml ./

# Install dependencies
RUN pnpm install --frozen-lockfile

# Copy frontend source files (excluding node_modules via .dockerignore)
COPY frontend/src ./src
COPY frontend/public ./public
COPY frontend/index.html ./
COPY frontend/vite.config.ts ./
COPY frontend/tsconfig.json ./
COPY frontend/tsconfig.app.json ./
COPY frontend/tsconfig.node.json ./
COPY frontend/components.json ./
COPY frontend/eslint.config.js ./

# Build arguments
ARG VITE_API_URL
ARG VITE_CLOUDINARY_CLOUD_NAME
ARG VITE_CLOUDINARY_UPLOAD_PRESET
ARG VITE_GOOGLE_CLIENT_ID

ENV VITE_API_URL=$VITE_API_URL
ENV VITE_CLOUDINARY_CLOUD_NAME=$VITE_CLOUDINARY_CLOUD_NAME
ENV VITE_CLOUDINARY_UPLOAD_PRESET=$VITE_CLOUDINARY_UPLOAD_PRESET
ENV VITE_GOOGLE_CLIENT_ID=$VITE_GOOGLE_CLIENT_ID

RUN pnpm build

# --- Final Stage ---
FROM eclipse-temurin:21-jre-alpine

# Install nginx and supervisor
RUN apk add --no-cache nginx supervisor

WORKDIR /app

# Copy backend JAR
COPY --from=backend-builder /app/backend/target/backend-0.0.1-SNAPSHOT.jar /app/backend.jar

# Copy frontend build to nginx
COPY --from=frontend-builder /app/frontend/dist /usr/share/nginx/html
COPY frontend/nginx.conf /etc/nginx/http.d/default.conf

# Copy supervisord config
COPY supervisord.conf /etc/supervisord.conf

# Create log directories
RUN mkdir -p /var/log/supervisor /var/log/nginx /run/nginx

# Expose ports
EXPOSE 80 10008

# Start supervisord
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
