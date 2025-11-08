import { render, screen } from '@testing-library/react';
import React from 'react';
import { Alert, AlertTitle, AlertDescription } from '@/components/ui/alert';

describe('Alert', () => {
  it('renders default alert with title and description', () => {
    render(
      <Alert>
        <AlertTitle>Heads up</AlertTitle>
        <AlertDescription>Something happened</AlertDescription>
      </Alert>
    );
    const alert = screen.getByRole('alert');
    expect(alert).toHaveAttribute('data-slot', 'alert');
    expect(screen.getByText('Heads up')).toHaveAttribute('data-slot', 'alert-title');
    expect(screen.getByText('Something happened')).toHaveAttribute('data-slot', 'alert-description');
  });
});

