import { useState } from 'react';
import { FileText, Download, MoreVertical, Trash2, Sparkles, Loader2 } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { Button } from './ui/button';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from './ui/dropdown-menu';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { toast } from 'sonner';
import { http } from '../utils/request';

interface AppDocument {
  id: string;
  name: string;
  type: string;
  size: string;
  uploadDate: string;
  category: string;
  tags: string[];
}

interface DocumentsListProps {
  documents: AppDocument[];
  searchTerm: string;
  searchMode?: 'simple' | 'advanced';
  isSearching?: boolean;
}

const DOCS_API_PREFIX = (import.meta.env as any).VITE_DOCS_API_PREFIX || 'http://localhost:8070/api/v1/documents';

export function DocumentsList({ documents, searchTerm, searchMode = 'simple', isSearching = false }: DocumentsListProps) {
  const [downloadingId, setDownloadingId] = useState<string | null>(null);

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

  const handleDownload = async (doc: AppDocument) => {
    // Predict preview type from name first to decide whether to pre-open a tab
    const nameLower = (doc.name || '').toLowerCase();
    const likelyPreviewByName = /\.(pdf|png|jpe?g|gif|webp|bmp|svg|tiff?)$/i.test(nameLower);
    const preOpenedTab = likelyPreviewByName ? window.open('', '_blank', 'noopener') : null;

    try {
      setDownloadingId(doc.id);
      // Call backend to get pre-signed download URL
      const resp: any = await http.get(`${DOCS_API_PREFIX}/download/${doc.id}`);
      const data = resp?.data ?? resp; // support ApiResponse wrapper or plain
      const d = data?.data ?? data;    // unwrap ApiResponse.data
      const url: string | undefined = d?.url || d?.downloadUrl || data?.url || data?.downloadUrl;
      const fileName: string | undefined = d?.fileName || doc.name;
      if (!url) throw new Error('download url not found');

      const nameOrUrl = (fileName || url).toLowerCase();
      const isPreviewType = /\.(pdf|png|jpe?g|gif|webp|bmp|svg|tiff?)($|\?)/i.test(nameOrUrl);

      if (isPreviewType) {
        // Use pre-opened tab if available to avoid popup blockers
        if (preOpenedTab && !preOpenedTab.closed) {
          preOpenedTab.location.href = url;
        } else {
          window.open(url, '_blank', 'noopener');
        }
        toast.success(`已在新标签页打开：${fileName || doc.name}`);
      } else {
        // Prefer blob download to avoid opening in current tab (works when CORS permits)
        let blobDownloaded = false;
        try {
          const res = await fetch(url, { credentials: 'omit' });
          if (!res.ok) throw new Error(`HTTP ${res.status}`);
          const blob = await res.blob();
          const objectUrl = URL.createObjectURL(blob);
          const a = window.document.createElement('a');
          a.href = objectUrl;
          if (fileName) a.download = fileName;
          a.style.display = 'none';
          window.document.body.appendChild(a);
          a.click();
          window.document.body.removeChild(a);
          setTimeout(() => URL.revokeObjectURL(objectUrl), 1000);
          blobDownloaded = true;
        } catch (blobErr) {
          console.warn('Blob download failed, falling back to direct link', blobErr);
        }

        // Fallback: direct link with download attribute (may be ignored by some browsers on cross-origin without Content-Disposition)
        if (!blobDownloaded) {
          const a = window.document.createElement('a');
          a.href = url;
          if (fileName) a.download = fileName;
          a.style.display = 'none';
          window.document.body.appendChild(a);
          a.click();
          window.document.body.removeChild(a);
        }
        toast.success(`开始下载：${fileName || doc.name}`);
      }
    } catch (e: any) {
      // Close pre-opened tab on failure
      if (preOpenedTab && !preOpenedTab.closed) {
        preOpenedTab.close();
      }
      console.error('Download failed', e);
      toast.error(`下载失败：${e?.message || '未知错误'}`);
    } finally {
      setDownloadingId(null);
    }
  };

  const handleDelete = (documentId: string) => {
    // Mock delete functionality
    console.log('Deleting document:', documentId);
  };

  const getFileIcon = (_type: string) => {
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
                      {/* <TableHead>Tags</TableHead> */}
                      <TableHead>Size</TableHead>
                      <TableHead>Upload Date</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredDocuments.map((docItem) => (
                        <TableRow key={docItem.id} className="hover:bg-muted/50">
                          <TableCell>
                            <div className="flex items-center space-x-3">
                              <div className="w-8 h-8 bg-primary/10 rounded-lg flex items-center justify-center">
                                {getFileIcon(docItem.type)}
                              </div>
                              <div>
                                <p className="font-medium">{docItem.name}</p>
                              </div>
                            </div>
                          </TableCell>
                          <TableCell>
                            <Badge variant="secondary">{docItem.type}</Badge>
                          </TableCell>
                          {/*
                    <TableCell>
                      <div className="flex flex-wrap gap-1 max-w-xs">
                        {docItem.tags.slice(0, 3).map((tag, index) => (
                          <Badge
                            key={`${tag}-${index}`}
                            variant="outline"
                            className="text-xs px-2 py-0"
                          >
                            {tag}
                          </Badge>
                        ))}
                        {docItem.tags.length > 3 && (
                          <Badge
                            variant="secondary"
                            className="text-xs px-2 py-0"
                          >
                            +{docItem.tags.length - 3}
                          </Badge>
                        )}
                      </div>
                    </TableCell>
                    */}
                          <TableCell className="text-muted-foreground">
                            {docItem.size}
                          </TableCell>
                          <TableCell className="text-muted-foreground">
                            {formatDate(docItem.uploadDate)}
                          </TableCell>
                          <TableCell className="text-right">
                            <DropdownMenu>
                              <DropdownMenuTrigger asChild>
                                <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                                  <MoreVertical className="w-4 h-4" />
                                </Button>
                              </DropdownMenuTrigger>
                              <DropdownMenuContent align="end">
                                <DropdownMenuItem onClick={() => handleDownload(docItem)} disabled={downloadingId === docItem.id}>
                                  {downloadingId === docItem.id ? (
                                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                                  ) : (
                                      <Download className="w-4 h-4 mr-2" />
                                  )}
                                  Download
                                </DropdownMenuItem>
                                <DropdownMenuItem
                                    onClick={() => handleDelete(docItem.id)}
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