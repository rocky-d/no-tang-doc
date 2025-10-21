import { useEffect, useState } from 'react';
import { FileText, TrendingUp, Calendar, HardDrive } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Progress } from './ui/progress';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { getAllDocuments } from '../utils/documentApi';

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

interface DashboardOverviewProps {
  documents: Document[];
}

// Mock data for charts
const monthlyData = [
  { month: 'Sep', uploads: 8 },
  { month: 'Oct', uploads: 12 },
  { month: 'Nov', uploads: 15 },
  { month: 'Dec', uploads: 18 },
  { month: 'Jan', uploads: 22 },
  { month: 'Feb', uploads: 25 }
];

// const categoryData = [
//   { name: 'Work', value: 40, color: '#8884d8' },
//   { name: 'Finance', value: 30, color: '#82ca9d' },
//   { name: 'Design', value: 20, color: '#ffc658' },
//   { name: 'Personal', value: 10, color: '#ff7c7c' }
// ];

export function DashboardOverview({ documents }: DashboardOverviewProps) {
  // Fetch real documents and prefer them over props
  const [apiDocs, setApiDocs] = useState<Document[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  useEffect(() => {
    let mounted = true;
    (async () => {
      // If parent already passed documents, skip fetching to avoid duplicate calls
      if (documents && documents.length > 0) {
        setApiDocs([]);
        return;
      }
      setLoading(true);
      try {
        const docs = await getAllDocuments();
        if (!mounted) return;
        setApiDocs(docs);
      } catch (e) {
        console.warn('DashboardOverview: failed to load documents from API, falling back to props', e);
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => {
      mounted = false;
    };
  }, [documents]);

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
              <p className="text-xs text-muted-foreground">
                <span className="text-emerald-500">+2</span> from last month
              </p>
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
                <span className="text-emerald-500">+12%</span> from last month
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
                <h3 className="tracking-tight text-sm font-medium">Growth Rate</h3>
                <TrendingUp className="h-4 w-4 text-muted-foreground" />
              </div>
              <div className="text-2xl font-bold">+15%</div>
              <p className="text-xs text-muted-foreground">
                Upload growth this quarter
              </p>
            </CardContent>
          </Card>
        </div>

        {/* Charts Row */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Upload Trends */}
          {/*<Card>*/}
          {/*  <CardHeader>*/}
          {/*    <CardTitle>Upload Trends</CardTitle>*/}
          {/*  </CardHeader>*/}
          {/*  <CardContent>*/}
          {/*<ResponsiveContainer width="100%" height={300}>*/}
          {/*  <BarChart data={monthlyData}>*/}
          {/*    <CartesianGrid strokeDasharray="3 3" />*/}
          {/*    <XAxis dataKey="month" />*/}
          {/*    <YAxis />*/}
          {/*    <Tooltip />*/}
          {/*    <Bar dataKey="uploads" fill="#8884d8" />*/}
          {/*  </BarChart>*/}
          {/*</ResponsiveContainer>*/}
          {/*  </CardContent>*/}
          {/*</Card>*/}

          {/* Category Distribution (temporarily hidden) */}
          {/**
           <Card>
           <CardHeader>
           <CardTitle>Category Distribution</CardTitle>
           </CardHeader>
           <CardContent>
           <ResponsiveContainer width="100%" height={300}>
           <PieChart>
           <Pie
           data={categoryData}
           cx="50%"
           cy="50%"
           labelLine={false}
           label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
           outerRadius={80}
           fill="#8884d8"
           dataKey="value"
           >
           {categoryData.map((entry, index) => (
           <Cell key={`cell-${index}`} fill={entry.color} />
           ))}
           </Pie>
           <Tooltip />
           </PieChart>
           </ResponsiveContainer>
           </CardContent>
           </Card>
           */}
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