import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthPage } from '@/components/AuthPage';
import { describe, it, expect, vi, beforeEach } from 'vitest';

const mockLogin = vi.fn();
const mockRegister = vi.fn();

vi.mock('@/components/AuthContext', () => ({
  useAuth: () => ({ login: mockLogin, register: mockRegister, isLoading: false })
}));

describe('AuthPage (SSO)', () => {
  const mockOnBack = vi.fn();

  beforeEach(() => {
    mockOnBack.mockClear();
    mockLogin.mockReset();
    mockRegister.mockReset();
  });

  it('renders branding and Back to Home', () => {
    render(<AuthPage onBack={mockOnBack} />);
    expect(screen.getByText('NTDoc')).toBeInTheDocument();
    expect(screen.getByText('Back to Home')).toBeInTheDocument();
  });

  it('calls onBack when Back to Home is clicked', async () => {
    const user = userEvent.setup();
    render(<AuthPage onBack={mockOnBack} />);
    await user.click(screen.getByText('Back to Home'));
    expect(mockOnBack).toHaveBeenCalledTimes(1);
  });

  it('shows SSO actions and triggers login', async () => {
    const user = userEvent.setup();
    render(<AuthPage onBack={mockOnBack} />);
    const signIn = screen.getByRole('button', { name: /Sign in/i });
    expect(signIn).toBeInTheDocument();
    await user.click(signIn);
    expect(mockLogin).toHaveBeenCalled();
  });

  it('triggers register flow', async () => {
    const user = userEvent.setup();
    render(<AuthPage initialMode="register" onBack={mockOnBack} />);
    const createBtn = screen.getByRole('button', { name: /Create account/i });
    await user.click(createBtn);
    expect(mockRegister).toHaveBeenCalled();
  });

  it('renders correct heading based on initialMode', () => {
    const { rerender } = render(<AuthPage onBack={mockOnBack} />);
    // Use role-based query to avoid collision with button text
    expect(screen.getByRole('heading', { level: 2, name: 'Sign in' })).toBeInTheDocument();
    rerender(<AuthPage initialMode="register" onBack={mockOnBack} />);
    expect(screen.getByRole('heading', { level: 2, name: 'Create your account' })).toBeInTheDocument();
  });
});
