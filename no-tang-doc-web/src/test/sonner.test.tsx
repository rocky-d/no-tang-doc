import { render, screen } from '@testing-library/react';
import { vi } from 'vitest';
import { Toaster } from '@/components/ui/sonner';

vi.mock('next-themes', () => ({ useTheme: () => ({ theme: 'light' }) }));

it('renders sonner toaster with theme applied', async () => {
  render(<Toaster />);

  const el = await screen.findByRole('region', {
    name: /notifications/i,
  });

  expect(el).toBeInTheDocument();
});
