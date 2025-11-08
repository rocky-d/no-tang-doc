import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';

describe('RadioGroup', () => {
  it('renders radios and selects one', async () => {
    const user = userEvent.setup();
    render(
      <RadioGroup defaultValue="a" aria-label="letters">
        <label>
          <RadioGroupItem value="a" /> A
        </label>
        <label>
          <RadioGroupItem value="b" /> B
        </label>
      </RadioGroup>
    );
    const a = screen.getByRole('radio', { name: /a/i });
    const b = screen.getByRole('radio', { name: /b/i });
    expect(a).toHaveAttribute('data-state', 'checked');
    await user.click(b);
    expect(b).toHaveAttribute('data-state', 'checked');
  });
});

