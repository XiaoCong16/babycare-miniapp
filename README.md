# BabyCare MiniApp

这是一个用于记录婴儿日常护理的微信小程序 + Java 后端项目，适合记录：

- 喂奶
- 尿便/拉粑
- 睡眠
- 体温
- 备注
- 微信提醒

## 技术栈

- Java 17
- Spring Boot 3.3.x
- MyBatis-Plus
- MySQL 8+
- Flyway
- 微信小程序

## 项目结构

```text
backend/
  pom.xml
  src/main/assembly/assembly.xml
  src/main/assembly/bin/
  src/main/resources/bootstrap.yml
  src/main/resources/log4j2-spring.xml
  src/main/java/com/example/babycare/...
  src/main/resources/application.yml
  src/main/resources/db/migration/V1__init.sql
miniprogram/
  app.json
  pages/index/index.js
  pages/index/index.wxml
  pages/index/index.wxss
```

## 数据库

本项目使用 MySQL + Flyway 管理结构。
Flyway 执行记录保存在 `babycare_server_VERSION` 表。旧的 `flyway_schema_history` 不会自动改动；已有业务表的数据库首次使用新记录表时会按现有 `baseline-on-migrate` 配置建立基线。

### 本地启动 MySQL

```bash
docker run --name babycare-mysql \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=babycare \
  -p 3306:3306 \
  -d mysql:8.0
```

## 后端启动

先在 Nacos 的 `xc` 命名空间（使用实际命名空间 ID）、`DEFAULT_GROUP` 下创建 Data ID `common-wxapp`，配置内容采用 YAML 格式：

```yaml
WXAPP_DB_CLASS_NAME: com.mysql.cj.jdbc.Driver
WXAPP_DB_URL: jdbc:mysql://localhost:3306/babycare?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
WXAPP_DB_USERNAME: root
WXAPP_DB_PASSWORD: "你的数据库密码"
```

Nacos 连接参数在 `bootstrap.yml` 中配置，可通过 `NACOS_SERVER_ADDR`、`NACOS_NAMESPACE`、`NACOS_USERNAME` 和 `NACOS_PASSWORD` 环境变量覆盖。数据库连接在启动时从 Nacos 读取；配置缺失或 Nacos 不可用时，应用会启动失败。

```bash
cd backend
mvn clean package
tar -xzf target/babycare-server.tar.gz
./babycare-server/bin/start.sh
./babycare-server/bin/status.sh
./babycare-server/bin/stop.sh
```

`start.sh` 和 `restart.sh` 启动时会打印完整、可复制的 Java 命令。传入 `-l y` 可从启动阶段开始持续输出 `startup.out`，效果与 `tail -f` 相同；按 `Ctrl+C` 只停止跟随日志，后台 Java 进程继续运行：

```bash
./babycare-server/bin/restart.sh -l y
```

分发包包含 `bin/`、`boot/`、`lib/` 和 `config/`。`bin/boot.config` 默认设置 `-Xms500m -Xmx500m`；也可用 `restart.sh` 重启。当前应用日志写到分发包上级目录的 `xcAppLogs/babycare-server-logs/info.log`；跨日后的下一条日志或单文件达到 200 MB 时，旧日志会压缩成 `info-yyyy-MM-dd-序号.log.gz`，新日志继续写入 `info.log`。同一天最多保留 10 个压缩归档。手工运行时可编辑分发包内的 `config/bootstrap.yml`；自动部署时请使用下文的服务器环境文件或配置覆盖目录。

## GitHub Actions 自动部署后端

仓库中的 [部署工作流](.github/workflows/deploy-backend.yml) 仅在 GitHub 的 **Actions → Deploy backend → Run workflow** 手动触发；普通代码提交不会自动部署。服务器需要能通过 SSH 访问，并预装 Java 17、`bash`、`tar` 和 `curl`。部署账号需要能写入家目录（暂存上传包）及正式部署目录的上级目录。

1. 在服务器创建专用部署账号，并将部署公钥添加到该账号的 `~/.ssh/authorized_keys`。在可信环境核对服务器 SSH 主机密钥指纹后，将对应的 `known_hosts` 条目用于下面的 Secret。

   可在本地执行 `ssh-keygen -t ed25519 -f ~/.ssh/babycare_deploy -N ''` 创建专用密钥；将 `babycare_deploy.pub` 的内容添加到服务器账号的 `authorized_keys`，将 `babycare_deploy` 的完整内容添加到 `DEPLOY_SSH_KEY`。`DEPLOY_KNOWN_HOSTS` 可从 `ssh-keyscan -H 服务器地址` 获取，填写前须与服务器实际指纹核对；自定义 SSH 端口时使用 `ssh-keyscan -p 端口 -H 服务器地址`。
2. 在仓库 **Settings → Environments → qiniuyun → Environment secrets** 中添加以下环境 Secrets。部署工作流已绑定 `qiniuyun` 环境：

   | 名称 | 内容 |
   | --- | --- |
   | `DEPLOY_HOST` | 服务器 SSH 地址或域名 |
   | `DEPLOY_USER` | 服务器部署账号 |
   | `DEPLOY_SSH_KEY` | 对应的 SSH 私钥原文，包含首尾标记和换行 |
   | `DEPLOY_KNOWN_HOSTS` | 已核对指纹的服务器 `known_hosts` 条目 |

   SSH 端口不是 22 时，在同一环境的 **Environment variables** 中添加 `DEPLOY_PORT`。可在这里添加 `DEPLOY_DIR` 设置服务器上的完整部署目录，默认是 `/data/babycare-server`；修改路径无需改代码。Nacos 不在部署服务器本机时，再添加 `NACOS_SERVER_ADDR`，值使用 `主机:端口`（例如 `10.0.0.8:8848`）；工作流会在构建前同时替换配置中心和服务注册使用的地址。未配置时默认使用 `127.0.0.1:8848`。
3. 确认 `NACOS_SERVER_ADDR` 指向的 Nacos 中已有命名空间 ID `xc`、`DEFAULT_GROUP` 下的 `common-wxapp` 配置，包含 `WXAPP_DB_CLASS_NAME`、`WXAPP_DB_URL`、`WXAPP_DB_USERNAME` 和 `WXAPP_DB_PASSWORD`。如果命名空间 ID 不同，在部署目录的上级目录创建 `babycare-server.env`，例如：

   ```bash
   NACOS_NAMESPACE=实际命名空间ID
   NACOS_USERNAME=实际用户名
   NACOS_PASSWORD=实际密码
   ```

   此文件是由启动脚本读取的 Shell 配置，默认路径为 `/data/babycare-server.env`，建议设置权限为 `600`。Nacos 和 MySQL 都位于服务器本机时，Nacos 内的 `WXAPP_DB_URL` 应指向服务器本机的 MySQL 地址。需要覆盖随包配置时，可把配置文件放在部署目录上级目录的 `babycare-config/` 中，部署时会复制到新版本的 `config/` 目录。
4. 将本工作流和脚本提交并推送到 `main`。默认程序路径为 `/data/babycare-server/`，日志为 `/data/xcAppLogs/babycare-server-logs/`，上一次部署的版本自动备份到 `/data/babycare-backups/`；设置 `DEPLOY_DIR` 后，这些目录随之移动。无论新版本启动成功还是失败，最新构建都会保留在 `/data/babycare-server/`，方便直接检查日志或手工重启；启动失败时工作流仍会标记为失败，但不会把最新构建移到 `failed-*`，也不会覆盖回旧版本。如先前在家目录启动过旧版本，迁移前先停止旧实例。

SSH 私钥只保存在 GitHub Secret 中；服务器上的 `babycare-server.env`、Nacos 数据库密码和个性化配置无需提交到仓库。

## 微信小程序说明

前端是原生微信小程序，不需要执行 `npm install` 或打包命令。开发时直接在微信开发者工具中导入 `miniprogram` 目录。

### 本地调试

1. 在 `miniprogram/project.config.json` 中把 `appid` 替换成自己的小程序 AppID，或在微信开发者工具导入项目时选择对应 AppID。
2. 启动本项目后端，确认 `http://localhost:8080` 可访问。
3. 在微信开发者工具中导入 `miniprogram` 目录并点击“编译”。开发环境默认请求 `http://localhost:8080`。
4. 使用真机预览时，手机无法访问电脑的 `localhost`。请临时把 `miniprogram/config.js` 中 `develop.apiBaseUrl` 改成电脑的局域网地址，例如 `http://192.168.1.20:8080`，并确保手机与电脑在同一网络、Windows 防火墙允许访问 8080 端口。

前端逻辑测试可在项目根目录执行：

```bash
node --test miniprogram/tests
```

### 体验版与正式版

1. 将后端部署在带有效 HTTPS 证书的备案域名下，例如 `https://api.example.cn`。
2. 修改 `miniprogram/config.js` 中 `trial` 和 `release` 的 `apiBaseUrl`，不要保留示例域名 `https://api.example.com`。
3. 在微信公众平台的“开发管理 → 开发设置 → 服务器域名”中，把该 HTTPS 地址配置为 `request` 合法域名。
4. 在微信公众平台创建订阅消息模板，将模板 ID 同时配置到：
   - `miniprogram/config.js` 的 `develop.templateId`、`trial.templateId` 和 `release.templateId`（本地不测试提醒时可保留 `develop` 为空）
   - 后端环境变量 `WX_TEMPLATE_ID`
5. 后端的 `WX_APPID`、`WX_SECRET` 必须与小程序项目使用的 AppID 对应，AppSecret 只能保存在后端，不能写入小程序代码。
6. 在微信开发者工具中依次完成“预览 → 真机测试 → 上传”，再到微信公众平台将开发版本设为体验版或提交审核，审核通过后发布。

`project.config.json` 默认关闭了开发者工具的合法域名校验，方便本地调试；微信体验版、审核版和正式版仍会执行平台的服务器域名和 HTTPS 校验。

## 默认提醒规则

- 3 小时未记录喂奶提醒
- 4 小时未记录尿便提醒

## 注意事项

- `application.yml` 中的微信配置需要替换为自己账号的 AppID / Secret / Template ID。
- 真实部署前，需要把后端地址和微信域名配置好。

## 版权

MIT
