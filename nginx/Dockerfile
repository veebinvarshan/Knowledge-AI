FROM nginx:1.27-alpine

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Expose HTTP port 80
EXPOSE 80

# Healthcheck to verify Nginx availability
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD wget -q --spider http://127.0.0.1/nginx-health || exit 1

CMD ["nginx", "-g", "daemon off;"]
