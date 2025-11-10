import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import {
  Collapsible,
  CollapsibleTrigger,
  CollapsibleContent,
} from '../components/ui/collapsible';

// Mock animation frame to avoid issues in test environment
vi.spyOn(window, 'requestAnimationFrame').mockImplementation((cb: FrameRequestCallback) => {
  cb(0);
  return 0;
});

describe('Collapsible', () => {
  it('renders trigger and handles content visibility', async () => {
    render(
      <Collapsible>
        <CollapsibleTrigger>Open</CollapsibleTrigger>
        <CollapsibleContent>
          <div>Content</div>
        </CollapsibleContent>
      </Collapsible>
    );
    expect(screen.getByText('Open')).toBeInTheDocument();
    // Content is not in the document by default
    expect(screen.queryByText('Content')).toBeNull();

    // Click to open
    fireEvent.click(screen.getByText('Open'));
    const content = await screen.findByText('Content');
    expect(content).toBeInTheDocument();
  });

  it('toggles content visibility on trigger click', async () => {
    render(
      <Collapsible>
        <CollapsibleTrigger>Open</CollapsibleTrigger>
        <CollapsibleContent>
          <div>Collapsible Content</div>
        </CollapsibleContent>
      </Collapsible>
    );

    const trigger = screen.getByText('Open');
    // Content should not be in the document initially
    expect(screen.queryByText('Collapsible Content')).toBeNull();

    // Click to open
    fireEvent.click(trigger);
    const content = await screen.findByText('Collapsible Content');
    expect(content).toBeInTheDocument();
    expect(content).toBeVisible();

    // Click to close
    fireEvent.click(trigger);
    // We wait for the element to be removed from the DOM
    await vi.waitFor(() => {
      expect(screen.queryByText('Collapsible Content')).toBeNull();
    });
  });

  it('can be open by default', () => {
    render(
      <Collapsible defaultOpen>
        <CollapsibleTrigger>Toggle</CollapsibleTrigger>
        <CollapsibleContent>
          <div>Initially Visible</div>
        </CollapsibleContent>
      </Collapsible>
    );
    expect(screen.getByText('Initially Visible')).toBeVisible();
  });

  it('can be controlled', () => {
    const { rerender } = render(
      <Collapsible open={false}>
        <CollapsibleTrigger>Toggle</CollapsibleTrigger>
        <CollapsibleContent>
          <div>Controlled Content</div>
        </CollapsibleContent>
      </Collapsible>
    );
    expect(screen.queryByText('Controlled Content')).toBeNull();

    rerender(
      <Collapsible open={true}>
        <CollapsibleTrigger>Toggle</CollapsibleTrigger>
        <CollapsibleContent>
          <div>Controlled Content</div>
        </CollapsibleContent>
      </Collapsible>
    );
    expect(screen.getByText('Controlled Content')).toBeVisible();
  });
});
