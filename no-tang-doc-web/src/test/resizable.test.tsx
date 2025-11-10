import { render } from '@testing-library/react';
import React from 'react';
import { ResizablePanelGroup, ResizablePanel, ResizableHandle } from '@/components/ui/resizable';

describe('Resizable', () => {
  it('renders group, panel and handle', () => {
    const { container } = render(
      <ResizablePanelGroup direction="horizontal">
        <ResizablePanel defaultSize={50} />
        <ResizableHandle withHandle />
        <ResizablePanel defaultSize={50} />
      </ResizablePanelGroup>
    );
    expect(container.querySelector('[data-slot="resizable-panel-group"]')).toBeInTheDocument();
    expect(container.querySelectorAll('[data-slot="resizable-panel"]').length).toBe(2);
    expect(container.querySelector('[data-slot="resizable-handle"]')).toBeInTheDocument();
  });
});

