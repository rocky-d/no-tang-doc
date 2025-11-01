import { render, screen } from '@testing-library/react';
import React from 'react';
import { Label } from '@/components/ui/label';

describe('Label', () => {
  it('renders with data-slot and associates with input via htmlFor', () => {
    render(
      <div>
        <Label htmlFor="name">Name</Label>
        <input id="name" />
      </div>
    );

    const label = screen.getByText('Name');
    expect(label).toHaveAttribute('data-slot', 'label');
    expect(label).toHaveAttribute('for', 'name');
  });
});

