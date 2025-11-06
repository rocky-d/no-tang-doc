import { render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { vi } from 'vitest';

vi.mock('@/components/ui/use-mobile', () => ({ useIsMobile: () => false }));

import { SidebarProvider, Sidebar, SidebarContent, SidebarHeader, SidebarTrigger } from '@/components/ui/sidebar';

describe('Sidebar (desktop)', () => {
  it('toggles between expanded and collapsed via SidebarTrigger', async () => {
    const user = userEvent.setup();
    const { container } = render(
      <SidebarProvider defaultOpen>
        <Sidebar>
          <SidebarHeader>Header</SidebarHeader>
          <SidebarContent>Content</SidebarContent>
        </Sidebar>
        <SidebarTrigger aria-label="toggle" />
      </SidebarProvider>
    );

    const sidebar = container.querySelector('[data-slot="sidebar"]') as HTMLElement;
    expect(sidebar).toHaveAttribute('data-state', 'expanded');

    await user.click(container.querySelector('[data-slot="sidebar-trigger"]') as HTMLElement);
    expect(sidebar).toHaveAttribute('data-state', 'collapsed');
  });
});

