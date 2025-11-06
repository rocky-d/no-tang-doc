import { render } from '@testing-library/react';
import React from 'react';
import { Sheet, SheetContent, SheetTitle, SheetDescription } from '@/components/ui/sheet';

describe('Sheet', () => {
  it('renders content when open', () => {
    render(
      <Sheet open>
        <SheetContent>
          <SheetTitle>Title</SheetTitle>
          <SheetDescription>Desc</SheetDescription>
        </SheetContent>
      </Sheet>
    );
    const content = document.querySelector('[data-slot="sheet-content"]') as HTMLElement;
    expect(content).toBeInTheDocument();
  });
});
