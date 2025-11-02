import { render, screen } from '@testing-library/react';
import React from 'react';
import { Accordion, AccordionItem, AccordionTrigger, AccordionContent } from '@/components/ui/accordion';

describe('Accordion', () => {
  it('renders an item open by default and shows content', () => {
    render(
      <Accordion type="single" defaultValue="item-1">
        <AccordionItem value="item-1">
          <AccordionTrigger>Section 1</AccordionTrigger>
          <AccordionContent>
            <div>Accordion Content</div>
          </AccordionContent>
        </AccordionItem>
      </Accordion>
    );

    expect(screen.getByText('Section 1')).toBeInTheDocument();
    expect(screen.getByText('Accordion Content')).toBeInTheDocument();
  });
});

