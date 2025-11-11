import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { Textarea } from '../components/ui/textarea';
import userEvent from '@testing-library/user-event';

describe('Textarea', () => {
  it('renders correctly', () => {
    render(<Textarea placeholder="Enter text" />);
    expect(screen.getByPlaceholderText('Enter text')).toBeInTheDocument();
  });

  it('accepts user input', async () => {
    const user = userEvent.setup();
    render(<Textarea placeholder="Type here" />);
    const textarea = screen.getByPlaceholderText('Type here');
    await user.type(textarea, 'Hello World');
    expect(textarea).toHaveValue('Hello World');
  });

  it('handles onChange event', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();
    render(<Textarea placeholder="Type here" onChange={handleChange} />);
    const textarea = screen.getByPlaceholderText('Type here');
    await user.type(textarea, 'Test');
    expect(handleChange).toHaveBeenCalled();
  });

  it('can be disabled', () => {
    render(<Textarea placeholder="Disabled" disabled />);
    const textarea = screen.getByPlaceholderText('Disabled');
    expect(textarea).toBeDisabled();
  });

  it('applies custom className', () => {
    render(<Textarea placeholder="Custom" className="custom-textarea" />);
    const textarea = screen.getByPlaceholderText('Custom');
    expect(textarea).toHaveClass('custom-textarea');
  });

  it('renders with default value', () => {
    render(<Textarea placeholder="With value" defaultValue="Initial text" />);
    const textarea = screen.getByPlaceholderText('With value');
    expect(textarea).toHaveValue('Initial text');
  });

  it('can have custom rows', () => {
    render(<Textarea placeholder="Many rows" rows={10} />);
    const textarea = screen.getByPlaceholderText('Many rows');
    expect(textarea).toHaveAttribute('rows', '10');
  });
});

