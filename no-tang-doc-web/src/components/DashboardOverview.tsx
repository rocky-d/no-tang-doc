import { useEffect, useState } from 'react';
import { FileText, TrendingUp, Calendar, HardDrive } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Progress } from './ui/progress';

import { getAllDocuments } from '../utils/documentApi';
import { http } from '../utils/request';

interface Document {
  id: string;
  name: string;
  type: string; // extension (e.g., pdf, md)
  size: string;
  uploadDate: string;
  category: string;
  tags: string[];
  sizeBytes?: number; // raw size for accurate totals
}

// Minimal LogEntry shape reused from LogsPage for success rate
interface LogEntry {
  operationStatus: 'SUCCESS' | 'FAILURE';
}

interface DashboardOverviewProps {
  readonly documents: Document[];
}

// Reuse LogsPage endpoint logic
const LOGS_LIST_ALL = (import.meta.env as any).VITE_LOGS_LIST_ALL || '/api/v1/logs';

export function DashboardOverview({ documents }: DashboardOverviewProps) {
  // Fetch real documents and prefer them over props
  const [apiDocs, setApiDocs] = useState<Document[]>([]);

  // Success rate state (from logs API)
  const [successRate, setSuccessRate] = useState<number | null>(null);
  const [successLoading, setSuccessLoading] = useState<boolean>(false);

  useEffect(() => {
    let mounted = true;
    (async () => {
      // If parent already passed documents, skip fetching to avoid duplicate calls
      if (documents && documents.length > 0) {
        setApiDocs([]);
        return;
      }
      try {
        const docs = await getAllDocuments();
        if (!mounted) return;
        setApiDocs(docs);
      } catch (e) {
        console.warn('DashboardOverview: failed to load documents from API, falling back to props', e);
      }
    })();
    return () => {
      mounted = false;
    };
  }, [documents]);

  // Fetch logs and compute success rate (same logic as LogsPage)
  useEffect(() => {
    let mounted = true;
    (async () => {
      setSuccessLoading(true);
      try {
        const resp: any = await http.get(LOGS_LIST_ALL);
        const data = resp?.data ?? resp;
        if (!Array.isArray(data)) {
          console.warn('DashboardOverview: logs API returned non-array payload');
          if (mounted) setSuccessRate(0);
          return;
        }
        const logs = data as LogEntry[];
        const total = logs.length;
        const success = logs.filter(l => l.operationStatus === 'SUCCESS').length;
        const rate = total > 0 ? Math.round((success / total) * 100) : 0;
        if (mounted) setSuccessRate(rate);
      } catch (e) {
        console.warn('DashboardOverview: failed to load logs for success rate', e);
        if (mounted) setSuccessRate(0);
      } finally {
        if (mounted) setSuccessLoading(false);
      }
    })();
    return () => { mounted = false; };
  }, []);

  const dataSource: Document[] = (documents && documents.length > 0) ? documents : apiDocs;

  // Calculate stats
  const totalDocuments = dataSource.length;
  const thisMonthUploads = dataSource.filter(doc => {
    const uploadDate = new Date(doc.uploadDate);
    const now = new Date();
    return uploadDate.getMonth() === now.getMonth() && uploadDate.getFullYear() === now.getFullYear();
  }).length;

  // Real storage usage based on bytes
  const totalBytes = dataSource.reduce((acc, doc) => acc + (typeof doc.sizeBytes === 'number' ? doc.sizeBytes : 0), 0);
  const capacityBytes = 100 * 1024 * 1024; // 100 MB fixed display capacity
  const storageUsed = Math.max(0, Math.min(100, Math.round((totalBytes / capacityBytes) * 100)));
  const totalMB = totalBytes / (1024 * 1024);

  // Get recent documents (3 most recent)
  const recentDocuments = [...dataSource]
      .sort((a, b) => new Date(b.uploadDate).getTime() - new Date(a.uploadDate).getTime())
      .slice(0, 3);

  // Precompute success rate text (avoid nested ternary in JSX)
  let successRateText: string;
  if (successRate !== null) successRateText = `${successRate}%`;
  else successRateText = successLoading ? '...' : '0%';

  return (
      <div className="space-y-6">
        {/* Overview Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between space-y-0 pb-2">
                <h3 className="tracking-tight text-sm font-medium">Total Documents</h3>
                <FileText className="h-4 w-4 text-muted-foreground" />
              </div>
              <div className="text-2xl font-bold">{totalDocuments}</div>
              {/*<p className="text-xs text-muted-foreground">*/}
              {/*  <span className="text-emerald-500">+2</span> from last month*/}
              {/*</p>*/}
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between space-y-0 pb-2">
                <h3 className="tracking-tight text-sm font-medium">This Month</h3>
                <Calendar className="h-4 w-4 text-muted-foreground" />
              </div>
              <div className="text-2xl font-bold">{thisMonthUploads}</div>
              <p className="text-xs text-muted-foreground">
                <span className="text-emerald-500">+{thisMonthUploads}</span> from last month
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between space-y-0 pb-2">
                <h3 className="tracking-tight text-sm font-medium">Storage Used</h3>
                <HardDrive className="h-4 w-4 text-muted-foreground" />
              </div>
              <div className="text-2xl font-bold">{totalMB.toFixed(1)} MB</div>
              <div className="mt-2">
                <Progress value={storageUsed} className="h-2" />
                <p className="text-xs text-muted-foreground mt-1">{storageUsed}% of 100 MB used</p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between space-y-0 pb-2">
                <h3 className="tracking-tight text-sm font-medium">Success Rate</h3>
                <TrendingUp className="h-4 w-4 text-muted-foreground" />
              </div>
              <div className="text-2xl font-bold">{successRateText}</div>
              <p className="text-xs text-muted-foreground">
                Based on NTDoc Logs
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Charts Row */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Upload Trends */}
          {/* Chart temporarily disabled */}
        </div>

        {/* Recent Documents */}
        <Card>
          <CardHeader>
            <CardTitle>Recent Documents</CardTitle>
          </CardHeader>
          <CardContent>
            {recentDocuments.length === 0 ? (
                <div className="text-center py-8">
                  <FileText className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">No documents uploaded yet.</p>
                </div>
            ) : (
                <div className="space-y-4">
                  {recentDocuments.map((document) => (
                      <div key={document.id} className="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors">
                        <div className="flex items-center space-x-4 flex-1 min-w-0">
                          <div className="w-10 h-10 bg-primary/10 rounded-lg flex items-center justify-center flex-shrink-0">
                            <FileText className="w-5 h-5 text-primary" />
                          </div>
                          <div className="flex-1 min-w-0">
                            <h4 className="font-medium">{document.name}</h4>
                            <div className="flex items-center flex-wrap gap-2 text-sm text-muted-foreground mt-1">
                              <span>{document.type}</span>
                              <span>•</span>
                              <span>{document.size}</span>
                              <span>•</span>
                              <span>Uploaded {document.uploadDate}</span>
                            </div>
                            {/** Tags temporarily hidden
                             <div className="flex flex-wrap gap-1 mt-2">
                             {document.tags.slice(0, 3).map((tag, index) => (
                             <Badge
                             key={index}
                             variant="secondary"
                             className="text-xs px-2 py-0"
                             >
                             {tag}
                             </Badge>
                             ))}
                             {document.tags.length > 3 && (
                             <Badge
                             variant="outline"
                             className="text-xs px-2 py-0"
                             >
                             +{document.tags.length - 3}
                             </Badge>
                             )}
                             </div>
                             */}
                          </div>
                        </div>
                      </div>
                  ))}
                </div>
            )}
          </CardContent>
        </Card>
      </div>
  );
}