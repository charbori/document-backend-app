  # syntax=docker/dockerfile:1
# MySQL 8.0 공식 이미지를 기반으로 합니다.
FROM mysql:8.0

COPY my.cnf /etc/mysql/conf.d/

COPY init.sql /docker-entrypoint-initdb.d/
