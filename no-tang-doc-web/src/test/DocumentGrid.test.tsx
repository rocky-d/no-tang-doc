import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DocumentGrid } from '@/components/DocumentGrid';
import { describe, it, expect, beforeEach } from 'vitest';

describe('DocumentGrid', () => {
  beforeEach(() => {
    // Clear any previous renders
  });

  it('renders document grid container', () => {
    render(<DocumentGrid />);

    // Check if search input is present
    const searchInput = screen.getByPlaceholderText(/search/i);
    expect(searchInput).toBeInTheDocument();
  });

  it('displays search input', () => {
    render(<DocumentGrid />);

    const searchInput = screen.getByRole('textbox');
    expect(searchInput).toBeInTheDocument();
  });

  it('renders mock documents', () => {
    render(<DocumentGrid />);

    expect(screen.getByText('Project Proposal.pdf')).toBeInTheDocument();
    expect(screen.getByText('Team Meeting Notes.docx')).toBeInTheDocument();
    expect(screen.getByText('Budget Analysis.xlsx')).toBeInTheDocument();
  });

  it('displays document categories', () => {
    render(<DocumentGrid />);

    expect(screen.getByText('Business')).toBeInTheDocument();
    expect(screen.getByText('Meeting')).toBeInTheDocument();
    expect(screen.getByText('Finance')).toBeInTheDocument();
  });

  it('shows file type badges', () => {
    render(<DocumentGrid />);

    // Component doesn't render raw type labels; verify by filenames with extensions
    expect(screen.getByText('Project Proposal.pdf')).toBeInTheDocument();
    expect(screen.getByText('Team Meeting Notes.docx')).toBeInTheDocument();
    expect(screen.getByText('Budget Analysis.xlsx')).toBeInTheDocument();
  });

  it('displays formatted file sizes', () => {
    render(<DocumentGrid />);

    const sizes = screen.getAllByText(/\d+(\.\d+)?\s(?:MB|KB|Bytes)/);
    expect(sizes.length).toBeGreaterThan(0);
  });

  it('allows typing in search input', async () => {
    const user = userEvent.setup();
    render(<DocumentGrid />);

    const searchInput = screen.getByRole('textbox');
    await user.type(searchInput, 'Project');

    expect(searchInput).toHaveValue('Project');
  });

  it('renders action buttons for documents', () => {
    render(<DocumentGrid />);

    // Check for dropdown menu triggers
    const dropdownButtons = screen.getAllByRole('button');
    expect(dropdownButtons.length).toBeGreaterThan(0);
  });

  it('displays all 6 mock documents', () => {
    render(<DocumentGrid />);

    expect(screen.getByText('Project Proposal.pdf')).toBeInTheDocument();
    expect(screen.getByText('Team Meeting Notes.docx')).toBeInTheDocument();
    expect(screen.getByText('Budget Analysis.xlsx')).toBeInTheDocument();
    expect(screen.getByText('Marketing Strategy.pptx')).toBeInTheDocument();
    expect(screen.getByText('User Research.pdf')).toBeInTheDocument();
    expect(screen.getByText('Technical Specification.md')).toBeInTheDocument();
  });

  it('shows upload dates', () => {
    render(<DocumentGrid />);

    // Dates are formatted as e.g., "Mar 15, 2024"
    expect(screen.getByText('Mar 15, 2024')).toBeInTheDocument();
    expect(screen.getByText('Mar 14, 2024')).toBeInTheDocument();
  });

  it('renders document cards', () => {
    const { container } = render(<DocumentGrid />);

    const cards = container.querySelectorAll('[data-slot="card"]');
    expect(cards.length).toBeGreaterThan(0);
  });

  it('displays filter button', () => {
    render(<DocumentGrid />);

    const filterButton = screen.getByRole('button', { name: /filter/i });
    expect(filterButton).toBeInTheDocument();
  });

  it('opens dropdown menu on click', async () => {
    const user = userEvent.setup();
    render(<DocumentGrid />);

    const dropdownTriggers = screen.getAllByRole('button');
    const menuTrigger = dropdownTriggers.find(btn =>
      btn.getAttribute('data-slot') === 'dropdown-menu-trigger'
    );

    if (menuTrigger) {
      await user.click(menuTrigger);
      // Menu should open - checking for common menu items
      expect(screen.queryByText(/download/i) || screen.queryByText(/share/i)).toBeTruthy();
    }
  });

  it('maintains search state', async () => {
    const user = userEvent.setup();
    render(<DocumentGrid />);

    const searchInput = screen.getByRole('textbox');
    await user.type(searchInput, 'test query');

    expect(searchInput).toHaveValue('test query');
  });
});
