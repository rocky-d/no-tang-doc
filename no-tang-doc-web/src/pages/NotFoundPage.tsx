
import { useNavigate } from 'react-router-dom';
import {Home, ArrowLeft, FileQuestion } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/components/AuthContext';
import {Footer} from "@/components/Footer";
import {Header} from "@/components/Header";
import type { HomePageProps } from '../pages/HomePage';

export function NotFoundPage(props: Readonly<HomePageProps>) {
    const {
        onNavigateToAuth,
        onNavigateToDashboard,
        onNavigateHome
    } = props;
    const navigate = useNavigate();
    const { user } = useAuth();

    const handleGoHome = () => {
        navigate('/');
    };

    const handleGoToDashboard = () => {
        navigate('/dashboard');
    };

    const handleGoBack = () => {
        navigate(-1);
    };

    return (
        <div className="min-h-screen bg-background flex flex-col">
            {/* Header */}
            <Header
                onNavigateToAuth={onNavigateToAuth}
                onNavigateToDashboard={onNavigateToDashboard}
                onNavigateHome={onNavigateHome}
            />

            {/* 404 Content */}
            <main className="flex-1 flex items-center justify-center px-4 py-16">
                <div className="max-w-2xl w-full text-center">
                    <div className="mb-8 flex justify-center">
                        <div className="relative">
                            <div className="absolute inset-0 bg-primary/10 blur-3xl rounded-full"></div>
                            <FileQuestion className="h-32 w-32 text-primary relative" strokeWidth={1.5} />
                        </div>
                    </div>

                    <h1 className="text-6xl mb-4">404</h1>
                    <h2 className="text-3xl mb-4">Page Not Found</h2>
                    <p className="text-muted-foreground mb-8 max-w-md mx-auto">
                        Sorry, we couldn't find the page you're looking for. The document you're trying to access may have been moved or doesn't exist.
                    </p>

                    <div className="flex flex-col sm:flex-row gap-4 justify-center items-center">
                        <Button
                            onClick={handleGoBack}
                            variant="outline"
                            className="flex items-center space-x-2"
                        >
                            <ArrowLeft className="h-4 w-4" />
                            <span>Go Back</span>
                        </Button>

                        {user ? (
                            <Button
                                onClick={handleGoToDashboard}
                                className="flex items-center space-x-2"
                            >
                                <Home className="h-4 w-4" />
                                <span>Go to Dashboard</span>
                            </Button>
                        ) : (
                            <Button
                                onClick={handleGoHome}
                                className="flex items-center space-x-2"
                            >
                                <Home className="h-4 w-4" />
                                <span>Go to Home</span>
                            </Button>
                        )}
                    </div>
                </div>
            </main>

            {/* Footer */}
            <Footer />
        </div>
    );
}