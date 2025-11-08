import { render, screen } from '@testing-library/react';
import React from 'react';
import { Textarea } from '@/components/ui/textarea';

describe('Textarea', () => {
  it('renders with data-slot and base styles', () => {
    render(<Textarea placeholder="Your message" />);
    const ta = screen.getByPlaceholderText('Your message');
    expect(ta).toHaveAttribute('data-slot', 'textarea');
    expect(ta.className).toMatch(/resize-none/);
  });

  it('merges custom className', () => {
    render(<Textarea placeholder="Note" className="custom-class" />);
    const ta = screen.getByPlaceholderText('Note');
    expect(ta.className).toMatch(/custom-class/);
  });
});

