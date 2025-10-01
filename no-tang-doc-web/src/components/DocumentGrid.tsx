import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Badge } from "./ui/badge";
import { 
  Search, 
  Filter, 
  Download, 
  Share2, 
  MoreVertical,
  FileText,
  File,
  Image,
  Video
} from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";

interface Document {
  id: string;
  name: string;
  type: string;
  size: number;
  uploadedAt: string;
  category: string;
}

const mockDocuments: Document[] = [
  {
    id: '1',
    name: 'Project Proposal.pdf',
    type: 'pdf',
    size: 2450000,
    uploadedAt: '2024-03-15',
    category: 'Business'
  },
  {
    id: '2',
    name: 'Team Meeting Notes.docx',
    type: 'docx',
    size: 890000,
    uploadedAt: '2024-03-14',
    category: 'Meeting'
  },
  {
    id: '3',
    name: 'Budget Analysis.xlsx',
    type: 'xlsx',
    size: 1200000,
    uploadedAt: '2024-03-13',
    category: 'Finance'
  },
  {
    id: '4',
    name: 'Marketing Strategy.pptx',
    type: 'pptx',
    size: 5600000,
    uploadedAt: '2024-03-12',
    category: 'Marketing'
  },
  {
    id: '5',
    name: 'User Research.pdf',
    type: 'pdf',
    size: 3200000,
    uploadedAt: '2024-03-11',
    category: 'Research'
  },
  {
    id: '6',
    name: 'Technical Specification.md',
    type: 'md',
    size: 150000,
    uploadedAt: '2024-03-10',
    category: 'Technical'
  }
];

export function DocumentGrid() {
  const [searchTerm, setSearchTerm] = useState('');
  const [documents] = useState<Document[]>(mockDocuments);

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const getFileIcon = (type: string) => {
    switch (type.toLowerCase()) {
      case 'pdf':
      case 'doc':
      case 'docx':
      case 'txt':
      case 'md':
        return FileText;
      case 'jpg':
      case 'jpeg':
      case 'png':
      case 'gif':
        return Image;
      case 'mp4':
      case 'avi':
      case 'mov':
        return Video;
      default:
        return File;
    }
  };

  const filteredDocuments = documents.filter(doc =>
    doc.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    doc.category.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  return (
    <section id="documents" className="py-20 bg-muted/30">
      <div className="container mx-auto px-4">
        <div className="text-center space-y-4 mb-12">
          <h2 className="text-3xl lg:text-4xl font-bold">Your Documents</h2>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            Manage and organize all your uploaded documents in one place.
          </p>
        </div>

        <div className="max-w-6xl mx-auto">
          <div className="flex flex-col sm:flex-row gap-4 mb-8">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search documents..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
            <Button variant="outline">
              <Filter className="mr-2 h-4 w-4" />
              Filter
            </Button>
          </div>

          {filteredDocuments.length === 0 ? (
            <Card className="text-center p-12">
              <CardContent>
                <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium mb-2">No documents found</h3>
                <p className="text-muted-foreground">
                  {searchTerm ? 'Try adjusting your search terms.' : 'Upload your first document to get started.'}
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredDocuments.map((document) => {
                const FileIcon = getFileIcon(document.type);
                return (
                  <Card key={document.id} className="hover:shadow-md transition-shadow">
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between">
                        <div className="flex items-center space-x-3">
                          <div className="w-10 h-10 bg-primary/10 rounded-lg flex items-center justify-center">
                            <FileIcon className="h-5 w-5 text-primary" />
                          </div>
                          <div className="flex-1 min-w-0">
                            <CardTitle className="text-base font-medium truncate">
                              {document.name}
                            </CardTitle>
                            <CardDescription className="text-xs">
                              {formatFileSize(document.size)}
                            </CardDescription>
                          </div>
                        </div>
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="sm">
                              <MoreVertical className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem>
                              <Download className="mr-2 h-4 w-4" />
                              Download
                            </DropdownMenuItem>
                            <DropdownMenuItem>
                              <Share2 className="mr-2 h-4 w-4" />
                              Share
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </div>
                    </CardHeader>
                    <CardContent className="pt-0">
                      <div className="flex items-center justify-between">
                        <Badge variant="secondary" className="text-xs">
                          {document.category}
                        </Badge>
                        <span className="text-xs text-muted-foreground">
                          {formatDate(document.uploadedAt)}
                        </span>
                      </div>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </section>
  );
}