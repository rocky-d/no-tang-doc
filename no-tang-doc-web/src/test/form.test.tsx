import { render, screen, waitFor } from '@testing-library/react';
import React, { useEffect } from 'react';
import { Form, FormField, FormItem, FormLabel, FormControl, FormDescription, FormMessage } from '@/components/ui/form';
import { useForm } from 'react-hook-form';

function TestForm() {
  const form = useForm<{ email: string }>({ defaultValues: { email: '' }, mode: 'onChange' });

  useEffect(() => {
    void form.trigger('email');
  }, [form]);

  return (
    <Form {...form}>
      <form>
        <FormField
          control={form.control}
          name="email"
          rules={{ required: 'Email is required' }}
          render={({ field }) => (
            <FormItem>
              <FormLabel>Email</FormLabel>
              <FormDescription>We will never share it.</FormDescription>
              <FormControl>
                <input placeholder="email" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
      </form>
    </Form>
  );
}

describe('Form', () => {
  it('shows error state and message', async () => {
    render(<TestForm />);
    await waitFor(() => {
      const label = screen.getByText('Email');
      expect(label).toHaveAttribute('data-slot', 'form-label');
      expect(label).toHaveAttribute('data-error', 'true');
      expect(screen.getByText('Email is required')).toHaveAttribute('data-slot', 'form-message');
    });
  });
});
