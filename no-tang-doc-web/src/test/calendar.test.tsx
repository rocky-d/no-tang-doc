import { render, screen } from '@testing-library/react';
import React from 'react';
import { Calendar } from '@/components/ui/calendar';

describe('Calendar', () => {
  it('renders day grid and nav buttons', () => {
    render(<Calendar />);
    expect(screen.getByRole('grid')).toBeInTheDocument();
    expect(screen.getAllByRole('button').length).toBeGreaterThan(0);
  });
});

