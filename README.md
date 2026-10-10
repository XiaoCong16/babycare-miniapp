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
- Spring Data JPA
- MySQL 8+
- Flyway
- 微信小程序

## 项目结构

```text
backend/
  pom.xml
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

```bash
cd backend
mvn clean package
java -jar target/babycare-server-0.0.1-SNAPSHOT.jar
```

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
