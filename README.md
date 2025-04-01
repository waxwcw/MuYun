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
