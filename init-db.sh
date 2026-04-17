#!/bin/bash
# 根据项目根目录 .env 中的数据库连接信息，手动执行 sql/init_newsky_schema.sql 初始化 schema。
# 需本机已安装 mysql 客户端；从宿主机连接 Compose 中的 MySQL 时，请将 .env 中 MYSQL_HOST/MYSQL_PORT
# 设为可从本机访问的地址与端口（例如 127.0.0.1 与 MYSQL_PUBLISH_PORT 对应的映射端口）。
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

if [ ! -f .env ]; then
  echo "错误: 未找到 ${ROOT}/.env，请从 env.example 复制并填写。" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

SQL_FILE="${ROOT}/sql/init_newsky_schema.sql"
if [ ! -f "$SQL_FILE" ]; then
  echo "错误: 未找到 ${SQL_FILE}" >&2
  exit 1
fi

if ! command -v mysql >/dev/null 2>&1; then
  echo "错误: 未找到 mysql 客户端，请先安装（例如 apt install mysql-client）。" >&2
  exit 1
fi

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:?请在 .env 中设置 MYSQL_ROOT_PASSWORD}"

echo "使用 ${MYSQL_HOST}:${MYSQL_PORT} 以 root 执行 ${SQL_FILE} ..."
mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -uroot -p"$MYSQL_ROOT_PASSWORD" <"$SQL_FILE"
echo "完成。"
