import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { Menubar, MenubarMenu, MenubarTrigger, MenubarContent, MenubarItem } from '@/components/ui/menubar';

describe('Menubar', () => {
  it('opens content on trigger click', async () => {
    const user = userEvent.setup();
    render(
      <Menubar>
        <MenubarMenu>
          <MenubarTrigger>File</MenubarTrigger>
          <MenubarContent>
            <MenubarItem>New</MenubarItem>
          </MenubarContent>
        </MenubarMenu>
      </Menubar>
    );

    await user.click(screen.getByText('File'));
    const content = document.querySelector('[data-slot="menubar-content"]') as HTMLElement;
    expect(content).toBeInTheDocument();
    expect(content).toHaveTextContent('New');
  });
});

