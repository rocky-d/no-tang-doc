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

// Mock XMLHttpRequest for upload flows used by DocumentUpload to avoid real network in tests
class XhrUploadMock {
  onprogress: ((this: XMLHttpRequestUpload, ev: ProgressEvent<EventTarget>) => any) | null = null;
}
class XMLHttpRequestMock {
  static DONE = 4;
  upload = new XhrUploadMock();
  status = 201;
  readyState = 0;
  onload: ((this: XMLHttpRequest, ev: Event) => any) | null = null;
  onerror: ((this: XMLHttpRequest, ev: Event) => any) | null = null;
  open(_method: string, _url: string) {}
  setRequestHeader(_key: string, _value: string) {}
  send(_data?: any) {
    // simulate a couple of progress ticks then success
    setTimeout(() => {
      if (this.upload.onprogress) {
        // @ts-ignore minimal shape is enough for tests
        this.upload.onprogress({ lengthComputable: true, loaded: 50, total: 100 });
      }
    }, 5);
    setTimeout(() => {
      if (this.upload.onprogress) {
        // @ts-ignore
        this.upload.onprogress({ lengthComputable: true, loaded: 100, total: 100 });
      }
      this.readyState = XMLHttpRequestMock.DONE;
      this.status = 201;
      // ensure correct `this` binding for the handler
      (this.onload as any)?.call(this, new Event('load'));
    }, 10);
  }
  addEventListener() {}
  removeEventListener() {}
}
// Always override global XMLHttpRequest in tests
// @ts-ignore
global.XMLHttpRequest = XMLHttpRequestMock as any;
