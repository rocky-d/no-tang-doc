// filepath: d:\Code\NoTangDoc\no-tang-doc-web\src\test\DocumentRepository.test.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { HttpDocumentRepository, setDocumentRepository } from '../repositories/DocumentRepository';
import { http } from '../utils/request';

// Helper to build a backend-style document
function makeBackendDoc(overrides: Partial<any> = {}) {
  return {
    documentId: overrides.documentId || 'id-' + Math.random().toString(36).slice(2, 8),
    fileName: overrides.fileName || 'Sample Document.pdf',
    fileSize: overrides.fileSize ?? 123456,
    mimeType: overrides.mimeType || 'application/pdf',
    description: overrides.description || 'Test file',
    uploadTime: overrides.uploadTime || '2024-01-01T12:00:00Z',
    tags: overrides.tags || ['alpha', 'beta'],
  };
}

describe('DocumentRepository', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    setDocumentRepository(new HttpDocumentRepository('/api/docs')); // ensure default repo points to test path
  });

  it('maps backend documents into UI model', async () => {
    const backendDocs = [
      makeBackendDoc({ documentId: '1', fileName: 'Report Q1 2024.pdf', fileSize: 2048 }),
      makeBackendDoc({ documentId: '2', fileName: 'image.png', mimeType: 'image/png', fileSize: 512 }),
    ];
    vi.spyOn(http, 'get').mockResolvedValue({ code: 0, data: { documents: backendDocs } });

    const repo = new HttpDocumentRepository('/api/docs');
    const docs = await repo.getAll();
    expect(docs).toHaveLength(2);
    expect(docs[0].id).toBe('1');
    expect(docs[0].name).toBe('Report Q1 2024.pdf');
    expect(docs[0].type).toBe('pdf');
    expect(docs[0].size).toMatch(/KB|B|MB/); // formatted size
    expect(docs[1].type).toBe('png');
  });

  it('advancedSearch filters by name, type, category, tags', async () => {
    const backendDocs = [
      makeBackendDoc({ documentId: '1', fileName: 'Annual Report 2023.pdf', tags: ['finance', 'yearly'] }),
      makeBackendDoc({ documentId: '2', fileName: 'Notes.txt', mimeType: 'text/plain', tags: ['personal'] }),
    ];
    vi.spyOn(http, 'get').mockResolvedValue({ code: 0, data: { documents: backendDocs } });

    const repo = new HttpDocumentRepository('/api/docs');
    const all = await repo.getAll();
    expect(all).toHaveLength(2);

    const searchByTag = await repo.advancedSearch('finance');
    expect(searchByTag).toHaveLength(1);
    expect(searchByTag[0].tags).toContain('finance');

    const searchByType = await repo.advancedSearch('txt');
    expect(searchByType).toHaveLength(1);
    expect(searchByType[0].type).toBe('txt');
  });

  it('searchByTags returns matching documents', async () => {
    const backendDocs = [
      makeBackendDoc({ documentId: 'a', fileName: 'DocA.pdf', tags: ['one', 'two'] }),
      makeBackendDoc({ documentId: 'b', fileName: 'DocB.pdf', tags: ['two', 'three'] }),
      makeBackendDoc({ documentId: 'c', fileName: 'DocC.pdf', tags: ['four'] }),
    ];
    vi.spyOn(http, 'get').mockResolvedValue({ code: 0, data: { documents: backendDocs } });
    const repo = new HttpDocumentRepository('/api/docs');

    const results = await repo.searchByTags(['two']);
    expect(results.map((d) => d.id)).toEqual(['a', 'b']);

    const multi = await repo.searchByTags(['three', 'four']);
    expect(multi.map((d) => d.id).sort()).toEqual(['b', 'c']);

    const empty = await repo.searchByTags([]);
    expect(empty).toHaveLength(3);
  });

  it('getAll throws on non-success code', async () => {
    vi.spyOn(http, 'get').mockResolvedValue({ code: 500, message: 'error', data: { documents: [] } });
    const repo = new HttpDocumentRepository('/api/docs');
    await expect(repo.getAll()).rejects.toThrow(/error|Failed/);
  });
});

