import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { Toggle } from '@/components/ui/toggle';

describe('Toggle', () => {
  it('toggles on click', async () => {
    const user = userEvent.setup();
    render(<Toggle aria-label="bold">B</Toggle>);
    const btn = screen.getByRole('button', { name: 'bold' });
    expect(btn).toHaveAttribute('data-state', 'off');
    await user.click(btn);
    expect(btn).toHaveAttribute('data-state', 'on');
  });
});
