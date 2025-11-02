import { render, screen } from '@testing-library/react';
import React from 'react';
import { vi } from 'vitest';
import { Carousel, CarouselContent, CarouselItem, CarouselNext, CarouselPrevious } from '@/components/ui/carousel';

const scrollPrev = vi.fn();
const scrollNext = vi.fn();

vi.mock('embla-carousel-react', () => {
  return {
    default: (opts: any) => [vi.fn(), { canScrollPrev: () => true, canScrollNext: () => true, scrollPrev, scrollNext, on: vi.fn(), off: vi.fn() }],
  };
});

describe('Carousel', () => {
  it('renders items and controls', () => {
    render(
      <Carousel>
        <CarouselContent>
          <CarouselItem>Slide 1</CarouselItem>
          <CarouselItem>Slide 2</CarouselItem>
        </CarouselContent>
        <CarouselPrevious />
        <CarouselNext />
      </Carousel>
    );

    expect(screen.getByText('Slide 1')).toBeInTheDocument();
    expect(screen.getByText('Slide 2')).toBeInTheDocument();
  });
});

