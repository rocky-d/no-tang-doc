import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react-swc';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  test: {
    include: [
      'src/test/**/*.test.{ts,tsx}',
      'src/components/**/*.test.{ts,tsx}',
    ],
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    css: true,
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      reportsDirectory: './test-results/coverage',
      include: [
        'src/components/ui/**/*.{ts,tsx}',
      ],
      exclude: [
        'src/test/**/*',
        '**/*.{test,spec}.{ts,tsx}',
        '**/*.d.ts',
        'src/main.tsx',
        'src/vite-env.d.ts',
        'src/mock-data/**/*',
        'src/components/figma/**/*',
        'src/pages/**/*',
        // exclude only internal hook not directly tested
        'src/components/ui/use-mobile.ts',
      ],
      thresholds: {
        lines: 80,
        statements: 80,
        functions: 80,
        branches: 60,
      },
    },
  },
});
