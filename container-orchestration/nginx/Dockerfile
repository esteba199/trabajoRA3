# ==============================================================================
# Nginx Reverse Proxy Dockerfile
# Production-ready, lightweight configuration
# ==============================================================================
FROM nginx:1.25.4-alpine

# Remove default configuration file
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Healthcheck for proxy itself
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/ || exit 1

# Expose HTTP port
EXPOSE 80

# Run nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
