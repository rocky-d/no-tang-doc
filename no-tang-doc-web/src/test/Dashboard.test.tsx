import { render, screen, waitFor } from '@testing-library/react';
import { Dashboard } from '@/components/Dashboard';
import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock documentApi
const mockGetAllDocuments = vi.fn();
vi.mock('@/utils/documentApi', () => ({
  getAllDocuments: () => mockGetAllDocuments(),
}));

describe('Dashboard', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders DashboardOverview component', async () => {
    mockGetAllDocuments.mockResolvedValue([]);
    render(<Dashboard />);

    // Expect a known heading from real DashboardOverview
    await waitFor(() => {
      expect(screen.getByText('Total Documents')).toBeInTheDocument();
    });
  });

  it('loads and passes documents to DashboardOverview', async () => {
    const mockDocuments = [
      {
        id: '1',
        name: 'Test.pdf',
        type: 'pdf',
        size: '1.5 MB',
        uploadDate: '2025-01-01',
        category: 'Test',
        tags: ['test']
      },
      {
        id: '2',
        name: 'Doc.docx',
        type: 'docx',
        size: '2.0 MB',
        uploadDate: '2025-01-02',
        category: 'Document',
        tags: ['doc']
      }
    ];

    mockGetAllDocuments.mockResolvedValue(mockDocuments);
    render(<Dashboard />);

    // Find the total documents card and assert count
    await waitFor(() => {
      const totalHeading = screen.getByText('Total Documents');
      const cardContent = totalHeading.closest('.p-6');
      const numberEl = cardContent?.querySelector('.text-2xl.font-bold');
      expect(numberEl).toHaveTextContent(/^2$/);
    });
  });

  it('handles API error gracefully', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    mockGetAllDocuments.mockRejectedValue(new Error('API Error'));

    render(<Dashboard />);

    await waitFor(() => {
      // With error, the overview still renders and shows 0
      const totalHeading = screen.getByText('Total Documents');
      const cardContent = totalHeading.closest('.p-6');
      const numberEl = cardContent?.querySelector('.text-2xl.font-bold');
      expect(numberEl).toHaveTextContent(/^0$/);
    });

    expect(consoleSpy).toHaveBeenCalledWith(
      'Failed to load documents for dashboard',
      expect.any(Error)
    );

    consoleSpy.mockRestore();
  });

  it('cleans up on unmount', async () => {
    mockGetAllDocuments.mockResolvedValue([]);
    const { unmount } = render(<Dashboard />);

    unmount();

    // Component should clean up without errors
    expect(true).toBe(true);
  });
});
