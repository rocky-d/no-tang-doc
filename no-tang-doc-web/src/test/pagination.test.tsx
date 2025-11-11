import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
  PaginationEllipsis,
} from '../components/ui/pagination';

describe('Pagination', () => {
  it('renders all parts of the pagination component', () => {
    render(
      <Pagination>
        <PaginationContent>
          <PaginationItem>
            <PaginationPrevious href="#" />
          </PaginationItem>
          <PaginationItem>
            <PaginationLink href="#">1</PaginationLink>
          </PaginationItem>
          <PaginationItem>
            <PaginationEllipsis />
          </PaginationItem>
          <PaginationItem>
            <PaginationNext href="#" />
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    );

    expect(screen.getByText('1')).toBeInTheDocument();
    // These are links, not buttons, and have specific aria-labels
    expect(screen.getByRole('link', { name: /go to previous page/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /go to next page/i })).toBeInTheDocument();
    // Ellipsis is rendered as a span with sr-only text
    expect(screen.getByText('More pages')).toBeInTheDocument();
  });

  it('handles active link correctly', () => {
    render(
      <Pagination>
        <PaginationContent>
          <PaginationItem>
            <PaginationLink href="#" isActive>
              2
            </PaginationLink>
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    );
    const activeLink = screen.getByText('2');
    // Check for the actual classes applied when isActive is true
    expect(activeLink).toHaveClass('bg-background');
    expect(activeLink).toHaveClass('border');
  });

  it('handles disabled state for previous and next buttons', () => {
    render(
      <Pagination>
        <PaginationContent>
          <PaginationItem>
            <PaginationPrevious href="#" disabled />
          </PaginationItem>
          <PaginationItem>
            <PaginationNext href="#" disabled />
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    );
    // Check for disabled attribute on links
    const prevLink = screen.getByRole('link', { name: /go to previous page/i });
    const nextLink = screen.getByRole('link', { name: /go to next page/i });
    expect(prevLink).toHaveAttribute('disabled', '');
    expect(nextLink).toHaveAttribute('disabled', '');
  });

  it('calls onClick when a link is clicked', () => {
    const handleClick = vi.fn();
    render(
      <Pagination>
        <PaginationContent>
          <PaginationItem>
            <PaginationLink href="#" onClick={handleClick}>
              3
            </PaginationLink>
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    );
    fireEvent.click(screen.getByText('3'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('applies custom classNames', () => {
    const { container } = render(
      <Pagination className="custom-pagination">
        <PaginationContent className="custom-content">
          <PaginationItem className="custom-item">
            <PaginationLink href="#" className="custom-link">
              4
            </PaginationLink>
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    );
    expect(container.querySelector('.custom-pagination')).toBeInTheDocument();
    expect(container.querySelector('.custom-content')).toBeInTheDocument();
    expect(container.querySelector('.custom-item')).toBeInTheDocument();
    expect(screen.getByText('4')).toHaveClass('custom-link');
  });
});
