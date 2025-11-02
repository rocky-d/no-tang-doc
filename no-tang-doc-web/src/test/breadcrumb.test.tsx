import { render, screen } from '@testing-library/react';
import React from 'react';
import { Breadcrumb, BreadcrumbList, BreadcrumbItem, BreadcrumbLink, BreadcrumbSeparator, BreadcrumbPage } from '@/components/ui/breadcrumb';

describe('Breadcrumb', () => {
  it('renders structure and current page', () => {
    render(
      <Breadcrumb>
        <BreadcrumbList>
          <BreadcrumbItem>
            <BreadcrumbLink href="#">Home</BreadcrumbLink>
          </BreadcrumbItem>
          <BreadcrumbSeparator />
          <BreadcrumbItem>
            <BreadcrumbPage>Dashboard</BreadcrumbPage>
          </BreadcrumbItem>
        </BreadcrumbList>
      </Breadcrumb>
    );
    expect(screen.getByLabelText('breadcrumb')).toBeInTheDocument();
    const page = screen.getByText('Dashboard');
    expect(page).toHaveAttribute('data-slot', 'breadcrumb-page');
    expect(page).toHaveAttribute('aria-current', 'page');
  });
});

