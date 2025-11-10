import { render, screen } from '@testing-library/react';
import React from 'react';
import { Input } from '@/components/ui/input';

describe('Input', () => {
  it('renders with data-slot and base styles', () => {
    render(<Input placeholder="Email" />);
    const el = screen.getByPlaceholderText('Email');
    expect(el).toHaveAttribute('data-slot', 'input');
    expect(el.className).toMatch(/h-9/);
  });

  it('merges custom className', () => {
    render(<Input placeholder="Name" className="custom-class" />);
    const el = screen.getByPlaceholderText('Name');
    expect(el.className).toMatch(/custom-class/);
  });
});

