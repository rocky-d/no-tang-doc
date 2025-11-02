import { useEffect, useState } from 'react';
import { DashboardOverview } from './DashboardOverview';
import { getAllDocuments } from '../utils/documentApi';

interface Document {
  id: string;
  name: string;
  type: string;
  size: string;
  uploadDate: string;
  category: string;
  tags: string[];
}

interface DashboardProps {
  onNavigateHome?: () => void;
  onNavigateToUpload?: () => void;
}

export function Dashboard(_: Readonly<DashboardProps>) {
  const [documents, setDocuments] = useState<Document[]>([]);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const docs = await getAllDocuments();
        if (!mounted) return;
        setDocuments(docs);
      } catch (e) {
        console.error('Failed to load documents for dashboard', e);
        if (mounted) setDocuments([]);
      }
    })();
    return () => { mounted = false; };
  }, []);

  return (
      <DashboardOverview documents={documents} />
  );
}