# Game Data Service

## 本地启动 MySQL/Redis

```bash
docker compose up -d
```

默认会启动：

- MySQL：`localhost:3306`，数据库 `game_db`，用户名 `root`，密码 `root`
- Redis：`localhost:6379`

初始化 SQL 位于 [init.sql](file:///workspace/data-service/docker/mysql/init.sql)。

## 项目运行

确保 JDK 17+

```bash
./mvnw -DskipTests clean package
./mvnw spring-boot:run
```

如果不使用 Maven Wrapper，可直接：

```bash
mvn -DskipTests clean package
mvn spring-boot:run
```
