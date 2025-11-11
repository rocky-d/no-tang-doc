import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';

const mockUpdateUser = vi.fn();
const mockUser = {
  id: 'u1',
  name: 'John Doe',
  email: 'john@example.com',
  avatar: '',
};

// Mock modules BEFORE importing component
vi.mock('sonner', () => ({ toast: { success: vi.fn(), error: vi.fn() } }));
vi.mock('@/components/AuthContext', () => ({
  useAuth: () => ({ user: mockUser, updateUser: mockUpdateUser })
}));

import { Profile } from '@/components/Profile';

describe('Profile (read-only)', () => {
  const setup = () => render(<Profile />);

  beforeEach(() => {
    vi.clearAllMocks();
    mockUpdateUser.mockReset();
  });

  it('renders headings and shows user basic info', () => {
    setup();
    // Headings and sections
    expect(screen.getByText('Profile')).toBeInTheDocument();
    expect(screen.getByText('Personal Information')).toBeInTheDocument();
    expect(screen.getByText('Basic details about your account')).toBeInTheDocument();
    expect(screen.getByText('Account Information')).toBeInTheDocument();

    // User information
    expect(screen.getByText('John Doe')).toBeInTheDocument();
    expect(screen.getByText('john@example.com')).toBeInTheDocument();
  });

  it('does not render edit controls or editable fields', () => {
    setup();
    // No edit buttons
    expect(screen.queryByRole('button', { name: /Edit Profile/i })).toBeNull();
    expect(screen.queryByRole('button', { name: /Save Changes/i })).toBeNull();
    expect(screen.queryByRole('button', { name: /Cancel/i })).toBeNull();

    // No editable inputs for profile fields
    expect(screen.queryByLabelText(/Full Name/i)).toBeNull();
    expect(screen.queryByLabelText(/Bio/i)).toBeNull();
    expect(screen.queryByLabelText(/Location/i)).toBeNull();
    expect(screen.queryByLabelText(/Website/i)).toBeNull();
    expect(document.getElementById('avatar-upload')).toBeNull();
  });
});
