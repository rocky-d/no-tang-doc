import { render, screen } from '@testing-library/react';
import React from 'react';
import { Progress } from '@/components/ui/progress';

describe('Progress', () => {
  it('renders with data-slot attributes', () => {
    render(<Progress value={50} aria-label="progress" />);
    const bar = screen.getByLabelText('progress');
    expect(bar).toHaveAttribute('data-slot', 'progress');
    const indicator = bar.querySelector('[data-slot="progress-indicator"]') as HTMLElement;
    expect(indicator).toBeInTheDocument();
  });

  it('applies transform based on value', () => {
    const { rerender } = render(<Progress value={25} aria-label="progress" />);
    let indicator = screen.getByLabelText('progress').querySelector('[data-slot="progress-indicator"]') as HTMLElement;
    expect(indicator.style.transform).toContain('translateX(-75%)');

    rerender(<Progress value={80} aria-label="progress" />);
    indicator = screen.getByLabelText('progress').querySelector('[data-slot="progress-indicator"]') as HTMLElement;
    expect(indicator.style.transform).toContain('translateX(-20%)');
  });
});

