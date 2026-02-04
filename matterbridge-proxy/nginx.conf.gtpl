daemon off;
user nginx;
pid /var/run/nginx.pid;

worker_processes 1;

events { worker_connections 512; }

http {
  access_log off;
  error_log /proc/1/fd/1 error;

  # WebSocket / Upgrade support
  map $http_upgrade $connection_upgrade {
    default upgrade;
    ''      close;
  }

  server {
    listen 8099 default_server;
    server_name _;

    location / {
      # Only Home Assistant Ingress Proxy
      allow 172.30.32.2;
      deny  all;

      set     $target "{{ if .https }}https{{ else }}http{{ end }}://{{ .host }}:{{ .port }}";

      proxy_pass $target;
      proxy_http_version 1.1;

      # WebSockets
      proxy_set_header Upgrade $http_upgrade;
      proxy_set_header Connection $connection_upgrade;

      # Forwarded headers
      proxy_set_header Host $http_host;
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_set_header X-Forwarded-Proto $scheme;

      # Stabilität: lange UI-Sessions nicht abwürgen
      proxy_read_timeout  3600s;
      proxy_send_timeout  3600s;

      # Kein Cache
      proxy_no_cache 1;
      proxy_cache_bypass 1;
      add_header Cache-Control "no-store";
    }
  }
}
