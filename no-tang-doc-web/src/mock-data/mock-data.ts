export interface Document {
  id: string;
  name: string;
  type: string;
  size: string;
  uploadDate: string;
  category: string;
  tags: string[];
}

// Mock documents data
export const mockDocuments: Document[] = [
  {
    id: '1',
    name: 'Project Proposal.pdf',
    type: 'PDF',
    size: '2.4 MB',
    uploadDate: '2024-01-15',
    category: 'Work',
    tags: ['Proposal', 'Q1 2024', 'Client', 'Important']
  },
  {
    id: '2',
    name: 'Invoice_2024_001.xlsx',
    type: 'Excel',
    size: '156 KB',
    uploadDate: '2024-01-14',
    category: 'Finance',
    tags: ['Invoice', 'Tax', 'Billing']
  },
  {
    id: '3',
    name: 'Meeting Notes.docx',
    type: 'Word',
    size: '89 KB',
    uploadDate: '2024-01-13',
    category: 'Work',
    tags: ['Meeting', 'Team', 'Action Items']
  },
  {
    id: '4',
    name: 'Design Mockups.fig',
    type: 'Figma',
    size: '5.2 MB',
    uploadDate: '2024-01-12',
    category: 'Design',
    tags: ['UI/UX', 'Mockup', 'Review', 'Version 2', 'Draft']
  },
  {
    id: '5',
    name: 'Tax Documents 2023.pdf',
    type: 'PDF',
    size: '1.8 MB',
    uploadDate: '2024-01-11',
    category: 'Finance',
    tags: ['Tax', 'Annual', '2023', 'Important', 'Archive']
  }
];

// Mock API function for advanced search
export const mockAdvancedSearch = async (query: string): Promise<Document[]> => {
  // Simulate API delay
  await new Promise(resolve => setTimeout(resolve, 500));

  // Mock advanced search logic - in real implementation, this would call your backend API
  // For now, return filtered results based on a more sophisticated search
  if (!query.trim()) {
    return mockDocuments;
  }

  // Simulate AI-powered search that understands context
  const lowerQuery = query.toLowerCase();
  return mockDocuments.filter(doc => {
    // Check if query matches document properties
    const nameMatch = doc.name.toLowerCase().includes(lowerQuery);
    const typeMatch = doc.type.toLowerCase().includes(lowerQuery);
    const categoryMatch = doc.category.toLowerCase().includes(lowerQuery);
    const tagMatch = doc.tags.some(tag => tag.toLowerCase().includes(lowerQuery));

    // Advanced: match partial words, synonyms, etc.
    const yearMatch = lowerQuery.includes('2024') || lowerQuery.includes('2023');
    const hasYear = doc.uploadDate.includes('2024') || doc.uploadDate.includes('2023');

    return nameMatch || typeMatch || categoryMatch || tagMatch || (yearMatch && hasYear);
  });
};
