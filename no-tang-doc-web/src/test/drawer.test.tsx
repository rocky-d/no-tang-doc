import { render } from '@testing-library/react';
import React from 'react';
import { Drawer, DrawerContent, DrawerTitle, DrawerDescription } from '@/components/ui/drawer';

describe('Drawer', () => {
  it('renders content when open', () => {
    render(
        <Drawer open>
          <DrawerContent>
            <DrawerTitle>Title</DrawerTitle>
            <DrawerDescription>Desc</DrawerDescription>
          </DrawerContent>
        </Drawer>
    );
    const content = document.querySelector('[data-slot="drawer-content"]') as HTMLElement;
    expect(content).toBeInTheDocument();
  });
});

