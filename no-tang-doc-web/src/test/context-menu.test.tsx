import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';
import { ContextMenu, ContextMenuTrigger, ContextMenuContent, ContextMenuItem } from '@/components/ui/context-menu';

describe('ContextMenu', () => {
  it('opens on contextmenu (right click)', () => {
    render(
      <ContextMenu>
        <ContextMenuTrigger>Target</ContextMenuTrigger>
        <ContextMenuContent>
          <ContextMenuItem>Copy</ContextMenuItem>
        </ContextMenuContent>
      </ContextMenu>
    );

    const trigger = screen.getByText('Target');
    fireEvent.contextMenu(trigger);
    const content = document.querySelector('[data-slot="context-menu-content"]') as HTMLElement;
    expect(content).toBeInTheDocument();
    expect(content).toHaveTextContent('Copy');
  });
});

