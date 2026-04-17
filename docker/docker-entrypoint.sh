#!/bin/bash
set -euo pipefail

export LANG="${LANG:-C.UTF-8}"
export LC_ALL="${LC_ALL:-C.UTF-8}"

SERVER_ROOT="${SERVER_ROOT:-/server}"
# 未设置 SERVER_SEED 时不在镜像内保留种子目录；可选设为只读目录路径以在启动时灌入或同步（高级用法）
SEED="${SERVER_SEED:-}"

cd "$SERVER_ROOT"

# 若设置了 SERVER_SEED 且数据目录为空，从该目录灌入完整服务端
seed_from_image() {
  [ -n "${SEED:-}" ] || return 0
  [ -d "$SEED" ] || return 0
  if [ -f "${SERVER_ROOT}/.seed-imported" ]; then
    return 0
  fi
  if [ -f "${SERVER_ROOT}/craftbukkit.jar" ] || [ -d "${SERVER_ROOT}/plugins" ] || [ -d "${SERVER_ROOT}/world" ] || [ -d "${SERVER_ROOT}/sky" ]; then
    touch "${SERVER_ROOT}/.seed-imported" 2>/dev/null || true
    return 0
  fi
  echo "首次启动：从 ${SEED} 复制服务端数据 -> ${SERVER_ROOT}"
  cp -a "${SEED}/." "${SERVER_ROOT}/"
  touch "${SERVER_ROOT}/.seed-imported" 2>/dev/null || true
}

if [ "${SKIP_SERVER_SEED:-0}" != "1" ]; then
  seed_from_image
fi

sync_known_plugin_yml_from_seed() {
  [ -n "${SEED:-}" ] || return 0
  [ -d "${SEED}/plugins" ] || return 0
  [ "${SKIP_SYNC_KNOWN_PLUGIN_YML:-0}" = "1" ] && return 0
  local rel src dst
  for rel in \
    lib/config.yml \
    Lockette/strings-cn.yml \
    clear/names.yml \
    trade/names.yml \
  ; do
    src="${SEED}/plugins/${rel}"
    dst="${SERVER_ROOT}/plugins/${rel}"
    [ -f "$src" ] || continue
    if [ ! -f "$dst" ] || ! cmp -s "$src" "$dst" 2>/dev/null; then
      echo "sync-plugins-from-seed: ${rel}"
      mkdir -p "$(dirname "$dst")"
      cp -a "$src" "$dst"
    fi
  done
}

sync_known_plugin_yml_from_seed

sync_language_yml_from_seed() {
  [ -n "${SEED:-}" ] || return 0
  [ -d "${SEED}/plugins" ] || return 0
  [ "${SKIP_SYNC_LANGUAGE_YML:-0}" = "1" ] && return 0
  local src dst rel prefix
  prefix="${SEED}/plugins/"
  while IFS= read -r -d '' src; do
    rel="${src#"$prefix"}"
    dst="${SERVER_ROOT}/plugins/${rel}"
    [ -f "$src" ] || continue
    if [ ! -f "$dst" ] || ! cmp -s "$src" "$dst" 2>/dev/null; then
      echo "sync-language-yml-from-seed: ${rel}"
      mkdir -p "$(dirname "$dst")"
      cp -a "$src" "$dst"
    fi
  done < <(find "${SEED}/plugins" \( -name 'language_cn.yml' -o -name 'language.yml' \) -print0 2>/dev/null || true)
}

sync_language_yml_from_seed

MYSQL_HOST="${MYSQL_HOST:-mysql}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_DATABASE="${MYSQL_DATABASE:-newsky}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-root}"

# 等待 TCP 端口（Compose depends_on 不能保证 MySQL 已监听）
wait_for_mysql_tcp() {
  [ "${SKIP_MYSQL_WAIT:-0}" = "1" ] && return 0
  local host="${MYSQL_HOST}"
  local port="${MYSQL_PORT}"
  local max="${MYSQL_WAIT_RETRIES:-90}"
  local n=0
  echo "等待 MySQL ${host}:${port} 可连接（最多约 ${max}s）..."
  while true; do
    if (echo >/dev/tcp/${host}/${port}) 2>/dev/null; then
      echo "MySQL 端口已开放"
      sleep 2
      return 0
    fi
    n=$((n + 1))
    if [ "$n" -ge "$max" ]; then
      echo "警告: 等待 MySQL 超时，仍将启动（Hibernate 可能重试失败）" >&2
      return 0
    fi
    sleep 1
  done
}

wait_for_mysql_tcp

# JDBC URL 写入 hibernate.cfg.xml 时必须转义 & 为 &amp;，否则 XML 解析报错（entity "characterEncoding"...）
# Hibernate 读入后会得到带 & 的合法 JDBC 连接串
JDBC_URL_XML="jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useUnicode=true&amp;characterEncoding=UTF-8"

replace_hibernate() {
  local f="$1"
  [ -f "$f" ] || return 0
  HIBERNATE_JDBC_URL="$JDBC_URL_XML" \
  HIBERNATE_USER="$MYSQL_USER" \
  HIBERNATE_PASS="$MYSQL_PASSWORD" \
  perl -i -0pe '
    BEGIN {
      $j = $ENV{HIBERNATE_JDBC_URL};
      $u = $ENV{HIBERNATE_USER};
      $p = $ENV{HIBERNATE_PASS};
    }
    s{<property name="connection\.url">.*?</property>}{<property name="connection.url">\n\t\t$j\n\t</property>}gs;
    s{<property name="connection\.username">.*?</property>}{<property name="connection.username">\n\t\t$u\n\t</property>}gs;
    s{<property name="connection\.password">.*?</property>}{<property name="connection.password">\n\t\t$p\n\t</property>}gs;
  ' "$f"
}

if [ "${SKIP_HIBERNATE_ENV:-0}" != "1" ]; then
  while IFS= read -r -d '' hf; do
    replace_hibernate "$hf"
  done < <(find "${SERVER_ROOT}/plugins" -maxdepth 2 -name hibernate.cfg.xml -print0 2>/dev/null || true)
fi

if [ -f "${SERVER_ROOT}/server.properties" ]; then
  if [ "${FIX_CONTAINER_BIND:-1}" = "1" ]; then
    sed -i 's/^server-ip=.*/server-ip=/' "${SERVER_ROOT}/server.properties" || true
  fi
  if [ -n "${MC_SERVER_PORT:-}" ]; then
    sed -i "s/^server-port=.*/server-port=${MC_SERVER_PORT}/" "${SERVER_ROOT}/server.properties" || true
  fi
fi

JAR="craftbukkit.jar"
if [ ! -f "${SERVER_ROOT}/${JAR}" ]; then
  echo "错误: 未找到 ${SERVER_ROOT}/${JAR}" >&2
  echo "请将包含 craftbukkit.jar 的宿主机目录挂载到 ${SERVER_ROOT}（例如 compose 中 .:/server），或设置 SERVER_SEED 指向含该文件的种子目录。" >&2
  exit 1
fi

JAVA_BIN="${JAVA_BIN:-java}"
if [ -z "${JAVA_OPTS:-}" ]; then
  JAVA_OPTS='-Xms512M -Xmx2048M -Dfile.encoding=UTF-8'
fi

# JLine 在非 TTY 下会把大量「>」刷在同一行前缀（再接 17:05:14 [INFO] ...），不经过 Spigot log-filters。
# 需剥 ANSI、处理 \r> 与 \r 后，再去掉行首连续「>」（^>+），否则仅匹配「整行只有 >」无效。
# 使用 pipefail 使管道退出码反映 java 进程。
set -o pipefail
"${JAVA_BIN}" ${JAVA_OPTS} -jar "${SERVER_ROOT}/${JAR}" "$@" </dev/null 2>&1 \
  | perl -e "$(cat <<'PERL'
$| = 1;
while (<STDIN>) {
  my $line = $_;
  $line =~ s/\x1b\[[0-9;?]*[0-9A-Za-z]//g;
  $line =~ s/\x1b\[[0-9;]*m//g;
  $line =~ s/\r\s*>//g;
  $line =~ s/\r//g;
  $line =~ s/^>+//;
  my $t = $line;
  chomp $t;
  next if $t =~ /^\s*$/;
  next if $t =~ /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2} \[INFO\] >\s*$/;
  print $line;
}
PERL
)"
exit "${PIPESTATUS[0]}"
