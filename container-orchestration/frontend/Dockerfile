# ==============================================================================
# React Frontend Multi-Stage Dockerfile (Production Optimized & Secure)
# ==============================================================================

# --- Stage 1: Build React Application ---
FROM node:18-alpine AS builder

WORKDIR /app

# Copy dependency definitions
COPY package.json package-lock.json* ./

# Install packages securely (clean install, production dependencies only if applicable)
RUN npm ci || npm install

# Copy application source
COPY . .

# Build the React production distribution
RUN npm run build

# --- Stage 2: Serve React Static Files with Non-Root Nginx ---
FROM nginx:1.25.4-alpine

# Set directory permissions for the standard non-root 'nginx' user
RUN touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid /var/cache/nginx /var/log/nginx /etc/nginx/conf.d

# Set active container user to 'nginx' (UID 101) instead of root
USER nginx

# Copy static build files from Stage 1 into the default serve path
COPY --link --from=builder /app/build /usr/share/nginx/html

# Copy the custom non-root Nginx server configuration (listening on port 8080)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Document the port the container listens on (non-root uses 8080)
EXPOSE 8080

# Run Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
