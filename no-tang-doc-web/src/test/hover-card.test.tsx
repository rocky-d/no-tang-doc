import { render } from '@testing-library/react';
import React from 'react';
import { HoverCard, HoverCardTrigger, HoverCardContent } from '@/components/ui/hover-card';

describe('HoverCard', () => {
  it('renders content when open', () => {
    render(
      <HoverCard open>
        <HoverCardTrigger>Hover me</HoverCardTrigger>
        <HoverCardContent>Card</HoverCardContent>
      </HoverCard>
    );

    const content = document.querySelector('[data-slot="hover-card-content"]') as HTMLElement;
    expect(content).toBeInTheDocument();
  });
});

