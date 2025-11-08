import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem } from '@/components/ui/dropdown-menu';

describe('DropdownMenu', () => {
  it('opens on trigger click and shows content', async () => {
    const user = userEvent.setup();
    render(
      <DropdownMenu>
        <DropdownMenuTrigger>Open</DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuItem>Item 1</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    );

    await user.click(screen.getByText('Open'));
    const content = document.querySelector('[data-slot="dropdown-menu-content"]') as HTMLElement;
    expect(content).toBeInTheDocument();
    expect(content).toHaveTextContent('Item 1');
  });
});

