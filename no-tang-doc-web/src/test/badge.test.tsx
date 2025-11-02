import { render, screen } from '@testing-library/react';
import React from 'react';
import { Badge } from '@/components/ui/badge';

describe('Badge', () => {
  it('renders with default variant and data-slot', () => {
    render(<Badge>New</Badge>);
    const el = screen.getByText('New');
    expect(el).toHaveAttribute('data-slot', 'badge');
  });

  it('applies destructive variant styles', () => {
    render(<Badge variant="destructive">Alert</Badge>);
    const el = screen.getByText('Alert');
    expect(el.className).toMatch(/bg-destructive/);
  });
});

