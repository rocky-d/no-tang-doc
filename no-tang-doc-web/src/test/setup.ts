// Vitest setup file
import '@testing-library/jest-dom/vitest';

// Minimal mocks for APIs not implemented in JSDOM that may be used by UI libs
class ResizeObserverMock {
  observe() {}
  unobserve() {}
  disconnect() {}
}

// @ts-ignore
global.ResizeObserver = global.ResizeObserver || ResizeObserverMock;

class IntersectionObserverMock {
  constructor() {}
  observe() {}
  unobserve() {}
  disconnect() {}
  takeRecords() { return []; }
}

// @ts-ignore
global.IntersectionObserver = global.IntersectionObserver || IntersectionObserverMock;

// Some libraries call scrollIntoView which isn't implemented in JSDOM
if (!Element.prototype.scrollIntoView) {
  // @ts-ignore
  Element.prototype.scrollIntoView = function scrollIntoView() {};
}
