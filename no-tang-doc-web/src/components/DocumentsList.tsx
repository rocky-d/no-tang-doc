import React from 'react';
import { FileText, Download, MoreVertical, Trash2, Sparkles, Loader2 } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { Button } from './ui/button';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from './ui/dropdown-menu';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';

interface Document {
  id: string;
  name: string;
  type: string;
  size: string;
  uploadDate: string;
  category: string;
  tags: string[];
}

interface DocumentsListProps {
  documents: Document[];
  searchTerm: string;
  searchMode?: 'simple' | 'advanced';
  isSearching?: boolean;
}

export function DocumentsList({ documents, searchTerm, searchMode = 'simple', isSearching = false }: DocumentsListProps) {
  // For simple search, filter on the client side
  // For advanced search, documents are already filtered by the API
  const filteredDocuments = searchMode === 'simple' 
    ? documents.filter(doc =>
        doc.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        doc.category.toLowerCase().includes(searchTerm.toLowerCase()) ||
        doc.type.toLowerCase().includes(searchTerm.toLowerCase()) ||
        doc.tags.some(tag => tag.toLowerCase().includes(searchTerm.toLowerCase()))
      )
    : documents;

  const handleDownload = (document: Document) => {
    // Mock download functionality
    console.log('Downloading:', document.name);
  };

  const handleDelete = (documentId: string) => {
    // Mock delete functionality
    console.log('Deleting document:', documentId);
  };

  const getFileIcon = (type: string) => {
    // You could expand this to show different icons for different file types
    return <FileText className="w-5 h-5 text-primary" />;
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold">Document Library</h1>
            {searchMode === 'advanced' && searchTerm && (
              <Badge variant="secondary" className="flex items-center gap-1">
                <Sparkles className="w-3 h-3" />
                Advanced Search
              </Badge>
            )}
          </div>
          <p className="text-muted-foreground">
            {isSearching ? (
              <span className="flex items-center gap-2">
                <Loader2 className="w-4 h-4 animate-spin" />
                Searching...
              </span>
            ) : (
              <>
                {filteredDocuments.length} {filteredDocuments.length === 1 ? 'document' : 'documents'}
                {searchTerm && ` matching "${searchTerm}"`}
                {!searchTerm && documents.length > 0 && ` (${documents.length} total)`}
              </>
            )}
          </p>
        </div>
      </div>

      {/* Documents Table */}
      <Card>
        <CardHeader>
          <CardTitle>All Documents</CardTitle>
        </CardHeader>
        <CardContent>
          {isSearching ? (
            <div className="text-center py-12">
              <Loader2 className="w-16 h-16 text-muted-foreground mx-auto mb-4 animate-spin" />
              <h3 className="text-lg font-medium mb-2">
                Searching documents...
              </h3>
              <p className="text-muted-foreground">
                Using advanced AI-powered search
              </p>
            </div>
          ) : filteredDocuments.length === 0 ? (
            <div className="text-center py-12">
              <FileText className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-medium mb-2">
                {searchTerm ? 'No documents found' : 'No documents uploaded yet'}
              </h3>
              <p className="text-muted-foreground">
                {searchTerm 
                  ? searchMode === 'advanced'
                    ? 'Try different search terms or switch to simple search.'
                    : 'Try adjusting your search terms or upload new documents.' 
                  : 'Upload your first document to get started.'
                }
              </p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Tags</TableHead>
                  <TableHead>Size</TableHead>
                  <TableHead>Upload Date</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredDocuments.map((document) => (
                  <TableRow key={document.id} className="hover:bg-muted/50">
                    <TableCell>
                      <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 bg-primary/10 rounded-lg flex items-center justify-center">
                          {getFileIcon(document.type)}
                        </div>
                        <div>
                          <p className="font-medium">{document.name}</p>
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant="secondary">{document.type}</Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1 max-w-xs">
                        {document.tags.slice(0, 3).map((tag, index) => (
                          <Badge 
                            key={index} 
                            variant="outline" 
                            className="text-xs px-2 py-0"
                          >
                            {tag}
                          </Badge>
                        ))}
                        {document.tags.length > 3 && (
                          <Badge 
                            variant="secondary" 
                            className="text-xs px-2 py-0"
                          >
                            +{document.tags.length - 3}
                          </Badge>
                        )}
                      </div>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {document.size}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formatDate(document.uploadDate)}
                    </TableCell>
                    <TableCell className="text-right">
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                            <MoreVertical className="w-4 h-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => handleDownload(document)}>
                            <Download className="w-4 h-4 mr-2" />
                            Download
                          </DropdownMenuItem>
                          <DropdownMenuItem 
                            onClick={() => handleDelete(document.id)}
                            className="text-destructive"
                          >
                            <Trash2 className="w-4 h-4 mr-2" />
                            Delete
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}