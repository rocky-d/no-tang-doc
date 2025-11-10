import { render } from '@testing-library/react';
import React from 'react';
import { Slider } from '@/components/ui/slider';

describe('Slider', () => {
  it('renders track, range and thumbs', () => {
    const { container } = render(<Slider defaultValue={[30]} min={0} max={100} />);
    expect(container.querySelector('[data-slot="slider-track"]')).toBeInTheDocument();
    expect(container.querySelector('[data-slot="slider-range"]')).toBeInTheDocument();
    expect(container.querySelectorAll('[data-slot="slider-thumb"]').length).toBe(1);
  });
});

