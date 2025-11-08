import { render } from '@testing-library/react';
import React from 'react';
import { vi } from 'vitest';
import { ChartContainer } from '@/components/ui/chart';

vi.mock('recharts', () => ({
  ResponsiveContainer: ({ children }: any) => <div data-mock="rc">{children}</div>,
  Tooltip: (props: any) => <div data-mock="tooltip" {...props} />,
  Legend: (props: any) => <div data-mock="legend" {...props} />,
}));

describe('Chart', () => {
  it('injects CSS variables for config colors', () => {
    const config = { series1: { color: 'red', label: 'Series 1' } };
    const { container } = render(
      <ChartContainer id="t" config={config}>
        <div>chart</div>
      </ChartContainer>
    );
    const style = container.querySelector('style') as HTMLStyleElement;
    expect(style.textContent).toContain('--color-series1: red');
  });
});

