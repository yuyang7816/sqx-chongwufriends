# 和它交个朋友管理系统

## 技术栈

- Vue 3
- Vite 5
- Element Plus 2
- Pinia 3
- Vue Router 4

## 启动

```bash
nvm use
npm install
npm run dev
```

默认账号：

- `admin / 123456`
- `operator / 123456`

## 目录结构

```text
src
├─api
│  ├─http.js
│  └─modules
├─components
│  └─common
├─composables
├─constants
├─layouts
├─mock
│  ├─database.js
│  └─modules
├─router
├─stores
├─styles
└─views
   ├─content
   ├─decoration
   ├─system
   └─user
```

## 对接规范

所有接口模块均已预留 Java 后端地址占位，位于 `src/api/modules/*.js`。

### GET 分页

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "keyword": "",
  "status": "enabled"
}
```

### POST 新增/编辑

```json
{
  "id": 1001,
  "name": "示例名称"
}
```

### 统一响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 0,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 当前前端联调方式

- 页面统一调用 `src/api/modules/*.js`
- 模拟数据统一落在 `src/mock/modules/*.js`
- 切换真实后端时，仅需替换模块内部 `resolveMock(...)`
- 保留 `get/post` 封装与拦截器，不需要改动页面层
