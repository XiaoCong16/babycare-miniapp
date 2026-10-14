# Nacos Service Registration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Register `babycare-server` with the existing Nacos server while preserving its current Nacos Config behavior.

**Architecture:** Add the Spring Cloud Alibaba Nacos Discovery starter managed by the existing BOM, then configure Discovery beside the existing Config client. Spring Boot auto-configuration will register the application under `spring.application.name`; no Java annotation or business-code change is required.

**Tech Stack:** Java 17, Spring Boot 3.3.1, Spring Cloud 2023.0.3, Spring Cloud Alibaba 2023.0.3.4, Maven, YAML

## Global Constraints

- Preserve all existing Nacos Config behavior and the `common-wxapp` import.
- Use `babycare-server` from `spring.application.name` as the registered service name.
- Use namespace ID `xc`, group `DEFAULT_GROUP`, and the existing Nacos credentials.
- Continue excluding `spring-boot-starter-logging` so the project remains on Log4j2.
- Do not add `@EnableDiscoveryClient` or modify business code and deployment scripts.

---

### Task 1: Enable Nacos service discovery

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/resources/bootstrap.yml`

**Interfaces:**
- Consumes: the existing Spring Cloud Alibaba BOM and `spring.application.name`.
- Produces: an auto-configured Nacos Discovery client that registers `babycare-server` with namespace `xc` and group `DEFAULT_GROUP`.

- [ ] **Step 1: Run a failing dependency/configuration check**

Run from the repository root:

```powershell
$pom = Get-Content -Raw backend/pom.xml
$yaml = Get-Content -Raw backend/src/main/resources/bootstrap.yml
if ($pom -notmatch 'spring-cloud-starter-alibaba-nacos-discovery' -or $yaml -notmatch '(?m)^      discovery:') { exit 1 }
```

Expected: exit code `1` because neither the Discovery starter nor the Discovery configuration exists.

- [ ] **Step 2: Add the Discovery starter**

Add this dependency next to the existing Nacos Config starter in `backend/pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

- [ ] **Step 3: Add Discovery configuration**

Add this block beneath `spring.cloud.nacos.config` in `backend/src/main/resources/bootstrap.yml`:

```yaml
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: xc
        group: DEFAULT_GROUP
        username: nacos
        password: nacos
        register-enabled: true
```

- [ ] **Step 4: Re-run the focused check**

Run the command from Step 1 again.

Expected: exit code `0`.

- [ ] **Step 5: Run project verification**

Run:

```powershell
mvn -f backend/pom.xml clean test package
```

Expected: `BUILD SUCCESS`, with the Discovery dependency copied into the generated distribution's `lib` directory.

- [ ] **Step 6: Review the diff**

Run:

```powershell
git diff --check
git diff -- backend/pom.xml backend/src/main/resources/bootstrap.yml
```

Expected: no whitespace errors and only the intended dependency/configuration changes.
