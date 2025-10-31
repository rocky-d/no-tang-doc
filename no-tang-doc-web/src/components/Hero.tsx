import { Button } from "./ui/button";
import { ImageWithFallback } from "./figma/ImageWithFallback";
import { Upload, FileText, Shield, Zap } from "lucide-react";
import { useAuth } from "./AuthContext";

interface HeroProps {
  onStartUploading: () => void;
}

export function Hero({ onStartUploading }: HeroProps) {
  const { user } = useAuth();
  return (
    <section id="home" className="py-20 lg:py-32 bg-gradient-to-br from-background to-muted/50">
      <div className="container mx-auto px-4">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          <div className="space-y-6">
            <div className="space-y-4">
              <h1 className="text-4xl lg:text-6xl font-bold tracking-tight">
                Your Digital
                <span className="text-primary block">Document Hub</span>
              </h1>
              <p className="text-lg text-muted-foreground max-w-lg">
                Store, organize, and share your documents securely. Access your files from anywhere, 
                collaborate with your team, and keep your digital workspace organized.
              </p>
            </div>
            
            <div className="flex flex-col sm:flex-row gap-4">
              <Button size="lg" className="text-base" onClick={onStartUploading}>
                <Upload className="mr-2 h-5 w-5" />
                Start Uploading
              </Button>
              <Button size="lg" variant="outline" className="text-base">
                <FileText className="mr-2 h-5 w-5" />
                View Demo
              </Button>
            </div>

            <div className="flex items-center space-x-8 pt-8">
              <div className="flex items-center space-x-2">
                <Shield className="h-5 w-5 text-green-600" />
                <span className="text-sm text-muted-foreground">Secure Storage</span>
              </div>
              <div className="flex items-center space-x-2">
                <Zap className="h-5 w-5 text-blue-600" />
                <span className="text-sm text-muted-foreground">Fast Access</span>
              </div>
            </div>
          </div>

          <div className="relative">
            <div className="absolute inset-0 bg-gradient-to-r from-primary/20 to-secondary/20 rounded-2xl transform rotate-3"></div>
            <ImageWithFallback
              src="https://images.unsplash.com/photo-1590417286292-4274afeee179?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxkaWdpdGFsJTIwd29ya3NwYWNlfGVufDF8fHx8MTc1ODk5NTg5NXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
              alt="Digital workspace"
              className="relative rounded-2xl shadow-2xl w-full h-auto"
            />
          </div>
        </div>
      </div>
    </section>
  );
}