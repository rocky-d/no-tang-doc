import React, { useState } from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SearchDialog } from '@/components/SearchDialog';
import { describe, it, expect, vi, beforeEach } from 'vitest';

vi.mock('sonner', () => ({ toast: { success: vi.fn(), error: vi.fn() } }));

// Helper harness to control open state similar to parent usage
function Harness({ initialMode = 'simple', onSearchMock }: { initialMode?: 'simple' | 'advanced'; onSearchMock: any }) {
  const [open, setOpen] = useState(true);
  return (
    <>
      <button onClick={() => setOpen(true)}>Reopen</button>
      <SearchDialog
        open={open}
        onOpenChange={setOpen}
        onSearch={onSearchMock}
        initialMode={initialMode}
      />
    </>
  );
}

describe('SearchDialog', () => {
  const STORAGE_KEY = 'doc-repo-search-history';
  const onSearch = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    onSearch.mockReset();
    localStorage.clear();
  });

  it('renders dialog with tabs and search button', () => {
    render(<Harness onSearchMock={onSearch} />);
    expect(screen.getByText('Search Documents')).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /Simple Search/i })).toBeInTheDocument();
    expect(screen.getByRole('tab', { name: /Advanced Search/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /^Search$/ })).toBeDisabled();
  });

  it('allows typing and performs simple search', async () => {
    const user = userEvent.setup();
    render(<Harness onSearchMock={onSearch} />);
    const input = screen.getByPlaceholderText(/filename or tags/i);
    await user.type(input, 'report');
    const searchBtn = screen.getByRole('button', { name: /^Search$/ });
    expect(searchBtn).not.toBeDisabled();
    await user.click(searchBtn);
    await waitFor(() => expect(onSearch).toHaveBeenCalledWith('report', 'simple'));
    // dialog closed
    expect(screen.queryByText('Search Documents')).not.toBeInTheDocument();
  });

  it('switches to advanced tab and searches', async () => {
    const user = userEvent.setup();
    render(<Harness onSearchMock={onSearch} />);
    await user.click(screen.getByRole('tab', { name: /Advanced Search/i }));
    const input = screen.getByPlaceholderText(/AI-powered advanced search/i);
    await user.type(input, 'semantic query');
    await user.click(screen.getByRole('button', { name: /^Search$/ }));
    await waitFor(() => expect(onSearch).toHaveBeenCalledWith('semantic query', 'advanced'));
  });

  it('stores search history and displays recent searches', async () => {
    const user = userEvent.setup();
    render(<Harness onSearchMock={onSearch} />);
    const input = screen.getByPlaceholderText(/filename or tags/i);
    await user.type(input, 'alpha');
    await user.click(screen.getByRole('button', { name: /^Search$/ }));
    // reopen
    await user.click(screen.getByText('Reopen'));
    // second search
    const newInput = screen.getByPlaceholderText(/filename or tags/i);
    await user.type(newInput, 'beta');
    await user.click(screen.getByRole('button', { name: /^Search$/ }));
    await user.click(screen.getByText('Reopen'));
    // history should show both queries
    expect(await screen.findByText('alpha')).toBeInTheDocument();
    expect(screen.getByText('beta')).toBeInTheDocument();
    // history badges show modes
    expect(screen.getAllByText(/simple/i).length).toBeGreaterThan(0);
  });

  it('clicking history item triggers onSearch and closes dialog', async () => {
    const user = userEvent.setup();
    render(<Harness onSearchMock={onSearch} />);
    const input = screen.getByPlaceholderText(/filename or tags/i);
    await user.type(input, 'gamma');
    await user.click(screen.getByRole('button', { name: /^Search$/ }));
    await user.click(screen.getByText('Reopen'));
    const historyItem = await screen.findByText('gamma');
    await user.click(historyItem);
    await waitFor(() => expect(onSearch).toHaveBeenCalledWith('gamma', 'simple'));
    expect(screen.queryByText('Search Documents')).not.toBeInTheDocument();
  });

  it('removes single history item', async () => {
    const user = userEvent.setup();
    render(<Harness onSearchMock={onSearch} />);
    const input = screen.getByPlaceholderText(/filename or tags/i);
    await user.type(input, 'delta');
    await user.click(screen.getByRole('button', { name: /^Search$/ }));
    await user.click(screen.getByText('Reopen'));
    const item = await screen.findByText('delta');
    // find remove button via aria-label on sibling container
    const removeButtons = screen.getAllByRole('button', { name: /Remove history item/i });
    expect(removeButtons.length).toBeGreaterThan(0);
    await user.click(removeButtons[0]);
    expect(screen.queryByText('delta')).not.toBeInTheDocument();
  });

  it('clears all history', async () => {
    const user = userEvent.setup();
    render(<Harness onSearchMock={onSearch} />);
    const input = screen.getByPlaceholderText(/filename or tags/i);
    await user.type(input, 'epsilon');
    await user.click(screen.getByRole('button', { name: /^Search$/ }));
    await user.click(screen.getByText('Reopen'));
    expect(await screen.findByText('epsilon')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: /Clear all/i }));
    expect(screen.queryByText('epsilon')).not.toBeInTheDocument();
    expect(localStorage.getItem(STORAGE_KEY)).toBe(JSON.stringify([]));
  });
});
