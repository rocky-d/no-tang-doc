import { render, screen } from '@testing-library/react';
import React from 'react';
import { Pagination, PaginationContent, PaginationItem, PaginationLink, PaginationPrevious, PaginationNext, PaginationEllipsis } from '@/components/ui/pagination';

describe('Pagination', () => {
  it('renders with links and active state', () => {
    render(
      <Pagination>
        <PaginationContent>
          <PaginationItem><PaginationPrevious href="#" /></PaginationItem>
          <PaginationItem><PaginationLink href="#" isActive>1</PaginationLink></PaginationItem>
          <PaginationItem><PaginationEllipsis /></PaginationItem>
          <PaginationItem><PaginationLink href="#">2</PaginationLink></PaginationItem>
          <PaginationItem><PaginationNext href="#" /></PaginationItem>
        </PaginationContent>
      </Pagination>
    );
    expect(screen.getByLabelText('pagination')).toBeInTheDocument();
    const active = screen.getByRole('link', { name: '1' });
    expect(active).toHaveAttribute('aria-current', 'page');
  });
});
