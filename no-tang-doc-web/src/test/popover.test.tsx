import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { Popover, PopoverTrigger, PopoverContent } from '@/components/ui/popover';

describe('Popover', () => {
  it('renders content when open', () => {
    render(
      <Popover open>
        <PopoverTrigger>Open</PopoverTrigger>
        <PopoverContent>Content</PopoverContent>
      </Popover>
    );
    const content = document.querySelector('[data-slot="popover-content"]') as HTMLElement;
    expect(content).toBeInTheDocument();
    expect(content).toHaveTextContent('Content');
  });

  it('opens on trigger click (controlled by Radix)', async () => {
    const user = userEvent.setup();
    render(
      <Popover>
        <PopoverTrigger>Toggle</PopoverTrigger>
        <PopoverContent>Popup</PopoverContent>
      </Popover>
    );

    await user.click(screen.getByText('Toggle'));
    const content = document.querySelector('[data-slot="popover-content"]') as HTMLElement;
    expect(content).toBeInTheDocument();
  });
});

