# NoTang Doc Web

基于 React + TypeScript + Vite 构建的文档管理前端界面。

## 项目结构

```
no-tang-doc-web/
├── public/                # 静态资源文件夹
├── src/                   # 源代码
│   ├── components/        # UI组件
│   │   ├── ui/            # 基础UI组件
│   │   └── figma/         # Figma设计相关组件
│   ├── mock-data/         # 模拟数据
│   ├── pages/             # 页面组件
│   ├── routes/            # 路由配置
│   ├── styles/            # 全局样式
│   ├── utils/             # 工具函数
│   ├── App.tsx            # 应用入口组件
│   └── main.tsx           # 应用入口文件
├── index.html             # HTML模板
├── package.json           # 项目依赖与脚本
├── tsconfig.json          # TypeScript配置
├── vite.config.ts         # Vite配置
├── Dockerfile             # 生产镜像构建（多阶段构建 + nginx）
├── nginx.conf             # nginx 配置（SPA fallback，监听 8080）
└── docker-compose.yml     # 开发环境（vite dev server）
```

## 核心功能

- 用户认证：登录、注册和个人资料管理
- 文档管理：上传、查看、分类和标签管理
- 高级搜索：支持简单搜索和高级搜索功能
- 响应式设计：适配不同设备尺寸

## 开始使用

### 开发环境设置

1. 克隆项目

```bash
git clone https://your-repository-url/no-tang-doc.git
cd no-tang-doc/no-tang-doc-web
```

2. 安装依赖

```bash
npm install
```

3. 启动开发服务器

```bash
npm run dev
```

应用将在 http://localhost:3000 开启。

### 构建生产版本

```bash
npm run build
```

## Docker 部署

该前端已适配 Docker 多阶段构建并使用 nginx 以 SPA 方式提供静态文件，容器监听端口 8080（兼容 DigitalOcean App Platform 默认端口）。

提示：Vite 以构建时变量注入（import.meta.env），即以 VITE_ 开头的环境变量在“构建阶段”生效，不能在“运行时”更改。若需运行时可配置，请见文末“可选增强”。

### 本地构建与运行（Windows cmd）

1) 可选：设置构建时环境变量（示例）

```cmd
SET VITE_KEYCLOAK_URL=https://auth.example.com/
SET VITE_KEYCLOAK_REALM=ntdoc
SET VITE_KEYCLOAK_CLIENT_ID=no-tang-doc-core
SET VITE_API_BASE_URL=https://api.example.com
SET VITE_DOCS_API_PREFIX=https://api.example.com/api/v1/documents
SET VITE_UPLOAD_PATH=/api/v1/documents/upload
SET VITE_OIDC_SCOPES=openid profile email
SET VITE_TEST_REFRESH_INTERVAL_MS=0
```

2) 构建镜像

```cmd
docker build -t adamxin/no-tang-doc-web:latest ^
  --build-arg VITE_KEYCLOAK_URL=%VITE_KEYCLOAK_URL% ^
  --build-arg VITE_KEYCLOAK_REALM=%VITE_KEYCLOAK_REALM% ^
  --build-arg VITE_KEYCLOAK_CLIENT_ID=%VITE_KEYCLOAK_CLIENT_ID% ^
  --build-arg VITE_API_BASE_URL=%VITE_API_BASE_URL% ^
  --build-arg VITE_DOCS_API_PREFIX=%VITE_DOCS_API_PREFIX% ^
  --build-arg VITE_UPLOAD_PATH=%VITE_UPLOAD_PATH% ^
  --build-arg VITE_OIDC_SCOPES=%VITE_OIDC_SCOPES% ^
  --build-arg VITE_TEST_REFRESH_INTERVAL_MS=%VITE_TEST_REFRESH_INTERVAL_MS% .
```

3) 运行容器

```cmd
docker run --rm -p 8080:8080 adamxin/no-tang-doc-web:latest
```

浏览器访问 http://localhost:8080

### 推送镜像到 DigitalOcean 容器镜像仓库（可选）

```cmd
docker login registry.digitalocean.com
REM 假设仓库为 registry.digitalocean.com/your-namespace/no-tang-doc-web

docker tag your-registry/no-tang-doc-web:latest registry.digitalocean.com/your-namespace/no-tang-doc-web:latest

docker push registry.digitalocean.com/your-namespace/no-tang-doc-web:latest
```

### DigitalOcean App Platform 配置

- Component Type：Docker Image
- Source：连接到包含本目录的 Git 仓库；Source Directory 选择 `no-tang-doc-web/`
- Dockerfile：使用仓库中的 `no-tang-doc-web/Dockerfile`
- HTTP Port：8080（容器内部监听端口）
- Build & Run Commands：无需额外命令，Dockerfile 已定义
- 环境变量（Build time）：配置以下变量以注入到 Vite 构建
    - VITE_KEYCLOAK_URL
    - VITE_KEYCLOAK_REALM
    - VITE_KEYCLOAK_CLIENT_ID
    - VITE_API_BASE_URL
    - VITE_DOCS_API_PREFIX
    - VITE_UPLOAD_PATH（可选）
    - VITE_OIDC_SCOPES（可选，默认 openid profile email）
    - VITE_TEST_REFRESH_INTERVAL_MS（可选）

部署完成后，前端通过上述变量访问后端（no-tang-doc-core 在 DOKS 暴露的域名/网关）。

## 可选增强（后续）

- 运行时配置注入：通过生成 `/usr/share/nginx/html/config.json` 并在应用启动时读取，或使用 `envsubst` 注入到配置脚本，避免每次修改后端地址都要重建镜像。
- 健康检查与探针：为 nginx 添加 `healthcheck` 指令或在 App Platform 配置健康检查端点。

# React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default defineConfig([
    globalIgnores(['dist']),
    {
        files: ['**/*.{ts,tsx}'],
        extends: [
            // Other configs...

            // Remove tseslint.configs.recommended and replace with this
            tseslint.configs.recommendedTypeChecked,
            // Alternatively, use this for stricter rules
            tseslint.configs.strictTypeChecked,
            // Optionally, add this for stylistic rules
            tseslint.configs.stylisticTypeChecked,

            // Other configs...
        ],
        languageOptions: {
            parserOptions: {
                project: ['./tsconfig.node.json', './tsconfig.app.json'],
                tsconfigRootDir: import.meta.dirname,
            },
            // other options...
        },
    },
])
```

You can also install [eslint-plugin-react-x](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.mjs
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
    globalIgnores(['dist']),
    {
        files: ['**/*.{ts,tsx}'],
        extends: [
            // Other configs...
            // Enable lint rules for React
            reactX.configs['recommended-typescript'],
            // Enable lint rules for React DOM
            reactDom.configs.recommended,
        ],
        languageOptions: {
            parserOptions: {
                project: ['./tsconfig.node.json', './tsconfig.app.json'],
                tsconfigRootDir: import.meta.dirname,
            },
            // other options...
        },
    },
])
```
