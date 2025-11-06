import { useState, useEffect } from 'react';
import { UserLayout } from '../components/UserLayout';
import type { SearchMode } from '../components/SearchDialog';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Badge } from '../components/ui/badge';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../components/ui/table';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { Button } from '../components/ui/button';
import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight, FileUp, FileDown, CheckCircle, XCircle, User, Activity, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { http } from '../utils/request';

interface LogEntry {
    id: number;
    actorType: string;
    actorName: string;
    userId: number;
    operationType: string;
    targetId: number;
    targetName: string;
    operationStatus: 'SUCCESS' | 'FAILURE';
    message: string | null;
    time: string;
}

const LOGS_LIST_ALL = (import.meta.env as unknown).VITE_LOGS_LIST_ALL || '/api/v1/logs';

export function LogsPage() {
    const [logs, setLogs] = useState<LogEntry[]>([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [searchQuery, setSearchQuery] = useState('');
    const [searchMode, setSearchMode] = useState<SearchMode>('simple');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const fetchLogs = async () => {
        try {
            setLoading(true);
            setError('');
            const resp: unknown = await http.get(`${LOGS_LIST_ALL}`);
            const data = resp?.data ?? resp;
            if (!Array.isArray(data)) {
                throw new Error('Unexpected logs response');
            }
            setLogs(data as LogEntry[]);
        } catch (e: unknown) {
            console.error('Fetch logs failed', e);
            setError(e?.message || '获取日志失败');
            toast.error(e?.message || '获取日志失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchLogs();
         
    }, []);

    // Calculate pagination
    const totalPages = Math.max(1, Math.ceil(logs.length / pageSize));
    const currentPageSafe = Math.min(currentPage, totalPages);
    const startIndex = (currentPageSafe - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const currentLogs = logs.slice(startIndex, endIndex);

    // Reset to page 1 when page size changes
    useEffect(() => {
        setCurrentPage(1);
    }, [pageSize]);

    const handleSearch = (query: string, mode: SearchMode) => {
        setSearchQuery(query);
        setSearchMode(mode);
        // Optional: client-side filtering can be implemented here if needed
    };

    const getOperationIcon = (operationType: string) => {
        switch (operationType) {
            case 'UPLOAD_DOCUMENT':
                return <FileUp className="w-4 h-4" />;
            case 'DOWNLOAD_DOCUMENT':
                return <FileDown className="w-4 h-4" />;
            default:
                return <Activity className="w-4 h-4" />;
        }
    };

    const getOperationLabel = (operationType: string) => {
        switch (operationType) {
            case 'UPLOAD_DOCUMENT':
                return 'Upload';
            case 'DOWNLOAD_DOCUMENT':
                return 'Download';
            default:
                return operationType.replace(/_/g, ' ');
        }
    };

    const getStatusBadge = (status: string) => {
        if (status === 'SUCCESS') {
            return (
                <Badge variant="outline" className="bg-green-500/10 text-green-700 dark:text-green-400 border-green-500/20">
                    <CheckCircle className="w-3 h-3 mr-1" />
                    Success
                </Badge>
            );
        }
        return (
            <Badge variant="outline" className="bg-red-500/10 text-red-700 dark:text-red-400 border-red-500/20">
                <XCircle className="w-3 h-3 mr-1" />
                Failed
            </Badge>
        );
    };

    const formatTime = (timeString: string) => {
        const date = new Date(timeString);
        return new Intl.DateTimeFormat('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        }).format(date);
    };

    return (
        <UserLayout
            onSearch={handleSearch}
            currentSearchQuery={searchQuery}
            currentSearchMode={searchMode}
        >
            <div className="space-y-6">
                {/* Header */}
                <div>
                    <h1>Activity Logs</h1>
                    <p className="text-muted-foreground">
                        View all user operations and system activities
                    </p>
                </div>

                {/* Loading & Error States */}
                {loading && (
                    <div className="flex items-center justify-center py-16">
                        <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
                    </div>
                )}
                {!loading && error && (
                    <Card className="p-6">
                        <p className="text-destructive mb-4">{error}</p>
                        <Button variant="outline" onClick={fetchLogs}>重试</Button>
                    </Card>
                )}

                {!loading && !error && (
                    <>
                        {/* Stats Cards */}
                        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                            <Card>
                                <CardHeader className="pb-2">
                                    <CardDescription>Total Activities</CardDescription>
                                    <CardTitle className="text-2xl">{logs.length}</CardTitle>
                                </CardHeader>
                            </Card>
                            <Card>
                                <CardHeader className="pb-2">
                                    <CardDescription>Uploads</CardDescription>
                                    <CardTitle className="text-2xl">
                                        {logs.filter(log => log.operationType === 'UPLOAD_DOCUMENT').length}
                                    </CardTitle>
                                </CardHeader>
                            </Card>
                            <Card>
                                <CardHeader className="pb-2">
                                    <CardDescription>Downloads</CardDescription>
                                    <CardTitle className="text-2xl">
                                        {logs.filter(log => log.operationType === 'DOWNLOAD_DOCUMENT').length}
                                    </CardTitle>
                                </CardHeader>
                            </Card>
                            <Card>
                                <CardHeader className="pb-2">
                                    <CardDescription>Success Rate</CardDescription>
                                    <CardTitle className="text-2xl">
                                        {logs.length > 0 ? Math.round((logs.filter(log => log.operationStatus === 'SUCCESS').length / logs.length) * 100) : 0}%
                                    </CardTitle>
                                </CardHeader>
                            </Card>
                        </div>

                        {/* Logs Table */}
                        <Card>
                            <CardHeader>
                                <div className="flex items-center justify-between">
                                    <div>
                                        <CardTitle>Recent Activities</CardTitle>
                                        <CardDescription>
                                            Showing {logs.length === 0 ? 0 : startIndex + 1} - {Math.min(endIndex, logs.length)} of {logs.length} activities
                                        </CardDescription>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <span className="text-sm text-muted-foreground">Rows per page:</span>
                                        <Select
                                            value={pageSize.toString()}
                                            onValueChange={(value: string) => setPageSize(Number(value))}
                                        >
                                            <SelectTrigger className="w-[80px]">
                                                <SelectValue />
                                            </SelectTrigger>
                                            <SelectContent>
                                                <SelectItem value="10">10</SelectItem>
                                                <SelectItem value="15">15</SelectItem>
                                                <SelectItem value="20">20</SelectItem>
                                            </SelectContent>
                                        </Select>
                                    </div>
                                </div>
                            </CardHeader>
                            <CardContent>
                                <div className="rounded-md border">
                                    <Table>
                                        <TableHeader>
                                            <TableRow>
                                                <TableHead className="w-[100px]">ID</TableHead>
                                                <TableHead>User</TableHead>
                                                <TableHead>Operation</TableHead>
                                                <TableHead>Target</TableHead>
                                                <TableHead>Status</TableHead>
                                                <TableHead>Time</TableHead>
                                            </TableRow>
                                        </TableHeader>
                                        <TableBody>
                                            {currentLogs.length === 0 ? (
                                                <TableRow>
                                                    <TableCell colSpan={6} className="text-center py-8 text-muted-foreground">
                                                        No activities found
                                                    </TableCell>
                                                </TableRow>
                                            ) : (
                                                currentLogs.map((log) => (
                                                    <TableRow key={log.id}>
                                                        <TableCell className="font-medium">#{log.id}</TableCell>
                                                        <TableCell>
                                                            <div className="flex items-center gap-2">
                                                                <User className="w-4 h-4 text-muted-foreground" />
                                                                <span>{log.actorName}</span>
                                                            </div>
                                                        </TableCell>
                                                        <TableCell>
                                                            <div className="flex items-center gap-2">
                                                                {getOperationIcon(log.operationType)}
                                                                <span>{getOperationLabel(log.operationType)}</span>
                                                            </div>
                                                        </TableCell>
                                                        <TableCell>
                                                            <div className="max-w-[300px] truncate" title={log.targetName}>
                                                                {log.targetName}
                                                            </div>
                                                        </TableCell>
                                                        <TableCell>{getStatusBadge(log.operationStatus)}</TableCell>
                                                        <TableCell className="text-sm text-muted-foreground">
                                                            {formatTime(log.time)}
                                                        </TableCell>
                                                    </TableRow>
                                                ))
                                            )}
                                        </TableBody>
                                    </Table>
                                </div>

                                {/* Pagination Controls */}
                                {logs.length > 0 && (
                                    <div className="flex items-center justify-between mt-4">
                                        <div className="text-sm text-muted-foreground">
                                            Page {currentPageSafe} of {totalPages}
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <Button
                                                variant="outline"
                                                size="sm"
                                                onClick={() => setCurrentPage(1)}
                                                disabled={currentPageSafe === 1}
                                            >
                                                <ChevronsLeft className="w-4 h-4" />
                                            </Button>
                                            <Button
                                                variant="outline"
                                                size="sm"
                                                onClick={() => setCurrentPage(prev => Math.max(1, prev - 1))}
                                                disabled={currentPageSafe === 1}
                                            >
                                                <ChevronLeft className="w-4 h-4" />
                                            </Button>
                                            <div className="flex items-center gap-1">
                                                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                                                    let pageNum;
                                                    if (totalPages <= 5) {
                                                        pageNum = i + 1;
                                                    } else if (currentPageSafe <= 3) {
                                                        pageNum = i + 1;
                                                    } else if (currentPageSafe >= totalPages - 2) {
                                                        pageNum = totalPages - 4 + i;
                                                    } else {
                                                        pageNum = currentPageSafe - 2 + i;
                                                    }

                                                    return (
                                                        <Button
                                                            key={pageNum}
                                                            variant={currentPageSafe === pageNum ? "default" : "outline"}
                                                            size="sm"
                                                            onClick={() => setCurrentPage(pageNum)}
                                                            className="w-8 h-8 p-0"
                                                        >
                                                            {pageNum}
                                                        </Button>
                                                    );
                                                })}
                                            </div>
                                            <Button
                                                variant="outline"
                                                size="sm"
                                                onClick={() => setCurrentPage(prev => Math.min(totalPages, prev + 1))}
                                                disabled={currentPageSafe === totalPages}
                                            >
                                                <ChevronRight className="w-4 h-4" />
                                            </Button>
                                            <Button
                                                variant="outline"
                                                size="sm"
                                                onClick={() => setCurrentPage(totalPages)}
                                                disabled={currentPageSafe === totalPages}
                                            >
                                                <ChevronsRight className="w-4 h-4" />
                                            </Button>
                                        </div>
                                    </div>
                                )}
                            </CardContent>
                        </Card>
                    </>
                )}
            </div>
        </UserLayout>
    );
}
