import React from 'react';
import { ArrowLeft, FileText } from 'lucide-react';
import { Button } from './ui/button';
import { DocumentUpload } from './DocumentUpload';

interface UploadPageProps {
  onBack: () => void;
  onNavigateHome?: () => void;
}

export function UploadDocument({ onBack, onNavigateHome }: UploadPageProps) {
  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b bg-card">
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <Button variant="ghost" onClick={onBack} className="flex items-center space-x-2">
              <ArrowLeft className="w-4 h-4" />
              <span>Back to Dashboard</span>
            </Button>
          </div>
          
          <button 
            className="flex items-center space-x-2 hover:opacity-80 transition-opacity"
            onClick={onNavigateHome}
          >
            <FileText className="h-6 w-6 text-primary" />
            <span className="font-semibold">DocRepo</span>
          </button>
          
          <div className="w-32"></div> {/* Spacer for center alignment */}
        </div>
      </header>

      {/* Upload Content */}
      <main className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <DocumentUpload />
        </div>
      </main>
    </div>
  );
}