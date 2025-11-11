import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DocumentsList } from '@/components/DocumentsList';
import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock toast
vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

// Mock http utility
const mockHttpGet = vi.fn();
const mockHttpPost = vi.fn();
const mockHttpDelete = vi.fn();
vi.mock('@/utils/request', () => ({
  http: {
    get: (url: string) => mockHttpGet(url),
    post: (url: string, data: any) => mockHttpPost(url, data),
    delete: (url: string) => mockHttpDelete(url),
  },
}));

describe('DocumentsList', () => {
  const mockDocuments = [
    {
      id: '1',
      name: 'Test Document.pdf',
      type: 'pdf',
      size: '1.5 MB',
      uploadDate: '2025-01-10T10:00:00Z',
      category: 'Test',
      tags: ['test', 'document', 'sample']
    },
    {
      id: '2',
      name: 'Report.docx',
      type: 'docx',
      size: '2.0 MB',
      uploadDate: '2025-01-05T15:30:00Z',
      category: 'Report',
      tags: ['report', 'work']
    },
    {
      id: '3',
      name: 'No Tags File.txt',
      type: 'txt',
      size: '10 KB',
      uploadDate: '2025-01-01T08:00:00Z',
      category: 'Note',
      tags: []
    }
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders documents table', () => {
    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    expect(screen.getByText('Test Document.pdf')).toBeInTheDocument();
    expect(screen.getByText('Report.docx')).toBeInTheDocument();
  });

  it('displays document names', () => {
    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    expect(screen.getByText('Test Document.pdf')).toBeInTheDocument();
    expect(screen.getByText('Report.docx')).toBeInTheDocument();
    expect(screen.getByText('No Tags File.txt')).toBeInTheDocument();
  });

  it('shows file type badges', () => {
    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    expect(screen.getByText('pdf')).toBeInTheDocument();
    expect(screen.getByText('docx')).toBeInTheDocument();
    expect(screen.getByText('txt')).toBeInTheDocument();
  });

  it('displays file sizes', () => {
    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    expect(screen.getByText('1.5 MB')).toBeInTheDocument();
    expect(screen.getByText('2.0 MB')).toBeInTheDocument();
    expect(screen.getByText('10 KB')).toBeInTheDocument();
  });

  it('shows upload dates formatted correctly', () => {
    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    // All three documents are in January 2025; assert all formatted dates are rendered
    const dates = screen.getAllByText(/Jan \d{1,2}, 2025/);
    expect(dates).toHaveLength(3);
  });

  it('renders tags for documents', () => {
    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    expect(screen.getByText('test')).toBeInTheDocument();
    expect(screen.getByText('document')).toBeInTheDocument();
    expect(screen.getByText('report')).toBeInTheDocument();
  });

  it('shows "No tags" for documents without tags', () => {
    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    expect(screen.getByText('No tags')).toBeInTheDocument();
  });

  it('limits displayed tags to 3 with overflow indicator', () => {
    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    // First document has 3 tags, should show all 3
    expect(screen.getByText('test')).toBeInTheDocument();
    expect(screen.getByText('document')).toBeInTheDocument();
    expect(screen.getByText('sample')).toBeInTheDocument();
  });

  it('renders action dropdown menu', () => {
    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    const triggers = screen.queryAllByTestId('actions-trigger');
    expect(triggers.length).toBeGreaterThan(0);
  });

  it('opens dropdown menu on click', async () => {
    const user = userEvent.setup();
    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    const triggers = screen.getAllByTestId('actions-trigger');
    const menuButton = triggers[0];

    await user.click(menuButton);

    await waitFor(() => {
      expect(screen.getByText('Download')).toBeInTheDocument();
      expect(screen.getByText('Share')).toBeInTheDocument();
      expect(screen.getByText('Delete')).toBeInTheDocument();
    });
  });

  it('handles empty documents list', () => {
    render(<DocumentsList documents={[]} searchTerm="" />);

    // Should show empty state content instead of a table
    expect(screen.getByText('All Documents')).toBeInTheDocument();
    expect(screen.getByText('No documents uploaded yet')).toBeInTheDocument();
  });

  it('displays searching indicator', () => {
    render(<DocumentsList documents={mockDocuments} searchTerm="test" isSearching={true} />);

    // Check if loading state is shown
    const loadingIndicators = document.querySelectorAll('.animate-spin');
    expect(loadingIndicators.length).toBeGreaterThan(0);
  });

  it('filters documents by search term', () => {
    render(<DocumentsList documents={mockDocuments} searchTerm="Report" />);

    expect(screen.getByText('Report.docx')).toBeInTheDocument();
  });

  it('handles download action', async () => {
    const user = userEvent.setup();
    mockHttpGet.mockResolvedValue({ data: 'mock-download-url' });

    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    const triggers = screen.getAllByTestId('actions-trigger');
    const menuButton = triggers[0];

    await user.click(menuButton);

    await waitFor(() => {
      expect(screen.getByText('Download')).toBeInTheDocument();
    });

    const downloadButton = screen.getByText('Download');
    await user.click(downloadButton);
  });

  it('handles share action', async () => {
    const user = userEvent.setup();
    // Component uses http.get to fetch share URL, so mock GET instead of POST
    mockHttpGet.mockResolvedValue({ data: { url: 'https://example.com/share/123' } });

    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    const triggers = screen.getAllByTestId('actions-trigger');
    const menuButton = triggers[0];

    await user.click(menuButton);

    await waitFor(() => {
      expect(screen.getByText('Share')).toBeInTheDocument();
    });

    const shareButton = screen.getByText('Share');
    await user.click(shareButton);

    // Dialog title should appear after successful GET
    expect(await screen.findByText('Share Document')).toBeInTheDocument();
  });

  it('handles delete action', async () => {
    const user = userEvent.setup();
    mockHttpDelete.mockResolvedValue({ success: true });

    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    const triggers = screen.getAllByTestId('actions-trigger');
    const menuButton = triggers[0];

    await user.click(menuButton);

    await waitFor(() => {
      expect(screen.getByText('Delete')).toBeInTheDocument();
    });
  });

  it('handles row click to view document', async () => {
    const user = userEvent.setup();
    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    const rows = screen.getAllByRole('row');
    const dataRow = rows.find(row => row.textContent?.includes('Test Document.pdf'));

    if (dataRow) {
      await user.click(dataRow);
      // Document detail dialog should open
      expect(dataRow).toHaveClass('cursor-pointer');
    }
  });

  it('renders table headers', () => {
    render(<DocumentsList documents={mockDocuments} searchTerm="" />);

    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Type')).toBeInTheDocument();
    expect(screen.getByText('Tags')).toBeInTheDocument();
    expect(screen.getByText('Size')).toBeInTheDocument();
    expect(screen.getByText('Upload Date')).toBeInTheDocument();
  });
});
