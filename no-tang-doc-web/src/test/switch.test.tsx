import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { Switch } from '@/components/ui/switch';

describe('Switch', () => {
  it('renders and toggles checked state', async () => {
    const user = userEvent.setup();
    render(<Switch aria-label="toggle" />);

    const sw = screen.getByRole('switch', { name: 'toggle' });
    expect(sw).toHaveAttribute('data-slot', 'switch');
    expect(sw).toHaveAttribute('data-state', 'unchecked');
    expect(sw).toHaveAttribute('aria-checked', 'false');

    await user.click(sw);
    expect(sw).toHaveAttribute('data-state', 'checked');
    expect(sw).toHaveAttribute('aria-checked', 'true');
  });
});

