import { render, screen } from '@testing-library/react';
import { DocumentsList } from '../components/DocumentsList';

describe('DocumentsList - tags rendering', () => {
  it('renders provided tags instead of "No tags"', async () => {
    const docs = [
      {
        id: '1',
        name: 'SamplePapersWithAppendix.pdf',
        type: 'pdf',
        size: '3.8 MB',
        uploadDate: '2025-11-10T04:20:18Z',
        category: 'ACTIVE',
        tags: ['Test', 'Java', 'New'],
      },
    ];

    render(
      <DocumentsList
        documents={docs as any}
        searchTerm=""
        searchMode="simple"
        isSearching={false}
      />
    );

    // Assert tags are visible
    expect(await screen.findByText('Test')).toBeInTheDocument();
    expect(screen.getByText('Java')).toBeInTheDocument();
    expect(screen.getByText('New')).toBeInTheDocument();

    // Ensure the fallback "No tags" is not shown
    const noTagsFallback = screen.queryByText(/No tags/i);
    expect(noTagsFallback).toBeNull();
  });
});

