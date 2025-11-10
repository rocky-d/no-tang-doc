import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AIAgentSection } from '@/components/AiAgentSection';
import { describe, it, expect } from 'vitest';

describe('AIAgentSection', () => {
  it('renders AI-Powered badge', () => {
    render(<AIAgentSection />);
    expect(screen.getByText('AI-Powered')).toBeInTheDocument();
  });

  it('renders main heading', () => {
    render(<AIAgentSection />);
    expect(screen.getByText('Intelligent AI Agent')).toBeInTheDocument();
  });

  it('renders description text', () => {
    render(<AIAgentSection />);
    expect(screen.getByText(/Experience the future of document management/i)).toBeInTheDocument();
  });

  it('renders NTDoc AI Assistant header', () => {
    render(<AIAgentSection />);
    expect(screen.getByText('NTDoc AI Assistant')).toBeInTheDocument();
    expect(screen.getByText('Powered by Claude')).toBeInTheDocument();
  });

  it('renders chat conversation example', () => {
    render(<AIAgentSection />);

    expect(screen.getByText('Can you summarize the Q4 financial report?')).toBeInTheDocument();
    expect(screen.getByText(/I've analyzed the Q4 financial report/i)).toBeInTheDocument();
    expect(screen.getByText('What were the main cost savings?')).toBeInTheDocument();
  });

  it('renders chat summary points', () => {
    render(<AIAgentSection />);

    expect(screen.getByText(/Revenue increased by 23%/i)).toBeInTheDocument();
    expect(screen.getByText(/Operating costs down 12%/i)).toBeInTheDocument();
    expect(screen.getByText(/Net profit: \$2.4M/i)).toBeInTheDocument();
  });

  it('renders chat input placeholder', () => {
    render(<AIAgentSection />);
    expect(screen.getByText('Ask me anything about your documents...')).toBeInTheDocument();
  });

  it('renders "Chat with Your Documents" section', () => {
    render(<AIAgentSection />);
    expect(screen.getByText('Chat with Your Documents')).toBeInTheDocument();
    expect(screen.getByText(/Our AI agent understands context/i)).toBeInTheDocument();
  });

  it('renders feature cards in sidebar', () => {
    render(<AIAgentSection />);

    expect(screen.getAllByText('Claude AI Model')).toHaveLength(2); // One in card, one in sidebar
    expect(screen.getByText('MCP Server Protocol')).toBeInTheDocument();
    expect(screen.getByText('Context-Aware Search')).toBeInTheDocument();
  });

  it('renders all 4 AI features', () => {
    render(<AIAgentSection />);

    expect(screen.getAllByText('Claude AI Model')).toHaveLength(2); // One in card, one in sidebar
    expect(screen.getByText('Intelligent Conversations')).toBeInTheDocument();
    expect(screen.getByText('Smart Document Analysis')).toBeInTheDocument();
    expect(screen.getByText('MCP Server Integration')).toBeInTheDocument();
  });

  it('renders feature descriptions', () => {
    render(<AIAgentSection />);

    expect(screen.getByText(/Powered by Anthropic's Claude/i)).toBeInTheDocument();
    expect(screen.getByText(/Ask questions about your documents/i)).toBeInTheDocument();
    expect(screen.getByText(/Automatically extract key information/i)).toBeInTheDocument();
  });

  it('renders Try AI Agent button', () => {
    render(<AIAgentSection />);
    const button = screen.getByRole('button', { name: /Try AI Agent/i });
    expect(button).toBeInTheDocument();
  });

  it('Try AI Agent button is clickable', async () => {
    const user = userEvent.setup();
    render(<AIAgentSection />);

    const button = screen.getByRole('button', { name: /Try AI Agent/i });
    await user.click(button);
    expect(button).toBeInTheDocument();
  });

  it('has correct section structure', () => {
    const { container } = render(<AIAgentSection />);
    const section = container.querySelector('section#agent');
    expect(section).toBeInTheDocument();
  });
});
