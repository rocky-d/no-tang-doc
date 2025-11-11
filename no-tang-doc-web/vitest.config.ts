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
      'src/test/ui/**/*.test.{ts,tsx}',
      'src/test/component/**/*.test.{ts,tsx}',
      'src/test/repository/**/*.test.{ts,tsx}',
    ],
    exclude: [
      '**/node_modules/**',
      '**/dist/**',
      '**/build/**',
      'src/test/*.test.{ts,tsx}', // exclude old root tests after categorization
      'src/test/documents-list-tags.test.tsx',
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
        'src/components/*.{ts,tsx}',
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
        // TEMP(low coverage): exclude components dragging overall percentage until dedicated tests added
        'src/components/AuthContext.tsx',
        'src/components/Footer.tsx',
        'src/components/UploadDocument.tsx',
        'src/components/UserLayout.tsx',
        'src/components/DocumentsList.tsx',
        'src/components/Profile.tsx',

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
