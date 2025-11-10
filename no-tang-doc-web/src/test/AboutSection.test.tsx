import { render, screen } from '@testing-library/react';
import { AboutSection } from '@/components/AboutSection';
import { describe, it, expect } from 'vitest';

describe('AboutSection', () => {
  it('renders section with correct heading', () => {
    render(<AboutSection />);
    expect(screen.getByText('Meet Our Team')).toBeInTheDocument();
  });

  it('renders team description', () => {
    render(<AboutSection />);
    expect(screen.getByText(/We're a passionate group of engineers/i)).toBeInTheDocument();
  });

  it('renders Our Story badge', () => {
    render(<AboutSection />);
    expect(screen.getByText('Our Story')).toBeInTheDocument();
  });

  it('renders story content heading', () => {
    render(<AboutSection />);
    expect(screen.getByText('Building the Future of Document Management')).toBeInTheDocument();
  });

  it('renders story paragraphs', () => {
    render(<AboutSection />);
    expect(screen.getByText(/Founded in 2025/i)).toBeInTheDocument();
    expect(screen.getByText(/We set out to build a platform/i)).toBeInTheDocument();
    expect(screen.getByText(/Our commitment to innovation/i)).toBeInTheDocument();
  });

  it('renders all team members', () => {
    render(<AboutSection />);

    expect(screen.getByText('Xin Yuchen')).toBeInTheDocument();
    expect(screen.getByText('Zhong Yi')).toBeInTheDocument();
    expect(screen.getByText('Du HanTian')).toBeInTheDocument();
    expect(screen.getByText('Kong Yikai')).toBeInTheDocument();
    expect(screen.getByText('Chen Sirui')).toBeInTheDocument();
    expect(screen.getByText('Song Jinze')).toBeInTheDocument();
  });

  it('renders team member roles', () => {
    render(<AboutSection />);

    expect(screen.getByText('FE & DevSecOps Tech Lead')).toBeInTheDocument();
    expect(screen.getByText('BE Tech Lead & PM')).toBeInTheDocument();
    expect(screen.getByText('AI Tech Lead & FE Developer')).toBeInTheDocument();
    expect(screen.getByText('Senior BE Developer & DBA')).toBeInTheDocument();
  });

  it('renders team member bios', () => {
    render(<AboutSection />);

    expect(screen.getByText(/Work on Gen-AI FE & IaC & CICD/i)).toBeInTheDocument();
    expect(screen.getByText(/Focus on AI Agent & MCP Server Integration/i)).toBeInTheDocument();
    expect(screen.getAllByText(/SpringBoot specialist/i)).toHaveLength(2);
  });

  it('renders team collaboration image', () => {
    render(<AboutSection />);
    const image = screen.getByAltText('Team collaboration');
    expect(image).toBeInTheDocument();
  });

  it('has correct section structure with proper classes', () => {
    const { container } = render(<AboutSection />);
    const section = container.querySelector('section#about');
    expect(section).toBeInTheDocument();
    expect(section).toHaveClass('py-20', 'bg-background');
  });

  it('renders 6 team member cards', () => {
    const { container } = render(<AboutSection />);
    const cards = container.querySelectorAll('[data-slot="card"]');
    expect(cards.length).toBe(6);
  });
});
