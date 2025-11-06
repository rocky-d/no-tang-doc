import { render, waitFor } from '@testing-library/react';
import React from 'react';
import { AlertDialog, AlertDialogTrigger, AlertDialogContent, AlertDialogTitle, AlertDialogDescription } from '@/components/ui/alert-dialog';

describe('AlertDialog', () => {
  it('renders content when open', async () => {
    render(
      <AlertDialog open>
        <AlertDialogTrigger>Open</AlertDialogTrigger>
        <AlertDialogContent>
          <AlertDialogTitle>Title</AlertDialogTitle>
          <AlertDialogDescription>Desc</AlertDialogDescription>
        </AlertDialogContent>
      </AlertDialog>
    );

    await waitFor(() => {
      const content = document.querySelector('[data-slot="alert-dialog-content"]');
      const title = document.querySelector('[data-slot="alert-dialog-title"]');
      const desc = document.querySelector('[data-slot="alert-dialog-description"]');
      expect(content).toBeInTheDocument();
      expect(title).toHaveTextContent('Title');
      expect(desc).toHaveTextContent('Desc');
    });
  });
});
