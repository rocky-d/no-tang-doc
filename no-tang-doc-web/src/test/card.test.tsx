import { render } from '@testing-library/react';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter, CardAction } from '../../src/components/ui/card';

describe('Card', () => {
  it('renders all slots', () => {
    const { container } = render(
      <Card>
        <CardHeader>
          <CardTitle>Title</CardTitle>
          <CardDescription>Desc</CardDescription>
          <CardAction>Action</CardAction>
        </CardHeader>
        <CardContent>Body</CardContent>
        <CardFooter>Footer</CardFooter>
      </Card>
    );
    expect(container.querySelector('[data-slot="card"]')).toBeInTheDocument();
    expect(container.querySelector('[data-slot="card-header"]')).toBeInTheDocument();
    expect(container.querySelector('[data-slot="card-title"]')).toBeInTheDocument();
    expect(container.querySelector('[data-slot="card-description"]')).toBeInTheDocument();
    expect(container.querySelector('[data-slot="card-action"]')).toBeInTheDocument();
    expect(container.querySelector('[data-slot="card-content"]')).toBeInTheDocument();
    expect(container.querySelector('[data-slot="card-footer"]')).toBeInTheDocument();
  });
});

