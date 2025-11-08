import { render } from '@testing-library/react';
import React from 'react';
import { Skeleton } from '@/components/ui/skeleton';

describe('Skeleton', () => {
  it('renders with data-slot and merges classes', () => {
    const { container } = render(<Skeleton className="h-4 w-4" />);
    const sk = container.querySelector('[data-slot="skeleton"]') as HTMLElement;
    expect(sk).toBeInTheDocument();
    expect(sk.className).toMatch(/animate-pulse/);
    expect(sk.className).toMatch(/h-4/);
    expect(sk.className).toMatch(/w-4/);
  });
});

