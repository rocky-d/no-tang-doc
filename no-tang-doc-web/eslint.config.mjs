// javascript
import js from '@eslint/js';
import tseslint from 'typescript-eslint';
import reactPlugin from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import jsxA11y from 'eslint-plugin-jsx-a11y';
import prettier from 'eslint-config-prettier';
import globals from 'globals';

export default tseslint.config(
    {
      ignores: [
        'node_modules/**',
        'dist/**',
        'build/**',
        '.vite/**',
        'coverage/**',
        'playwright-report/**',
        '.idea/**',
        'public/**',
        'scripts/**',
        'target/**',
        '**/*.d.ts',
        '**/*.{test,spec}.{js,jsx,ts,tsx}',
        'eslint.config.*',
        'src/test/**'
      ]
    },
    {
      files: ['src/**/*.{js,jsx,ts,tsx}'],
      extends: [
        js.configs.recommended,
        ...tseslint.configs.recommended,
        reactPlugin.configs.flat.recommended,
        reactPlugin.configs.flat['jsx-runtime'],
        jsxA11y.flatConfigs.recommended,
        prettier
      ],
      plugins: { 'react-hooks': reactHooks },
      languageOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        parserOptions: {
          projectService: false,
          tsconfigRootDir: process.cwd()
        },
        globals: { ...globals.browser, ...globals.node }
      },
      settings: { react: { version: 'detect' } },
      rules: {
        // 关闭无障碍相关规则
        'jsx-a11y/anchor-has-content': 'off',
        'jsx-a11y/heading-has-content': 'off',
        'jsx-a11y/no-autofocus': 'off',
        'jsx-a11y/click-events-have-key-events': 'off',
        'jsx-a11y/no-static-element-interactions': 'off',
        'react/no-unescaped-entities': 'off',
        'react/react-in-jsx-scope': 'off',
        'react/prop-types': 'off',
        'no-unused-vars': 'off',
        '@typescript-eslint/no-unused-vars': [
          'warn',
          { argsIgnorePattern: '^_', varsIgnorePattern: '^_', caughtErrorsIgnorePattern: '^_' }
        ],
        'react-hooks/rules-of-hooks': 'error',
        'react-hooks/exhaustive-deps': 'warn',

        // 关闭需类型信息的规则，避免在非 type-aware 模式下报错
        '@typescript-eslint/no-floating-promises': 'off',
        '@typescript-eslint/no-misused-promises': 'off',

        // 降噪
        '@typescript-eslint/no-unsafe-assignment': 'off',
        '@typescript-eslint/no-unsafe-member-access': 'off',
        '@typescript-eslint/no-unsafe-call': 'off',
        '@typescript-eslint/no-unsafe-return': 'off',
        '@typescript-eslint/restrict-template-expressions': 'off',
        '@typescript-eslint/require-await': 'off',

        'jsx-a11y/anchor-is-valid': 'warn'
      }
    }
);
