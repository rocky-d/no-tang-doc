import { useState, useEffect, useCallback, memo } from 'react';
import {
  FileText,
  Download,
  MoreVertical,
  Trash2,
  Sparkles,
  Loader2,
  Share2,
  Copy,
  Check,
  MessageCircle,
  Tag,
  X,
  Calendar,
} from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Badge } from './ui/badge';
import { Button } from './ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from './ui/dialog';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from './ui/dropdown-menu';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from './ui/table';
import { Input } from './ui/input';
import { toast } from 'sonner';
import { getDocumentRepository, type Document as AppDocument } from '../repositories/DocumentRepository';
import { Textarea } from './ui/textarea';
import { Avatar, AvatarFallback } from './ui/avatar';
import { Label } from './ui/label';
import { Separator } from './ui/separator';
import { ScrollArea } from './ui/scroll-area';

interface DocumentsListProps {
  readonly documents: AppDocument[];
  readonly searchTerm: string;
  readonly searchMode?: 'simple' | 'advanced';
  readonly isSearching?: boolean;
}

interface Comment {
  id: string;
  user: string;
  avatar?: string;
  content: string;
  timestamp: string;
}

const formatBytes = (bytes: number): string => {
  if (!isFinite(bytes) || bytes < 0) return '—';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  const value = bytes / Math.pow(1024, i);
  return `${value.toFixed(value >= 100 || i === 0 ? 0 : 1)} ${units[i]}`;
};
const normalizeDoc = (raw: unknown): AppDocument => {
  const r = raw as Record<string, unknown>;
  const id = r.id ?? r.documentId ?? r.docId ?? r.fileId ?? r.uuid;
  const name = r.name ?? r.fileName ?? 'Untitled';
  const mime = r.mimeType as (string | undefined);
  let type = (r.type as string | undefined) ?? (mime ? mime.split('/').pop() : undefined);
  if (!type && typeof name === 'string' && name.includes('.')) type = name.split('.').pop();
  type = (type || 'unknown').toString();
  const sizeNumber = r.size ?? r.fileSize;
  let size: string;
  if (typeof sizeNumber === 'number') {
    size = formatBytes(sizeNumber);
  } else {
    size = sizeNumber ? String(sizeNumber) : '—';
  }
  const uploadDate = r.uploadDate ?? r.uploadTime ?? r.createdAt ?? new Date().toISOString();
  const category = r.category ?? r.status ?? 'default';
  const rawTags = Array.isArray(r.tags) ? (r.tags as unknown[]) : [];
  const tags: string[] = rawTags
    .filter((t: unknown) => typeof t === 'string')
    .map((t: unknown) => (t as string).trim())
    .filter(Boolean);
  return { id: String(id ?? name), name: String(name), type: String(type), size, uploadDate: String(uploadDate), category: String(category), tags };
};
const getInitials = (name: string) => (name || '').split(' ').filter(Boolean).map(w => w[0]).join('').toUpperCase().slice(0, 2);
const formatDate = (dateString: string) => new Date(dateString).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
const getFileIcon = (_type: string) => <FileText className="w-5 h-5 text-primary" />;

// Reusable sub components -------------------------------------------------

interface DocumentRowProps {
  readonly doc: AppDocument;
  readonly onView: (doc: AppDocument) => void;
  readonly onDownload: (doc: AppDocument) => void;
  readonly onShare: (doc: AppDocument) => void;
  readonly onDelete: (id: string) => void;
  readonly downloadingId: string | null;
  readonly deletingId: string | null;
}
const DocumentRow = memo(({ doc, onView, onDownload, onShare, onDelete, downloadingId, deletingId }: DocumentRowProps) => (
  <TableRow key={doc.id} className="hover:bg-muted/50 cursor-pointer" onClick={() => onView(doc)}>
    <TableCell>
      <div className="flex items-center space-x-3">
        <div className="w-8 h-8 bg-primary/10 rounded-lg flex items-center justify-center">{getFileIcon(doc.type)}</div>
        <p className="font-medium line-clamp-1" title={doc.name}>{doc.name}</p>
      </div>
    </TableCell>
    <TableCell><Badge variant="secondary">{doc.type}</Badge></TableCell>
    <TableCell>
      <div className="flex flex-wrap gap-1 max-w-xs min-h-5">
        {doc.tags.length ? (
          <>
            {doc.tags.slice(0, 3).map((tag, i) => (
              <Badge key={tag + i} variant="outline" className="text-xs px-2 py-0">{tag}</Badge>
            ))}
            {doc.tags.length > 3 && <Badge variant="secondary" className="text-xs px-2 py-0">+{doc.tags.length - 3}</Badge>}
          </>
        ) : (
          <span className="text-xs text-muted-foreground italic select-none">No tags</span>
        )}
      </div>
    </TableCell>
    <TableCell className="text-muted-foreground">{doc.size}</TableCell>
    <TableCell className="text-muted-foreground">{formatDate(doc.uploadDate)}</TableCell>
    <TableCell className="text-right">
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="sm" className="h-8 w-8 p-0" aria-label="Actions" data-testid="actions-trigger" onClick={e => e.stopPropagation()}>
            <MoreVertical className="w-4 h-4" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" onClick={e => e.stopPropagation()}>
          <DropdownMenuItem onClick={() => onDownload(doc)} disabled={downloadingId === doc.id}>
            {downloadingId === doc.id ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : <Download className="w-4 h-4 mr-2" />}
            Download
          </DropdownMenuItem>
          <DropdownMenuItem onClick={() => onShare(doc)}>
            <Share2 className="w-4 h-4 mr-2" /> Share
          </DropdownMenuItem>
          <DropdownMenuItem onClick={() => onDelete(doc.id)} disabled={deletingId === doc.id} className="text-destructive">
            {deletingId === doc.id ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : <Trash2 className="w-4 h-4 mr-2" />}
            {deletingId === doc.id ? 'Deleting...' : 'Delete'}
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </TableCell>
  </TableRow>
));
DocumentRow.displayName = 'DocumentRow';

interface ShareDialogProps {
  readonly open: boolean;
  readonly shareUrl: string;
  readonly loading: boolean;
  readonly copied: boolean;
  readonly onOpenChange: (o: boolean) => void;
  readonly onCopy: () => void;
}
const ShareDialog = ({ open, shareUrl, loading, copied, onOpenChange, onCopy }: ShareDialogProps) => {
  const rightIcon = copied ? (
    <Check className="w-4 h-4" />
  ) : loading ? (
    <Loader2 className="w-4 h-4 animate-spin" />
  ) : (
    <Copy className="w-4 h-4" />
  );
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Share Document</DialogTitle>
          <DialogDescription>The generated sharing link is only valid for 10 minutes</DialogDescription>
        </DialogHeader>
        <div className="flex items-center gap-2">
          <Input value={shareUrl} placeholder={loading ? 'Generating link...' : 'Share link'} readOnly disabled={loading} />
          <Button variant="outline" size="sm" onClick={onCopy} disabled={!shareUrl || loading} className="h-8 w-8 p-0">
            {rightIcon}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

interface CommentsSectionProps {
  readonly comments: Comment[];
  readonly loading: boolean;
  readonly error: string;
  readonly page: number;
  readonly pageSize: number;
  readonly totalPages: number;
  readonly onPrev: () => void;
  readonly onNext: () => void;
  readonly getInitials: (name: string) => string;
}
const CommentsSection = memo(({ comments, loading, error, page, pageSize, totalPages, onPrev, onNext, getInitials }: CommentsSectionProps) => {
  if (loading) return <p className="text-center text-sm text-muted-foreground py-4">Loading comments...</p>;
  if (error) return <p className="text-center text-sm text-destructive py-4">{error}</p>;
  if (!comments.length) return <p className="text-center text-sm text-muted-foreground py-4">No comments yet. Be the first to comment!</p>;
  const sliceStart = (page - 1) * pageSize;
  const current = comments.slice(sliceStart, sliceStart + pageSize);
  return (
    <>
      {current.map(c => (
        <div key={c.id} className="flex gap-3 p-3 rounded-lg bg-muted/50">
          <Avatar className="h-8 w-8 flex-shrink-0">
            <AvatarFallback className="text-xs">{getInitials(c.user)}</AvatarFallback>
          </Avatar>
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-1">
              <span className="font-medium text-sm">{c.user}</span>
              <span className="text-xs text-muted-foreground">{c.timestamp}</span>
            </div>
            <p className="text-sm break-words whitespace-pre-wrap">{c.content}</p>
          </div>
        </div>
      ))}
      <div className="flex items-center justify-between pt-2">
        <span className="text-xs text-muted-foreground">Page {page} / {totalPages}</span>
        <div className="flex gap-2">
          <Button variant="outline" size="sm" disabled={page <= 1} onClick={onPrev}>Prev</Button>
          <Button variant="outline" size="sm" disabled={page >= totalPages} onClick={onNext}>Next</Button>
        </div>
      </div>
    </>
  );
});
CommentsSection.displayName = 'CommentsSection';

// Helper to safely derive error message from unknown
const getErrorMessage = (e: unknown, fallback = 'Unknown error'): string => {
  if (e instanceof Error) return e.message || fallback;
  if (typeof e === 'string') return e || fallback;
  if (e && typeof e === 'object' && 'message' in e) {
    const m = (e as { message?: unknown }).message;
    if (typeof m === 'string') return m;
  }
  return fallback;
};

// Main component ----------------------------------------------------------
export function DocumentsList({ documents, searchTerm, searchMode = 'simple', isSearching = false }: DocumentsListProps) {
  // State
  const [downloadingId, setDownloadingId] = useState<string | null>(null);
  const [shareDialogOpen, setShareDialogOpen] = useState(false);
  const [localDocs, setLocalDocs] = useState<AppDocument[]>(Array.isArray(documents) ? documents.map(normalizeDoc) : []);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [selectedDocument, setSelectedDocument] = useState<AppDocument | null>(null);
  const [copied, setCopied] = useState(false);
  const [shareUrl, setShareUrl] = useState<string>('');
  const [shareLoading, setShareLoading] = useState<boolean>(false);
  const [detailDialogOpen, setDetailDialogOpen] = useState(false);
  const [editableTags, setEditableTags] = useState<string[]>([]);
  const [newTag, setNewTag] = useState('');
  const [newComment, setNewComment] = useState('');
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentsLoading, setCommentsLoading] = useState<boolean>(false);
  const [commentsError, setCommentsError] = useState<string>('');
  const [savingTags, setSavingTags] = useState<boolean>(false);
  const [addingComment, setAddingComment] = useState<boolean>(false);
  const [commentsPage, setCommentsPage] = useState<number>(1);
  const COMMENTS_PAGE_SIZE = 3;

  // Effects
  useEffect(() => setLocalDocs(Array.isArray(documents) ? documents.map(normalizeDoc) : []), [documents]);
  useEffect(() => { if (commentsPage > Math.max(1, Math.ceil(comments.length / COMMENTS_PAGE_SIZE))) setCommentsPage(1); }, [comments, commentsPage]);

  // Derived
  const filteredDocuments = searchMode === 'simple'
    ? localDocs.filter(doc => {
        const term = searchTerm.toLowerCase();
        return doc.name.toLowerCase().includes(term) ||
          doc.category.toLowerCase().includes(term) ||
          doc.type.toLowerCase().includes(term) ||
          doc.tags.some(tag => tag.toLowerCase().includes(term));
      })
    : localDocs;
  const totalCommentPages = Math.max(1, Math.ceil(comments.length / COMMENTS_PAGE_SIZE));

  // Actions ---------------------------------------------------------------
  const handleViewDetails = (doc: AppDocument) => {
    setSelectedDocument(doc);
    setEditableTags([...doc.tags]);
    setDetailDialogOpen(true);
    fetchComments(doc.id);
  };

  const handleDownload = async (doc: AppDocument) => {
    const repo = getDocumentRepository();
    const preOpenedTab: Window | null = null;
    const openPreview = (url: string) => {
      if (preOpenedTab && !preOpenedTab.closed) {
        preOpenedTab.location.href = url;
      } else {
        window.open(url, '_blank', 'noopener');
      }
    };
    const triggerDownload = (url: string, fileName?: string) => {
      const a = document.createElement('a');
      a.href = url;
      if (fileName) a.download = fileName;
      a.style.display = 'none';
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    };
    const tryBlobDownload = async (url: string, fileName?: string) => {
      try {
        const res = await fetch(url, { credentials: 'omit' });
        if (!res.ok) return false;
        const blob = await res.blob();
        const objectUrl = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = objectUrl;
        if (fileName) a.download = fileName;
        a.style.display = 'none';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        setTimeout(() => URL.revokeObjectURL(objectUrl), 1000);
        return true;
      } catch { return false; }
    };
    try {
      setDownloadingId(doc.id);
      const info = await repo.getDownloadInfo(doc.id);
      const url = info.url;
      const fileName = info.fileName || doc.name;
      const nameOrUrl = (fileName || url).toLowerCase();
      const isPreviewType = /\.(pdf|png|jpe?g|gif|webp|bmp|svg|tiff?)(\?|$)/i.test(nameOrUrl);
      if (isPreviewType) {
        openPreview(url);
        toast.success(`Open in new Tab: ${fileName}`);
      } else {
        const ok = await tryBlobDownload(url, fileName);
        if (!ok) triggerDownload(url, fileName);
        toast.success(`Start Downloading: ${fileName}`);
      }
    } catch (e: unknown) {
      if (preOpenedTab && !preOpenedTab.closed) preOpenedTab.close();
      toast.error(`Download failed: ${getErrorMessage(e)}`);
    } finally { setDownloadingId(null); }
  };

  const handleDelete = async (documentId: string) => {
    if (!window.confirm('Are you sure? Deletion cannot undo.')) return;
    try {
      setDeletingId(documentId);
      const repo = getDocumentRepository();
      const res = await repo.delete(documentId);
      if (!res.success) {
        toast.error(res.message || 'Delete failed');
        return;
      }
      setLocalDocs(prev => prev.filter(d => d.id !== documentId));
      toast.success(res.message || 'Deleted successfully');
    } catch (e: unknown) { toast.error(getErrorMessage(e, 'Delete failed')); } finally { setDeletingId(null); }
  };

  const handleShare = async (doc: AppDocument) => {
    setSelectedDocument(doc); setShareDialogOpen(true); setCopied(false); setShareUrl(''); setShareLoading(true);
    try {
      const repo = getDocumentRepository();
      const url = await repo.getShareUrl(doc.id);
      setShareUrl(url); toast.success('Shared URL generated');
    } catch (e: unknown) { toast.error(getErrorMessage(e, 'Get share url failed')); setShareDialogOpen(false); } finally { setShareLoading(false); }
  };

  const handleCopyLink = () => {
    if (!shareUrl) return toast.error('No link to copy');
  };

  const fetchComments = useCallback(async (documentId: string) => {
    if (!documentId) return;
    try {
      setCommentsLoading(true); setCommentsError('');
      const repo = getDocumentRepository();
      const list = await repo.getComments(documentId);
      setComments(list); setCommentsPage(1);
    } catch (e: unknown) { setCommentsError(getErrorMessage(e, 'Failed to load comments')); setComments([]); } finally { setCommentsLoading(false); }
  }, []);

  const handleSaveTags = async () => {
    if (!selectedDocument || savingTags) return;
    try {
      setSavingTags(true);
      const repo = getDocumentRepository();
      const newTags = await repo.updateTags(selectedDocument.id, [...editableTags]);
      setSelectedDocument(prev => (prev ? { ...prev, tags: newTags } : prev));
      setLocalDocs(prev => prev.map(d => (d.id === selectedDocument.id ? { ...d, tags: newTags } : d)));
      toast.success('Tags updated successfully');
    } catch (e: unknown) { toast.error(getErrorMessage(e, 'Failed to update tags')); } finally { setSavingTags(false); }
  };

  const handleAddTag = () => {
    const t = newTag.trim();
    if (!t) return;
    if (editableTags.includes(t)) {
      toast.info('Tag already exists');
      return;
    }
    setEditableTags(prev => [...prev, t]);
    setNewTag('');
  };
  const handleRemoveTag = (tag: string) => setEditableTags(prev => prev.filter(t => t !== tag));

  const handleAddComment = async () => {
    const content = newComment.trim(); if (!selectedDocument || !content || addingComment) return;
    try {
      setAddingComment(true);
      const repo = getDocumentRepository();
      const entry = await repo.addComment(selectedDocument.id, content);
      setComments(prev => [entry, ...prev]); setNewComment(''); setCommentsPage(1); toast.success('Comment added successfully');
    } catch (e: unknown) { toast.error(getErrorMessage(e, 'Failed to add comment')); } finally { setAddingComment(false); }
  };

  // Render helpers --------------------------------------------------------
  const renderDocumentsContent = () => {
    if (isSearching) {
      return (
        <div className="text-center py-12">
          <Loader2 className="w-16 h-16 text-muted-foreground mx-auto mb-4 animate-spin" />
          <h3 className="text-lg font-medium mb-2">Searching documents...</h3>
          <p className="text-muted-foreground">Using advanced AI-powered search</p>
        </div>
      );
    }
    if (!filteredDocuments.length) {
      const noDocsMessage = searchTerm
        ? (searchMode === 'advanced'
          ? 'Try different search terms or switch to simple search.'
          : 'Try adjusting your search terms or upload new documents.')
        : 'Upload your first document to get started.';
      return (
        <div className="text-center py-12">
          <FileText className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-medium mb-2">{searchTerm ? 'No documents found' : 'No documents uploaded yet'}</h3>
          <p className="text-muted-foreground">{noDocsMessage}</p>
        </div>
      );
    }
    return (
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
          {filteredDocuments.map(doc => (
            <DocumentRow
              key={doc.id}
              doc={doc}
              onView={handleViewDetails}
              onDownload={handleDownload}
              onShare={handleShare}
              onDelete={handleDelete}
              downloadingId={downloadingId}
              deletingId={deletingId}
            />
          ))}
        </TableBody>
      </Table>
    );
  };

  // JSX -------------------------------------------------------------------
  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold">Document Library</h1>
            {searchMode === 'advanced' && searchTerm && (
              <Badge variant="secondary" className="flex items-center gap-1">
                <Sparkles className="w-3 h-3" /> Advanced Search
              </Badge>
            )}
          </div>
          <p className="text-muted-foreground">
            {isSearching ? (
              <span className="flex items-center gap-2">
                <Loader2 className="w-4 h-4 animate-spin" /> Searching...
              </span>
            ) : (
              <>
                {filteredDocuments.length} {filteredDocuments.length === 1 ? 'document' : 'documents'}
                {searchTerm && ` matching "${searchTerm}"`}
                {!searchTerm && localDocs.length > 0 && ` (${localDocs.length} total)`}
              </>
            )}
          </p>
        </div>
      </div>

      {/* Documents Table */}
      <Card>
        <CardHeader><CardTitle>All Documents</CardTitle></CardHeader>
        <CardContent>{renderDocumentsContent()}</CardContent>
      </Card>

      {/* Share Dialog */}
      <ShareDialog
        open={shareDialogOpen}
        onOpenChange={setShareDialogOpen}
        shareUrl={shareUrl}
        loading={shareLoading}
        copied={copied}
        onCopy={handleCopyLink}
      />

      {/* Detail Dialog */}
      <Dialog open={detailDialogOpen} onOpenChange={setDetailDialogOpen}>
        <DialogContent className="sm:max-w-[700px] p-0">
          <div className="h-[80vh] flex flex-col overflow-hidden">
            <DialogHeader className="shrink-0 px-6 pt-6 pb-3">
              <DialogTitle>Document Details</DialogTitle>
              <DialogDescription>View and manage document information, tags, and comments.</DialogDescription>
            </DialogHeader>
            {selectedDocument && (
              <ScrollArea className="flex-1 min-h-0 px-6 pb-4 pr-4">
                <div className="space-y-6 pb-2">
                  {/* Info */}
                  <div className="space-y-3">
                    <div className="flex items-center gap-3">
                      <div className="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center">{getFileIcon(selectedDocument.type)}</div>
                      <div className="flex-1">
                        <h3 className="font-medium" title={selectedDocument.name}>{selectedDocument.name}</h3>
                        <div className="flex items-center gap-4 text-sm text-muted-foreground flex-wrap">
                          <span className="flex items-center gap-1"><Badge variant="secondary">{selectedDocument.type}</Badge></span>
                          <span>{selectedDocument.size}</span>
                          <span className="flex items-center gap-1"><Calendar className="w-3 h-3" /> {formatDate(selectedDocument.uploadDate)}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <Separator />

                  {/* Tags */}
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <Label className="flex items-center gap-2"><Tag className="w-4 h-4" /> Tags</Label>
                      <Button size="sm" onClick={handleSaveTags} disabled={savingTags}>{savingTags ? 'Saving...' : 'Save Tags'}</Button>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {editableTags.map(tag => (
                        <Badge key={tag} variant="secondary" className="flex items-center gap-1 pr-1">
                          {tag}
                          <Button variant="ghost" size="sm" className="h-4 w-4 p-0" onClick={() => handleRemoveTag(tag)}>
                            <X className="w-3 h-3" />
                          </Button>
                        </Badge>
                      ))}
                      {!editableTags.length && <span className="text-xs text-muted-foreground italic">No tags</span>}
                    </div>
                    <div className="flex gap-2">
                      <Input
                        placeholder="Add new tag..."
                        value={newTag}
                        onChange={e => setNewTag(e.target.value)}
                        className="flex-1"
                        onKeyDown={e => e.key === 'Enter' ? handleAddTag() : undefined}
                      />
                      <Button size="sm" onClick={handleAddTag} disabled={!newTag.trim()}>{addingComment ? 'Adding...' : 'Add Tag'}</Button>
                    </div>
                  </div>
                  <Separator />

                  {/* Comments */}
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <Label className="flex items-center gap-2"><MessageCircle className="w-4 h-4" /> Comments</Label>
                    </div>
                    <div className="bg-muted/50 rounded-lg p-4">
                      <CommentsSection
                        comments={comments}
                        loading={commentsLoading}
                        error={commentsError}
                        page={commentsPage}
                        pageSize={COMMENTS_PAGE_SIZE}
                        totalPages={totalCommentPages}
                        onPrev={() => setCommentsPage(p => Math.max(p - 1, 1))}
                        onNext={() => setCommentsPage(p => Math.min(p + 1, totalCommentPages))}
                        getInitials={getInitials}
                      />
                    </div>
                    <div className="flex gap-2">
                      <Textarea
                        placeholder="Add a comment..."
                        value={newComment}
                        onChange={e => setNewComment(e.target.value)}
                        className="flex-1 resize-none"
                        minRows={1}
                        maxRows={4}
                      />
                      <Button size="sm" onClick={handleAddComment} disabled={!newComment.trim() || addingComment} className="whitespace-nowrap">
                        {addingComment ? 'Adding...' : 'Add Comment'}
                      </Button>
                    </div>
                  </div>
                </div>
              </ScrollArea>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
