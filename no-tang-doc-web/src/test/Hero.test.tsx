import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Hero } from '@/components/Hero';
import { describe, it, expect, vi } from 'vitest';

// Mock AuthContext
vi.mock('@/components/AuthContext', () => ({
  useAuth: vi.fn(() => ({ user: null })),
}));

describe('Hero', () => {
  const mockOnStartUploading = vi.fn();

  beforeEach(() => {
    mockOnStartUploading.mockClear();
  });

  it('renders main heading', () => {
    render(<Hero onStartUploading={mockOnStartUploading} />);
    expect(screen.getByText('Your Digital')).toBeInTheDocument();
    expect(screen.getByText('Document Hub')).toBeInTheDocument();
  });

  it('renders description text', () => {
    render(<Hero onStartUploading={mockOnStartUploading} />);
    expect(screen.getByText(/Store, organize, and share your documents securely/i)).toBeInTheDocument();
  });

  it('renders Start Uploading button', () => {
    render(<Hero onStartUploading={mockOnStartUploading} />);
    const button = screen.getByRole('button', { name: /Start Uploading/i });
    expect(button).toBeInTheDocument();
  });

  it('calls onStartUploading when button is clicked', async () => {
    const user = userEvent.setup();
    render(<Hero onStartUploading={mockOnStartUploading} />);

    const button = screen.getByRole('button', { name: /Start Uploading/i });
    await user.click(button);

    expect(mockOnStartUploading).toHaveBeenCalledTimes(1);
  });

  it('renders Secure Storage feature', () => {
    render(<Hero onStartUploading={mockOnStartUploading} />);
    expect(screen.getByText('Secure Storage')).toBeInTheDocument();
  });

  it('renders Fast Access feature', () => {
    render(<Hero onStartUploading={mockOnStartUploading} />);
    expect(screen.getByText('Fast Access')).toBeInTheDocument();
  });

  it('renders workspace image with correct alt text', () => {
    render(<Hero onStartUploading={mockOnStartUploading} />);
    const image = screen.getByAltText('Digital workspace');
    expect(image).toBeInTheDocument();
  });

  it('has correct section structure', () => {
    const { container } = render(<Hero onStartUploading={mockOnStartUploading} />);
    const section = container.querySelector('section#home');
    expect(section).toBeInTheDocument();
  });
});

