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
└── docker-compose.yml     # Docker部署配置
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
// eslint.config.js
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
