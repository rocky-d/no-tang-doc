import { render, screen } from '@testing-library/react';
import { Footer } from '@/components/Footer';
import { describe, it, expect } from 'vitest';

describe('Footer', () => {
  it('renders NTDoc logo and name', () => {
    render(<Footer />);
    expect(screen.getByText('NTDoc')).toBeInTheDocument();
  });

  it('renders description text', () => {
    render(<Footer />);
    expect(screen.getByText(/Your secure digital document repository/i)).toBeInTheDocument();
  });

  it('renders Product section', () => {
    render(<Footer />);
    expect(screen.getByText('Product')).toBeInTheDocument();
    expect(screen.getByText('Features')).toBeInTheDocument();
    expect(screen.getByText('Performance')).toBeInTheDocument();
    expect(screen.getByText('AI Agent')).toBeInTheDocument();
    expect(screen.getByText('API')).toBeInTheDocument();
  });

  it('renders Support section', () => {
    render(<Footer />);
    expect(screen.getByText('Support')).toBeInTheDocument();
    expect(screen.getByText('Contact Us')).toBeInTheDocument();
  });

  it('renders Company section', () => {
    render(<Footer />);
    expect(screen.getByText('Company')).toBeInTheDocument();
    expect(screen.getByText('About')).toBeInTheDocument();
  });

  it('renders copyright text', () => {
    render(<Footer />);
    expect(screen.getByText(/© 2025 NTDoc/i)).toBeInTheDocument();
  });

  it('renders GitHub link', () => {
    const { container } = render(<Footer />);
    const githubLink = container.querySelector('a[href="https://github.com/rocky-d/no-tang-doc"]');
    expect(githubLink).toBeInTheDocument();
    expect(githubLink).toHaveAttribute('href', 'https://github.com/rocky-d/no-tang-doc');
  });

  it('renders footer with correct structure', () => {
    const { container } = render(<Footer />);
    const footer = container.querySelector('footer');
    expect(footer).toBeInTheDocument();
    expect(footer).toHaveClass('bg-muted/50', 'border-t');
  });
});
