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

分发包包含 `bin/`、`boot/`、`lib/` 和 `config/`。`bin/boot.config` 默认设置 `-Xms500m -Xmx500m`；也可用 `restart.sh` 重启。当前应用日志写到分发包上级目录的 `xcAppLogs/babycare-server-logs/info.log`；跨日后的下一条日志或单文件达到 200 MB 时，旧日志会压缩成 `info-yyyy-MM-dd-序号.log.gz`，新日志继续写入 `info.log`。同一天最多保留 10 个压缩归档。修改 Nacos 连接参数时编辑分发包内的 `config/bootstrap.yml`。

## 微信小程序说明

1. 在微信开发者工具中打开 `miniprogram` 目录。
2. 配置自己的 AppID。
3. 替换后端地址为你的实际部署地址。
4. 在微信后台配置订阅消息模板。

## 默认提醒规则

- 3 小时未记录喂奶提醒
- 4 小时未记录尿便提醒

## 注意事项

- `application.yml` 中的微信配置需要替换为自己账号的 AppID / Secret / Template ID。
- 真实部署前，需要把后端地址和微信域名配置好。

## 版权

MIT
