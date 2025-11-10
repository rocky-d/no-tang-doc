import { render } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { Skeleton } from '../components/ui/skeleton';

describe('Skeleton', () => {
  it('renders with default styling', () => {
    const { container } = render(<Skeleton />);
    const skeleton = container.firstChild as HTMLElement;
    expect(skeleton).toBeInTheDocument();
    expect(skeleton).toHaveClass('animate-pulse');
    expect(skeleton).toHaveClass('rounded-md');
    expect(skeleton).toHaveClass('bg-accent');
  });

  it('applies custom className', () => {
    const { container } = render(<Skeleton className="custom-skeleton h-20 w-20" />);
    const skeleton = container.firstChild as HTMLElement;
    expect(skeleton).toHaveClass('custom-skeleton');
    expect(skeleton).toHaveClass('h-20');
    expect(skeleton).toHaveClass('w-20');
  });

  it('renders with custom dimensions', () => {
    const { container } = render(<Skeleton className="h-12 w-full" />);
    const skeleton = container.firstChild as HTMLElement;
    expect(skeleton).toHaveClass('h-12');
    expect(skeleton).toHaveClass('w-full');
  });

  it('can be used as a circle', () => {
    const { container } = render(<Skeleton className="h-12 w-12 rounded-full" />);
    const skeleton = container.firstChild as HTMLElement;
    expect(skeleton).toHaveClass('rounded-full');
  });
});
