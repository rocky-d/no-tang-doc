import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { Tooltip, TooltipTrigger, TooltipContent } from '@/components/ui/tooltip';

describe('Tooltip', () => {
  it('renders content when defaultOpen is true', () => {
    render(
      <Tooltip defaultOpen>
        <TooltipTrigger>Hover me</TooltipTrigger>
        <TooltipContent>Tooltip text</TooltipContent>
      </Tooltip>
    );

    const content = document.querySelector('[data-slot="tooltip-content"]') as HTMLElement;
    expect(content).toBeInTheDocument();
    expect(content).toHaveTextContent('Tooltip text');
  });

  it('shows content on hover interaction', async () => {
    const user = userEvent.setup();
    render(
      <Tooltip>
        <TooltipTrigger>Hover me</TooltipTrigger>
        <TooltipContent>Tooltip hover</TooltipContent>
      </Tooltip>
    );

    await user.hover(screen.getByText('Hover me'));
    const content = document.querySelector('[data-slot="tooltip-content"]') as HTMLElement;
    expect(content).toBeInTheDocument();
    expect(content).toHaveTextContent('Tooltip hover');
  });
});
