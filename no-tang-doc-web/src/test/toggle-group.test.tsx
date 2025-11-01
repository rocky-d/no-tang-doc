import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group';

describe('ToggleGroup', () => {
  it('selects a single item', async () => {
    const user = userEvent.setup();
    render(
      <ToggleGroup type="single" aria-label="align">
        <ToggleGroupItem value="left">L</ToggleGroupItem>
        <ToggleGroupItem value="center">C</ToggleGroupItem>
      </ToggleGroup>
    );
    const left = screen.getByRole('radio', { name: 'L' });
    expect(left).toHaveAttribute('data-state', 'off');
    await user.click(left);
    expect(left).toHaveAttribute('data-state', 'on');
    expect(left).toHaveAttribute('aria-checked', 'true');
  });
});
