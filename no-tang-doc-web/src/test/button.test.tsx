import { render, screen } from '@testing-library/react';
import React from 'react';
import { Button } from '@/components/ui/button';

describe('Button', () => {
  it('renders with default variant and size', () => {
    render(<Button>Click me</Button>);
    const btn = screen.getByRole('button', { name: 'Click me' });
    expect(btn).toBeInTheDocument();
    expect(btn).toHaveAttribute('data-slot', 'button');
    // default variant should include primary styles
    expect(btn.className).toMatch(/bg-primary/);
  });

  it('applies destructive variant', () => {
    render(<Button variant="destructive">Delete</Button>);
    const btn = screen.getByRole('button', { name: 'Delete' });
    expect(btn.className).toMatch(/bg-destructive/);
  });
});

