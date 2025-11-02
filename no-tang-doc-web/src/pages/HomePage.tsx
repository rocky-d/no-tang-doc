// src/pages/HomePage.tsx (命名导出版本)
import { Header } from '../components/Header';
import { Hero } from '../components/Hero';
import { Features } from '../components/Features';
import { Footer } from '../components/Footer';
import { PerformanceSection } from "../components/PerformanceSection.tsx";
import { AIAgentSection } from "../components/AiAgentSection";
import { AboutSection } from "../components/AboutSection";

export interface HomePageProps {
    onNavigateToAuth: (mode: 'login' | 'register') => void;
    onNavigateToDashboard: () => void;
    onNavigateHome: () => void;
    onStartUploading: () => void;
}

export function HomePage(props: HomePageProps) {
    const {
        onNavigateToAuth,
        onNavigateToDashboard,
        onNavigateHome,
        onStartUploading
    } = props;

    return (
        <div className="min-h-screen bg-background">
            <Header
                onNavigateToAuth={onNavigateToAuth}
                onNavigateToDashboard={onNavigateToDashboard}
                onNavigateHome={onNavigateHome}
            />
            <main>
                <Hero onStartUploading={onStartUploading} />
                <Features />
                <PerformanceSection />
                <AIAgentSection />
                <AboutSection />
            </main>
            <Footer />
        </div>
    );
}