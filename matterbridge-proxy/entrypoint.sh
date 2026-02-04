#!/bin/sh
set -e

tempio -conf /data/options.json -template /nginx.conf.gtpl -out /tmp/nginx.conf

nginx -t -c /tmp/nginx.conf >/dev/null 2>&1 || {
  echo "nginx: config test FAILED"
  nginx -t -c /tmp/nginx.conf
  exit 1
}

exec nginx -c /tmp/nginx.conf
