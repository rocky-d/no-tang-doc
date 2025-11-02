import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { Checkbox } from '@/components/ui/checkbox';

describe('Checkbox', () => {
  it('renders with data-slot and can be checked/unchecked', async () => {
    const user = userEvent.setup();
    render(<Checkbox aria-label="accept" />);

    const cb = screen.getByRole('checkbox', { name: 'accept' });
    expect(cb).toHaveAttribute('data-slot', 'checkbox');
    expect(cb).toHaveAttribute('data-state', 'unchecked');

    await user.click(cb);
    expect(cb).toHaveAttribute('data-state', 'checked');

    await user.click(cb);
    expect(cb).toHaveAttribute('data-state', 'unchecked');
  });

  it('supports defaultChecked', () => {
    render(<Checkbox aria-label="accept" defaultChecked />);
    const cb = screen.getByRole('checkbox', { name: 'accept' });
    expect(cb).toHaveAttribute('data-state', 'checked');
  });
});

