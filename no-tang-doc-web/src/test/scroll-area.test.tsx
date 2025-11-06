import { render } from '@testing-library/react';
import React from 'react';
import { ScrollArea } from '@/components/ui/scroll-area';

describe('ScrollArea', () => {
  it('renders viewport', () => {
    const { container } = render(
      <ScrollArea>
        <div>content</div>
      </ScrollArea>
    );
    expect(container.querySelector('[data-slot="scroll-area-viewport"]')).toBeInTheDocument();
  });
});
