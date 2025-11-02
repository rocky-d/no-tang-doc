# Frontend testing with Vitest

This project uses Vitest + Testing Library for unit tests and generates coverage reports via v8.

## Commands

- Run all tests

```bat
cd /d D:\Code\NoTangDoc\no-tang-doc-web
npm run test
```

- Watch mode

```bat
cd /d D:\Code\NoTangDoc\no-tang-doc-web
npm run test:watch
```

- UI mode (optional)

```bat
cd /d D:\Code\NoTangDoc\no-tang-doc-web
npm run test:ui
```

- Generate coverage report

```bat
cd /d D:\Code\NoTangDoc\no-tang-doc-web
npm run test:coverage
```

Or use the Windows helper script:

```bat
cd /d D:\Code\NoTangDoc\no-tang-doc-web
run-coverage.bat
```

Coverage HTML report path:

```
no-tang-doc-web/test-results/coverage/index.html
```

## Where to put tests

To avoid running unfinished legacy tests, Vitest is configured to only pick up tests under:

```
src/test/**/*.{test,spec}.{ts,tsx}
```

Add new tests there. Example files added:

- `src/test/accordion.test.tsx` – renders a basic Accordion and verifies content
- `src/test/button.test.tsx` – checks Button defaults and variant class

A global setup file lives at `src/test/setup.ts` and:

- Extends `expect` with jest-dom matchers
- Provides minimal mocks for `ResizeObserver` and `IntersectionObserver` used by some UI libs

## Coverage scope

Coverage includes core UI components (`src/components/ui`), general components, and utilities, while excluding pages and legacy test files. Adjust `vitest.config.ts` if you want broader coverage.

## Troubleshooting

- If tests fail due to missing DOM APIs, add small mocks in `src/test/setup.ts`.
- If you want to include legacy tests under `src/components` or `src/pages`, remove the `include` filter in `vitest.config.ts` and fix the tests accordingly.
