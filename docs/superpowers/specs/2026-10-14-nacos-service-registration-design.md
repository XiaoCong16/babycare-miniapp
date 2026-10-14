# Nacos 服务注册设计

## 背景

`babycare-server` 当前能够通过 Nacos Config 读取配置，但没有引入 Nacos Discovery，因此应用不会注册到 Nacos 服务列表。

## 目标

- 保留现有 Nacos Config 行为。
- 将 `babycare-server` 注册到当前 Nacos 实例。
- 使用 `spring.application.name` 作为服务名，并沿用现有命名空间、分组和认证信息。

## 方案

1. 在 Maven 依赖中增加 `spring-cloud-starter-alibaba-nacos-discovery`，版本继续由现有 Spring Cloud Alibaba BOM 管理。
2. 在 `bootstrap.yml` 的 `spring.cloud.nacos` 下增加 `discovery` 配置。
3. 不增加 `@EnableDiscoveryClient`；当前 Spring Cloud 版本使用 starter 自动配置。
4. 不修改业务代码、数据库配置或部署脚本。

## 配置与数据流

应用启动后，Nacos Config 继续读取 `common-wxapp`。Spring Cloud Alibaba Discovery 自动读取服务名、端口和 Discovery 配置，并向 Nacos 注册实例。服务应出现在 `xc` 命名空间、`DEFAULT_GROUP` 分组下。

## 异常与兼容性

- Nacos 不可访问或认证失败时，启动日志应给出 Discovery 注册错误。
- `namespace` 必须是 Nacos 命名空间 ID；沿用当前已成功读取配置的 `xc`。
- 保留项目统一排除 Logback、使用 Log4j2 的依赖约定。

## 验证

- 用静态配置检查确认 Discovery 依赖和必需参数存在。
- 执行 Maven 测试和打包，确认依赖解析及构建成功。
- 部署后由运维侧在 Nacos 控制台确认 `babycare-server` 实例上线。
