import { render, screen } from '@testing-library/react';
import { PerformanceSection } from '@/components/PerformanceSection';
import { describe, it, expect } from 'vitest';

describe('PerformanceSection', () => {
  it('renders main heading', () => {
    render(<PerformanceSection />);
    expect(screen.getByText('Enterprise-Grade Performance')).toBeInTheDocument();
  });

  it('renders description text', () => {
    render(<PerformanceSection />);
    expect(screen.getByText(/Built on cutting-edge cloud infrastructure/i)).toBeInTheDocument();
  });

  it('renders all 6 architecture features', () => {
    render(<PerformanceSection />);

    expect(screen.getByText('Digital Ocean Infrastructure')).toBeInTheDocument();
    expect(screen.getByText('DOKS (Kubernetes)')).toBeInTheDocument();
    expect(screen.getAllByText('App Platform')).toHaveLength(2); // One in card, one in diagram
    expect(screen.getByText('Keycloak Authentication')).toBeInTheDocument();
    expect(screen.getByText('OAuth 2.0 Security')).toBeInTheDocument();
    expect(screen.getByText('Auto-Scaling')).toBeInTheDocument();
  });

  it('renders feature descriptions', () => {
    render(<PerformanceSection />);

    expect(screen.getByText(/Deployed on Digital Ocean's robust infrastructure/i)).toBeInTheDocument();
    expect(screen.getByText(/Leveraging Digital Ocean Kubernetes Service/i)).toBeInTheDocument();
    expect(screen.getByText(/Enterprise-grade authentication and authorization/i)).toBeInTheDocument();
  });

  it('renders architecture section heading', () => {
    render(<PerformanceSection />);
    expect(screen.getByText('Scalable & Secure Architecture')).toBeInTheDocument();
  });

  it('renders Cloud-Native Deployment step', () => {
    render(<PerformanceSection />);
    expect(screen.getByText('Cloud-Native Deployment')).toBeInTheDocument();
    expect(screen.getByText(/Hosted on Digital Ocean with DOKS/i)).toBeInTheDocument();
  });

  it('renders Secure Authentication step', () => {
    render(<PerformanceSection />);
    expect(screen.getByText('Secure Authentication')).toBeInTheDocument();
    expect(screen.getByText(/Keycloak integration with OAuth 2.0/i)).toBeInTheDocument();
  });

  it('renders High Availability step', () => {
    render(<PerformanceSection />);
    expect(screen.getByText('High Availability')).toBeInTheDocument();
    expect(screen.getByText(/Multi-node clusters and automated failover/i)).toBeInTheDocument();
  });

  it('renders architecture diagram labels', () => {
    render(<PerformanceSection />);

    expect(screen.getByText('Digital Ocean Cloud')).toBeInTheDocument();
    // DOKS appears in: card title as "DOKS (Kubernetes)" and diagram as "DOKS"
    expect(screen.getByText('DOKS (Kubernetes)')).toBeInTheDocument(); // Card title
    expect(screen.getAllByText(/DOKS/i)).toHaveLength(3); // Card title, description text, and diagram
    expect(screen.getAllByText('App Platform')).toHaveLength(2); // One in card title, one in diagram
    expect(screen.getByText(/Keycloak \+ OAuth 2\.0/i)).toBeInTheDocument();
    expect(screen.getByText('Frontend')).toBeInTheDocument();
    expect(screen.getByText('Backend')).toBeInTheDocument();
    expect(screen.getByText('Database')).toBeInTheDocument();
  });

  it('has correct section structure', () => {
    const { container } = render(<PerformanceSection />);
    const section = container.querySelector('section#performance');
    expect(section).toBeInTheDocument();
  });
});
