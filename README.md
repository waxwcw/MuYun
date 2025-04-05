```shell
docker compose up -d
```

或者 docker 命令：

```shell
docker run -p 54324:5432 -e POSTGRES_PASSWORD=muyun2024 -e POSTGRES_DB=muyun  postgres:17-alpine
```


```shell
./gradlew --console=plain :muyun-boot:quarkusDev
```

## 全局JWT认证开关

项目支持通过配置开启或关闭全局JWT认证机制。

### 配置方法

在`application.yml`中配置：

```yaml
muyun:
  enable-jwt-auth: true  # 默认为true，开启JWT认证
```

当设置为`false`时，所有API请求都会被视为白名单用户，无需认证即可访问。

### 使用场景

1. 开发环境快速测试API，无需每次都提供令牌
2. 内部网络环境中临时关闭认证
3. 系统维护模式

**注意：** 在生产环境中应始终保持`enable-jwt-auth`为`true`，确保API安全。
