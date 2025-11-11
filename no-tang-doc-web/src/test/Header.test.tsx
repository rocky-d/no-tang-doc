import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Header } from '@/components/Header';
import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock AuthContext - must be hoisted
vi.mock('@/components/AuthContext', () => ({
  useAuth: vi.fn(),
}));

// Import the mocked module
import { useAuth } from '@/components/AuthContext';

const mockLogin = vi.fn();
const mockRegister = vi.fn();
const mockLogout = vi.fn();

describe('Header', () => {
  const mockOnNavigateToDashboard = vi.fn();
  const mockOnNavigateHome = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    (useAuth as ReturnType<typeof vi.fn>).mockReturnValue({
      user: null,
      login: mockLogin,
      register: mockRegister,
      logout: mockLogout,
      isLoading: false,
    });
  });

  it('renders NTDoc logo and name', () => {
    render(<Header onNavigateToDashboard={mockOnNavigateToDashboard} onNavigateHome={mockOnNavigateHome} />);
    expect(screen.getByText('NTDoc')).toBeInTheDocument();
  });

  it('renders navigation links', () => {
    render(<Header onNavigateToDashboard={mockOnNavigateToDashboard} onNavigateHome={mockOnNavigateHome} />);

    expect(screen.getByText('Home')).toBeInTheDocument();
    expect(screen.getByText('Features')).toBeInTheDocument();
    expect(screen.getByText('Performance')).toBeInTheDocument();
    expect(screen.getByText('AI Agent')).toBeInTheDocument();
  });

  it('renders Sign In button when user is not logged in', () => {
    render(<Header onNavigateToDashboard={mockOnNavigateToDashboard} onNavigateHome={mockOnNavigateHome} />);
    expect(screen.getByRole('button', { name: /Sign In/i })).toBeInTheDocument();
  });

  it('renders Sign Up button when user is not logged in', () => {
    render(<Header onNavigateToDashboard={mockOnNavigateToDashboard} onNavigateHome={mockOnNavigateHome} />);
    expect(screen.getByRole('button', { name: /Sign Up/i })).toBeInTheDocument();
  });

  it('calls login when Sign In button is clicked', async () => {
    const user = userEvent.setup();
    render(<Header onNavigateToDashboard={mockOnNavigateToDashboard} onNavigateHome={mockOnNavigateHome} />);

    await user.click(screen.getByRole('button', { name: /Sign In/i }));
    expect(mockLogin).toHaveBeenCalledWith('/dashboard');
  });

  it('calls register when Sign Up button is clicked', async () => {
    const user = userEvent.setup();
    render(<Header onNavigateToDashboard={mockOnNavigateToDashboard} onNavigateHome={mockOnNavigateHome} />);

    await user.click(screen.getByRole('button', { name: /Sign Up/i }));
    expect(mockRegister).toHaveBeenCalledWith('/dashboard');
  });

  it('calls onNavigateHome when logo is clicked', async () => {
    const user = userEvent.setup();
    render(<Header onNavigateToDashboard={mockOnNavigateToDashboard} onNavigateHome={mockOnNavigateHome} />);

    await user.click(screen.getByText('NTDoc'));
    expect(mockOnNavigateHome).toHaveBeenCalledTimes(1);
  });

  it('renders Dashboard button when user is logged in', () => {
    (useAuth as ReturnType<typeof vi.fn>).mockReturnValue({
      user: { name: 'John Doe', email: 'john@example.com' },
      login: mockLogin,
      register: mockRegister,
      logout: mockLogout,
      isLoading: false,
    });

    render(<Header onNavigateToDashboard={mockOnNavigateToDashboard} onNavigateHome={mockOnNavigateHome} />);
    expect(screen.getByRole('button', { name: /Dashboard/i })).toBeInTheDocument();
  });

  it('renders user avatar when user is logged in', () => {
    (useAuth as ReturnType<typeof vi.fn>).mockReturnValue({
      user: { name: 'John Doe', email: 'john@example.com' },
      login: mockLogin,
      register: mockRegister,
      logout: mockLogout,
      isLoading: false,
    });

    const { container } = render(<Header onNavigateToDashboard={mockOnNavigateToDashboard} onNavigateHome={mockOnNavigateHome} />);
    expect(container.querySelector('.rounded-full')).toBeInTheDocument();
  });

  it('calls onNavigateToDashboard when Dashboard button is clicked', async () => {
    (useAuth as ReturnType<typeof vi.fn>).mockReturnValue({
      user: { name: 'John Doe', email: 'john@example.com' },
      login: mockLogin,
      register: mockRegister,
      logout: mockLogout,
      isLoading: false,
    });

    const user = userEvent.setup();
    render(<Header onNavigateToDashboard={mockOnNavigateToDashboard} onNavigateHome={mockOnNavigateHome} />);

    await user.click(screen.getByRole('button', { name: /Dashboard/i }));
    expect(mockOnNavigateToDashboard).toHaveBeenCalledTimes(1);
  });

  it('disables Sign In button when loading', () => {
    (useAuth as ReturnType<typeof vi.fn>).mockReturnValue({
      user: null,
      login: mockLogin,
      register: mockRegister,
      logout: mockLogout,
      isLoading: true,
    });

    render(<Header onNavigateToDashboard={mockOnNavigateToDashboard} onNavigateHome={mockOnNavigateHome} />);
    expect(screen.getByRole('button', { name: /Sign In/i })).toBeDisabled();
  });

  it('disables Sign Up button when loading', () => {
    (useAuth as ReturnType<typeof vi.fn>).mockReturnValue({
      user: null,
      login: mockLogin,
      register: mockRegister,
      logout: mockLogout,
      isLoading: true,
    });

    render(<Header onNavigateToDashboard={mockOnNavigateToDashboard} onNavigateHome={mockOnNavigateHome} />);
    expect(screen.getByRole('button', { name: /Sign Up/i })).toBeDisabled();
  });
});
