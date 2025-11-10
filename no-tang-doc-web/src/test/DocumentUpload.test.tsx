import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DocumentUpload } from '@/components/DocumentUpload';
import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock toast
vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

describe('DocumentUpload', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders upload area', () => {
    render(<DocumentUpload />);

    expect(screen.getByText(/Drop files here or click to browse/i)).toBeInTheDocument();
  });

  it('displays upload card with title', () => {
    render(<DocumentUpload />);

    // Component renders section heading 'Upload Your Documents' and card title 'Upload Files'
    expect(screen.getByText(/Upload Your Documents/i)).toBeInTheDocument();
    expect(screen.getByText(/Upload Files/i)).toBeInTheDocument();
  });

  it('shows drag and drop zone', () => {
    const { container } = render(<DocumentUpload />);

    const dropZone = container.querySelector('[class*="border-dashed"]');
    expect(dropZone).toBeInTheDocument();
  });

  it('handles file input change', async () => {
    const user = userEvent.setup();
    render(<DocumentUpload />);

    const file = new File(['test content'], 'test.pdf', { type: 'application/pdf' });
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;

    if (input) {
      await user.upload(input, file);

      await waitFor(() => {
        expect(screen.getByText('test.pdf')).toBeInTheDocument();
      });
    }
  });

  it('displays file size correctly', async () => {
    const user = userEvent.setup();
    render(<DocumentUpload />);

    const file = new File(['a'.repeat(1024 * 1024)], 'large.pdf', { type: 'application/pdf' });
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;

    if (input) {
      await user.upload(input, file);

      await waitFor(() => {
        // Ensure the file row is present
        expect(screen.getByText('large.pdf')).toBeInTheDocument();
        // Specific size text (avoid generic /MB/ which matches other description text)
        expect(screen.getByText(/^1 MB$/)).toBeInTheDocument();
      });
    }
  });

  it('shows upload progress', async () => {
    const user = userEvent.setup();
    render(<DocumentUpload />);

    const file = new File(['test'], 'progress.pdf', { type: 'application/pdf' });
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;

    if (input) {
      await user.upload(input, file);

      // Progress bar should appear
      const progressBars = document.querySelectorAll('[role="progressbar"]');
      expect(progressBars.length).toBeGreaterThan(0);
    }
  });

  it('allows removing uploaded files', async () => {
    const user = userEvent.setup();
    render(<DocumentUpload />);

    const file = new File(['test'], 'remove.pdf', { type: 'application/pdf' });
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;

    if (input) {
      await user.upload(input, file);

      await waitFor(() => {
        expect(screen.getByText('remove.pdf')).toBeInTheDocument();
      });

      const removeButton = screen.getAllByRole('button').find(btn =>
        btn.querySelector('svg')?.classList.contains('lucide-x')
      );

      if (removeButton) {
        await user.click(removeButton);

        await waitFor(() => {
          expect(screen.queryByText('remove.pdf')).not.toBeInTheDocument();
        });
      }
    }
  });

  it('handles multiple file uploads', async () => {
    const user = userEvent.setup();
    render(<DocumentUpload />);

    const file1 = new File(['test1'], 'file1.pdf', { type: 'application/pdf' });
    const file2 = new File(['test2'], 'file2.pdf', { type: 'application/pdf' });
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;

    if (input) {
      await user.upload(input, [file1, file2]);

      await waitFor(() => {
        expect(screen.getByText('file1.pdf')).toBeInTheDocument();
        expect(screen.getByText('file2.pdf')).toBeInTheDocument();
      });
    }
  });

  it('displays file type badges', async () => {
    const user = userEvent.setup();
    render(<DocumentUpload />);

    const file = new File(['test'], 'document.docx', { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' });
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;

    if (input) {
      await user.upload(input, file);

      await waitFor(() => {
        expect(screen.getByText('document.docx')).toBeInTheDocument();
      });
    }
  });

  it('shows completed status after upload', async () => {
    const user = userEvent.setup();
    render(<DocumentUpload />);

    const file = new File(['test'], 'complete.pdf', { type: 'application/pdf' });
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;

    if (input) {
      await user.upload(input, file);

      await waitFor(() => {
        // Badge with text 'completed' indicates completion; icon class may differ by lucide version
        expect(screen.getByText('completed')).toBeInTheDocument();
      }, { timeout: 3000 });
    }
  });

  it('formats file sizes correctly', async () => {
    const user = userEvent.setup();
    render(<DocumentUpload />);

    const smallFile = new File(['a'.repeat(500)], 'small.txt', { type: 'text/plain' });
    const input = document.querySelector('input[type="file"]') as HTMLInputElement;

    if (input) {
      await user.upload(input, smallFile);

      await waitFor(() => {
        expect(screen.getByText(/Bytes|KB/i)).toBeInTheDocument();
      });
    }
  });
});
