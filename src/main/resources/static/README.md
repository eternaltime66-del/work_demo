# 前端静态目录（前后端分离）

```
static/
  app/     # 玩法端（玩家）
  admin/   # 后台管理
  scenario/# 文档（非运行时）
```

## 访问入口

| 端 | 同域（Spring :8081） | 纯静态 |
|----|----------------------|--------|
| 玩法 | http://localhost:8081/app/ | 用任意静态服务器打开 `app/`，并指定 API |
| 后台 | http://localhost:8081/admin/ | 打开 `admin/`，并指定 API |

根路径 `/` 由后端直接返回纯文本 `404`（不跳转）。玩法/后台请访问上表地址。

## 指定后端地址

前端通过 `APP_CONFIG.apiBase` 请求 `/api/*` 与 `/back/*`。

1. URL 参数（会写入 localStorage）：`?api=http://localhost:8081`
2. 控制台：`APP_CONFIG.setApiBase('http://localhost:8081')`
3. 默认：
   - 同域 8081 → 空（相对路径）
   - `file://` 或其它端口 → `http://<host>:8081`

## 本地纯静态示例

```bash
# 终端 A：后端
mvnw spring-boot:run

# 终端 B：只托管前端
npx --yes serve src/main/resources/static -p 5500
```

浏览器打开：

- 玩法：http://localhost:5500/app/?api=http://localhost:8081
- 后台：http://localhost:5500/admin/?api=http://localhost:8081
