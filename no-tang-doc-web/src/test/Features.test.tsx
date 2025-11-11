import { render, screen } from '@testing-library/react';
import { Features } from '@/components/Features';
import { describe, it, expect } from 'vitest';

describe('Features', () => {
  it('renders main heading', () => {
    render(<Features />);
    expect(screen.getByText('Powerful Features')).toBeInTheDocument();
  });

  it('renders description text', () => {
    render(<Features />);
    expect(screen.getByText(/Everything you need to manage your documents efficiently/i)).toBeInTheDocument();
  });

  it('renders all 6 feature cards', () => {
    render(<Features />);

    expect(screen.getByText('Easy Upload')).toBeInTheDocument();
    expect(screen.getByText('Smart Search')).toBeInTheDocument();
    expect(screen.getByText('Secure Sharing')).toBeInTheDocument();
    expect(screen.getByText('Data Security')).toBeInTheDocument();
    expect(screen.getByText('Cloud Storage')).toBeInTheDocument();
    expect(screen.getByText('Mobile Ready')).toBeInTheDocument();
  });

  it('renders feature descriptions', () => {
    render(<Features />);

    expect(screen.getByText(/Drag and drop files or browse to upload/i)).toBeInTheDocument();
    expect(screen.getByText(/Find your documents quickly with powerful search/i)).toBeInTheDocument();
    expect(screen.getByText(/Enterprise-grade encryption/i)).toBeInTheDocument();
  });

  it('renders collaboration section heading', () => {
    render(<Features />);
    expect(screen.getByText('Collaborate Seamlessly')).toBeInTheDocument();
  });

  it('renders collaboration description', () => {
    render(<Features />);
    expect(screen.getByText(/Work together on documents with real-time collaboration/i)).toBeInTheDocument();
  });

  it('renders Team Workspaces feature', () => {
    render(<Features />);
    expect(screen.getByText('Team Workspaces')).toBeInTheDocument();
    expect(screen.getByText(/Create shared spaces for different projects/i)).toBeInTheDocument();
  });

  it('renders Version Control feature', () => {
    render(<Features />);
    expect(screen.getByText('Version Control')).toBeInTheDocument();
    expect(screen.getByText(/Track document versions and changes/i)).toBeInTheDocument();
  });

  it('renders collaboration image', () => {
    render(<Features />);
    const image = screen.getByAltText('Team collaboration');
    expect(image).toBeInTheDocument();
  });

  it('has correct section structure', () => {
    const { container } = render(<Features />);
    const section = container.querySelector('section#features');
    expect(section).toBeInTheDocument();
  });
});

