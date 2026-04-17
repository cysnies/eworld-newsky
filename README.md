# E世界-新空岛服务端

基于 **Minecraft Java 1.6.2** 与 **CraftBukkit**，使用 **Docker / Docker Compose** 在 Linux 上部署服务器以及 **MySQL 8.0**。

镜像**仅**包含 Java 运行环境与入口脚本；`craftbukkit.jar`、地图、插件等**始终在宿主机项目目录**，通过 Compose **绑定挂载**到容器内 `/server`，不在镜像中保存副本。数据库表结构**不在容器内自动初始化**，需要时要在宿主机上**手动**执行数据库初始化脚本。

---

## 功能概览

| 项目 | 说明 |
|------|------|
| 游戏核心 | `craftbukkit.jar`（构建时校验必须存在） |
| 编排 | `docker-compose.yml`：游戏服务器 `newsky` + 官方 `mysql:8.0` |
| 持久化 | 游戏数据在**宿主机项目目录**（挂载为容器 `/server`）；仅 MySQL 使用命名卷 `mysql-data` |
| 数据库 | 插件通过 Hibernate 连接 MySQL；建库建表使用 `sql/init_newsky_schema.sql` + `init-db.sh` **手动执行** |

---

## 目录结构

```
.
├── Dockerfile                 # 游戏服务器镜像（仅 JRE + 入口脚本，不含地图与插件）
├── docker-compose.yml         # 服务定义（`.:/server` 绑定挂载）
├── docker/
│   └── docker-entrypoint.sh   # 容器入口：灌卷、同步部分插件配置、写 JDBC、启动 MC
├── init-db.sh                 # 宿主机手动初始化数据库 schema
├── sql/
│   └── init_newsky_schema.sql # 建库建表 SQL（与插件 ORM 对应）
├── env.example                # 环境变量模板（复制为 .env）
├── craftbukkit.jar            # 服务端核心（放在项目根目录，由挂载供容器使用）
├── plugins/                   # Bukkit 插件
├── world/、sky/ 等            # 世界与存档相关目录（宿主机持久化，不经镜像复制）
└── server.properties 等       # 服务端配置
```

---

## 环境要求

- **操作系统**：Linux
- **Docker**、**Docker Compose**（Compose V2：`docker compose`）
- 手动执行 `init-db.sh` 时：宿主机需安装 **MySQL 客户端**（如 `mysql` 命令，Debian/Ubuntu 可 `apt install mysql-client`）

---

## 快速开始

### 1. 配置环境变量

```bash
cp env.example .env
# 然后编辑 .env 文件
```

### 2. 构建并启动

请确保项目根目录已包含 `craftbukkit.jar` 及所需地图、插件等（与本地直接开服相同）。

```bash
docker compose build
docker compose up -d
```

Compose 将**当前目录**挂载为容器内 `/server`，请在**项目根目录**执行上述命令，以便挂载路径正确。

- 游戏服务器对外端口默认 **`PUBLISH_PORT`**（默认 `25565`）映射到容器内 `25565`。
- MySQL 对外映射为 **`MYSQL_PUBLISH_PORT`**（默认 `33060`）→ 容器内 `3306`。

### 3. 初始化数据库

Compose **不会**自动执行 SQL。如果是首次启动或需要重建 schema 时，需要在 **MySQL 已可连接** 后，在**项目根目录**执行：

```bash
chmod +x init-db.sh
./init-db.sh
```

脚本会：

1. 读取 **`.env`**
2. 使用 **`MYSQL_ROOT_PASSWORD`** 以 **root** 连接 MySQL
3. 将 **`sql/init_newsky_schema.sql`** 导入数据库

**从宿主机连接 Compose 内的 MySQL 时**，请保证 `.env` 中的地址与端口可从本机访问，例如：

- `MYSQL_HOST=127.0.0.1`
- `MYSQL_PORT` 与 **`MYSQL_PUBLISH_PORT`** 一致（如 `33060`）

若仅在容器网络内使用服务名 `mysql:3306`，则保持 `env.example` 中的默认值即可，但此时应在**能解析 `mysql` 主机名的环境**中执行客户端（例如在另一容器内），一般推荐在宿主机初始化时使用 **`127.0.0.1` + 映射端口**。

---

## 环境变量说明（`.env`）

| 变量 | 说明 |
|------|------|
| `MYSQL_HOST` | 游戏容器内访问 MySQL 的主机名；Compose 默认 **`mysql`**。宿主机跑 `init-db.sh` 时常改为 **`127.0.0.1`**。 |
| `MYSQL_PORT` | MySQL 端口；容器内为 **`3306`**；宿主机执行脚本时常为映射端口（如 **`33060`**）。 |
| `MYSQL_DATABASE` | 数据库名，默认 **`newsky`**。 |
| `MYSQL_USER` / `MYSQL_PASSWORD` | 应用使用的账号（与 Compose 中 MySQL 服务创建的用户一致）。 |
| `MYSQL_ROOT_PASSWORD` | root 密码；**`init-db.sh` 使用 root 执行 SQL**，必填。 |
| `PUBLISH_PORT` | 宿主机映射 Minecraft 端口，默认 **25565**。 |
| `MC_SERVER_PORT` | 写入 `server.properties` 的 `server-port`，需与容器监听一致，默认 **25565**。 |
| `MYSQL_PUBLISH_PORT` | 宿主机映射 MySQL **3306** 的端口，默认 **33060**。 |

游戏进程内的 JVM 参数可通过镜像环境变量 **`JAVA_OPTS`** 覆盖（见 `Dockerfile` 默认值）。

---

## Docker 说明

### 游戏服务器镜像（`Dockerfile`）

- 基础镜像：**eclipse-temurin:8-jre-jammy**
- **不向镜像复制**服务端数据；仅安装 **perl**（入口脚本改写 Hibernate 配置用）并安装 **`docker-entrypoint.sh`**。
- 运行时 **`SERVER_ROOT=/server`** 由宿主机目录挂载提供；构建上下文通过 **`.dockerignore`** 仅包含入口脚本，避免构建时向 Docker 传输大量游戏文件。
- 启动命令：`java` 运行 **`craftbukkit.jar`**，参数 **`nogui`**。

### 入口脚本（`docker/docker-entrypoint.sh`）主要行为

1. **可选种子灌入**：若设置了环境变量 **`SERVER_SEED`** 指向某目录，且挂载的 `/server` 中尚无核心文件/世界，则从该目录复制；默认不设置（数据完全来自宿主机挂载）。可用 **`SKIP_SERVER_SEED=1`** 跳过。
2. **插件文件同步**：当 **`SERVER_SEED`** 已设置且含 `plugins` 时，向工作目录同步部分已知 `yml`（可分别用 **`SKIP_SYNC_KNOWN_PLUGIN_YML`**、**`SKIP_SYNC_LANGUAGE_YML`** 关闭）。
3. **等待 MySQL**：对 `MYSQL_HOST:MYSQL_PORT` 做 TCP 探测；可用 **`SKIP_MYSQL_WAIT=1`** 跳过。
4. **Hibernate**：根据环境变量改写插件目录下 **`hibernate.cfg.xml`** 中的 JDBC；可用 **`SKIP_HIBERNATE_ENV=1`** 跳过。
5. **`server.properties`**：可选清空 `server-ip`（**`FIX_CONTAINER_BIND`**，默认开启）、按 **`MC_SERVER_PORT`** 写端口。

### MySQL 服务

- 使用**官方** **`mysql:8.0`** 镜像。
- **`command`**：`--default-authentication-plugin=mysql_native_password`（兼容旧版 JDBC 驱动）。
- 数据持久化在卷 **`mysql-data`**。

### 使用外部 / 宿主机已有 MySQL

1. 从 `docker-compose.yml` 中**删除或注释** `mysql` 服务。
2. 去掉 `newsky` 的 **`depends_on: mysql`**（或改为适合你环境的依赖）。
3. 将 `MYSQL_HOST`（及必要时端口）指向可达的 MySQL 地址；若从容器访问宿主机上的 MySQL，可使用 **`extra_hosts`** + **`host.docker.internal`**（见 compose 内注释示例）。

数据库 schema 仍通过 **`./init-db.sh`** 在 **能连上该 MySQL** 的环境执行 **`sql/init_newsky_schema.sql`**。

---

## 数据持久化与备份

| 位置 | 用途 |
|------|------|
| **宿主机项目目录**（挂载为 `/server`） | 游戏核心、世界、插件及运行时数据 |
| 命名卷 **`mysql-data`** | MySQL 数据文件 |

升级镜像或重建容器前，请按需备份项目目录与 `mysql-data` 卷。

---

## 常见问题

**游戏连不上数据库**

- 确认 MySQL 已健康启动，且 **`init-db.sh` 已成功执行**（至少执行过一次）。
- 确认 `MYSQL_*` 与插件内 Hibernate 配置一致（入口脚本会在启动时写入 `hibernate.cfg.xml`）。

**宿主机执行 `init-db.sh` 连接被拒绝**

- 检查 `MYSQL_HOST` / `MYSQL_PORT` 是否指向**宿主机可访问**的地址（通常为 `127.0.0.1` 与 `MYSQL_PUBLISH_PORT`）。
- 确认防火墙放行对应端口。

**首次启动后世界/插件为空**

- 确认在**项目根目录**执行 `docker compose up`，且该目录下已有 `craftbukkit.jar`、地图与插件目录。若需从另一目录首次灌入，可设置 **`SERVER_SEED`** 指向该只读目录（高级用法）。
