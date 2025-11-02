import { render } from '@testing-library/react';
import React from 'react';
import { Separator } from '@/components/ui/separator';

describe('Separator', () => {
  it('renders horizontal by default', () => {
    const { container } = render(<Separator />);
    const sep = container.querySelector('[data-slot="separator-root"]') as HTMLElement;
    expect(sep).toBeInTheDocument();
    expect(sep).toHaveAttribute('data-orientation', 'horizontal');
  });

  it('supports vertical orientation', () => {
    const { container } = render(<Separator orientation="vertical" />);
    const sep = container.querySelector('[data-slot="separator-root"]') as HTMLElement;
    expect(sep).toHaveAttribute('data-orientation', 'vertical');
  });
});

