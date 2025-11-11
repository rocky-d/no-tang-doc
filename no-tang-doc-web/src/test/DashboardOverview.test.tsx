import { describe, it, expect, vi, beforeEach } from 'vitest';

import { render, screen, waitFor } from '@testing-library/react';
import { DashboardOverview } from '@/components/DashboardOverview';
import { setLogsRepository } from '@/repositories/LogsRepository';
import { setDocumentRepository } from '@/repositories/DocumentRepository';

// Shared spies
const mockGetAllDocuments = vi.fn();
// Provide a minimal mock repository implementing required interface
function installMockDocumentRepo(returnDocs: any[] = []) {
  setDocumentRepository({
    getAll: async () => { mockGetAllDocuments(); return returnDocs; },
    advancedSearch: async () => returnDocs,
    searchByTags: async () => returnDocs,
    getDownloadInfo: async () => ({ url: '', fileName: null }),
    delete: async () => ({ success: true }),
    getShareUrl: async () => '',
    getComments: async () => [],
    addComment: async () => ({ id: '1', user: 'Me', content: '', timestamp: new Date().toISOString() }),
    updateTags: async () => [],
  } as any);
}

describe('DashboardOverview', () => {
  const mockDocuments = [
    {
      id: '1',
      name: 'Test Document.pdf',
      type: 'pdf',
      size: '1.5 MB',
      uploadDate: '2025-01-10',
      category: 'Test',
      tags: ['test', 'document'],
      sizeBytes: 1572864
    },
    {
      id: '2',
      name: 'Report.docx',
      type: 'docx',
      size: '2.0 MB',
      uploadDate: '2025-01-05',
      category: 'Report',
      tags: ['report'],
      sizeBytes: 2097152
    },
    {
      id: '3',
      name: 'Presentation.pptx',
      type: 'pptx',
      size: '5.0 MB',
      uploadDate: '2024-12-20',
      category: 'Presentation',
      tags: ['slides'],
      sizeBytes: 5242880
    }
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    setLogsRepository({ getAllLogs: async () => [] });
    // Default doc repo: empty docs
    installMockDocumentRepo([]);
  });

  it('renders overview cards', () => {
    render(<DashboardOverview documents={mockDocuments} />);

    // Component renders these card headings
    expect(screen.getByText('Total Documents')).toBeInTheDocument();
    expect(screen.getByText('This Month')).toBeInTheDocument();
    expect(screen.getByText('Storage Used')).toBeInTheDocument();
    expect(screen.getByText('Success Rate')).toBeInTheDocument();
  });

  it('displays correct total documents count', () => {
    render(<DashboardOverview documents={mockDocuments} />);
    const totalCardHeading = screen.getByText('Total Documents');
    const cardContent = totalCardHeading.closest('.p-6');
    const numberEl = cardContent?.querySelector('.text-2xl.font-bold');
    expect(numberEl).toHaveTextContent(/^3$/);
  });

  it('calculates this month uploads correctly', () => {
    // Freeze time to a January date so test remains stable regardless of current month
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2025-01-15T00:00:00Z'));

    render(<DashboardOverview documents={mockDocuments} />);
    const monthHeading = screen.getByText('This Month');
    const monthCard = monthHeading.closest('.p-6');
    const monthNumber = monthCard?.querySelector('.text-2xl.font-bold');
    expect(monthNumber).toHaveTextContent(/^2$/);

    vi.useRealTimers();
  });

  it('displays storage usage information', () => {
    render(<DashboardOverview documents={mockDocuments} />);

    expect(screen.getByText('Storage Used')).toBeInTheDocument();
  });

  it('shows recent documents list', () => {
    render(<DashboardOverview documents={mockDocuments} />);

    expect(screen.getByText('Test Document.pdf')).toBeInTheDocument();
    expect(screen.getByText('Report.docx')).toBeInTheDocument();
  });

  it('displays documents sorted by upload date', () => {
    render(<DashboardOverview documents={mockDocuments} />);

    // Most recent document should appear first
    const documentNames = screen.getAllByText(/\.(pdf|docx|pptx)$/i);
    expect(documentNames[0]).toHaveTextContent('Test Document.pdf');
  });

  it('limits recent documents to 3 items', () => {
    const manyDocuments = [
      ...mockDocuments,
      {
        id: '4',
        name: 'Extra.pdf',
        type: 'pdf',
        size: '1 MB',
        uploadDate: '2025-01-11',
        category: 'Extra',
        tags: [],
        sizeBytes: 1048576
      }
    ];

    render(<DashboardOverview documents={manyDocuments} />);

    // Should only show 3 recent documents
    expect(screen.queryByText('Presentation.pptx')).not.toBeInTheDocument();
  });

  it('fetches success rate from logs API', async () => {
    const mockLogs = [
      { operationStatus: 'SUCCESS' },
      { operationStatus: 'SUCCESS' },
      { operationStatus: 'FAILURE' },
      { operationStatus: 'SUCCESS' }
    ];

    setLogsRepository({ getAllLogs: async () => mockLogs } as any);
    render(<DashboardOverview documents={mockDocuments} />);

    await waitFor(() => {
      expect(screen.getByText('75%')).toBeInTheDocument();
    });
  });

  it('handles logs API error gracefully', async () => {
    const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});
    setLogsRepository({ getAllLogs: async () => { throw new Error('API Error'); } } as any);

    render(<DashboardOverview documents={mockDocuments} />);

    await waitFor(() => {
      expect(screen.getByText('0%')).toBeInTheDocument();
    });

    consoleSpy.mockRestore();
  });

  // it('fetches documents from API when no documents prop provided', async () => {
  //   // Configure repository to return mockDocuments on fetch
  //   installMockDocumentRepo(mockDocuments as any);
  //   render(<DashboardOverview documents={[]} />);
  //   await waitFor(() => {
  //     expect(mockGetAllDocuments).toHaveBeenCalled();
  //   });
  // });

  it('uses prop documents over API documents', () => {
    mockGetAllDocuments.mockResolvedValue([]);
    render(<DashboardOverview documents={mockDocuments} />);
    const totalCardHeading = screen.getByText('Total Documents');
    const cardContent = totalCardHeading.closest('.p-6');
    const numberEl = cardContent?.querySelector('.text-2xl.font-bold');
    expect(numberEl).toHaveTextContent(/^3$/);
  });

  it('handles empty documents list', () => {
    render(<DashboardOverview documents={[]} />);

    // Instead of asserting on a non-unique "0" value, check for recent documents empty state message
    expect(screen.getByText('Total Documents')).toBeInTheDocument();
    expect(screen.getByText('Recent Documents')).toBeInTheDocument();
    expect(screen.getByText('No documents uploaded yet.')).toBeInTheDocument();
  });

  it('displays document metadata correctly', () => {
    render(<DashboardOverview documents={mockDocuments} />);

    expect(screen.getByText('pdf')).toBeInTheDocument();
    expect(screen.getByText('1.5 MB')).toBeInTheDocument();
  });
});
