import { render } from '@testing-library/react';
import React from 'react';
import { Select, SelectTrigger, SelectContent, SelectItem, SelectValue } from '@/components/ui/select';

describe('Select', () => {
  it('renders content when open', () => {
    render(
      <Select open>
        <SelectTrigger>
          <SelectValue placeholder="Pick" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="a">A</SelectItem>
        </SelectContent>
      </Select>
    );

    const content = document.querySelector('[data-slot="select-content"]') as HTMLElement;
    expect(content).toBeInTheDocument();
    expect(content).toHaveTextContent('A');
  });
});

