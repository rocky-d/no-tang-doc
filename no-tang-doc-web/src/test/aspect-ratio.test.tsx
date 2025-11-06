import { render } from '@testing-library/react';
import React from 'react';
import { AspectRatio } from '@/components/ui/aspect-ratio';

describe('AspectRatio', () => {
  it('renders container', () => {
    const { container } = render(
      <AspectRatio ratio={16/9}>
        <div>content</div>
      </AspectRatio>
    );
    expect(container.querySelector('[data-slot="aspect-ratio"]')).toBeInTheDocument();
  });
});

