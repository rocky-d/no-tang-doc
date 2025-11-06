import { render, screen } from '@testing-library/react';
import React from 'react';
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell, TableFooter, TableCaption } from '@/components/ui/table';

describe('Table', () => {
  it('renders table with header, body and footer', () => {
    render(
      <Table>
        <TableCaption>Caption</TableCaption>
        <TableHeader>
          <TableRow>
            <TableHead>H1</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow>
            <TableCell>C1</TableCell>
          </TableRow>
        </TableBody>
        <TableFooter>
          <TableRow>
            <TableCell>F1</TableCell>
          </TableRow>
        </TableFooter>
      </Table>
    );
    expect(screen.getByText('H1')).toHaveAttribute('data-slot', 'table-head');
    expect(screen.getByText('C1')).toHaveAttribute('data-slot', 'table-cell');
    expect(screen.getByText('F1')).toHaveAttribute('data-slot', 'table-cell');
    expect(screen.getByText('Caption')).toHaveAttribute('data-slot', 'table-caption');
  });
});

