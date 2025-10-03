import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { ImageWithFallback } from "./figma/ImageWithFallback";
import { 
  Upload, 
  Search, 
  Share2, 
  Lock, 
  FileText, 
  Users,
  Cloud,
  Smartphone
} from "lucide-react";

const features = [
  {
    icon: Upload,
    title: "Easy Upload",
    description: "Drag and drop files or browse to upload documents instantly. Support for all major file formats."
  },
  {
    icon: Search,
    title: "Smart Search",
    description: "Find your documents quickly with powerful search functionality across file names and content."
  },
  {
    icon: Share2,
    title: "Secure Sharing",
    description: "Share documents with team members or external collaborators with customizable permissions."
  },
  {
    icon: Lock,
    title: "Data Security",
    description: "Enterprise-grade encryption ensures your documents are protected and compliant."
  },
  {
    icon: Cloud,
    title: "Cloud Storage",
    description: "Access your documents from anywhere with reliable cloud storage and automatic backups."
  },
  {
    icon: Smartphone,
    title: "Mobile Ready",
    description: "Responsive design works seamlessly across desktop, tablet, and mobile devices."
  }
];

export function Features() {
  return (
    <section id="features" className="py-20 bg-muted/30">
      <div className="container mx-auto px-4">
        <div className="text-center space-y-4 mb-16">
          <h2 className="text-3xl lg:text-4xl font-bold">Powerful Features</h2>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
            Everything you need to manage your documents efficiently and collaborate with your team.
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 mb-16">
          {features.map((feature, index) => (
            <Card key={index} className="border-0 shadow-sm hover:shadow-md transition-shadow">
              <CardHeader>
                <div className="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center mb-4">
                  <feature.icon className="h-6 w-6 text-primary" />
                </div>
                <CardTitle className="text-xl">{feature.title}</CardTitle>
              </CardHeader>
              <CardContent>
                <CardDescription className="text-base">
                  {feature.description}
                </CardDescription>
              </CardContent>
            </Card>
          ))}
        </div>

        <div className="grid lg:grid-cols-2 gap-12 items-center">
          <div className="space-y-6">
            <div className="space-y-4">
              <h3 className="text-2xl lg:text-3xl font-bold">
                Collaborate Seamlessly
              </h3>
              <p className="text-muted-foreground">
                Work together on documents with real-time collaboration features. 
                Share files, leave comments, and track changes with your team members.
              </p>
            </div>
            
            <div className="space-y-4">
              <div className="flex items-start space-x-3">
                <Users className="h-5 w-5 text-primary mt-1" />
                <div>
                  <h4 className="font-medium">Team Workspaces</h4>
                  <p className="text-sm text-muted-foreground">Create shared spaces for different projects and teams.</p>
                </div>
              </div>
              <div className="flex items-start space-x-3">
                <FileText className="h-5 w-5 text-primary mt-1" />
                <div>
                  <h4 className="font-medium">Version Control</h4>
                  <p className="text-sm text-muted-foreground">Track document versions and changes automatically.</p>
                </div>
              </div>
            </div>
          </div>

          <div className="relative">
            <ImageWithFallback
              src="https://images.unsplash.com/photo-1600880292089-90a7e086ee0c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0ZWFtJTIwY29sbGFib3JhdGlvbnxlbnwxfHx8fDE3NTkwMDE4MDR8MA&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
              alt="Team collaboration"
              className="rounded-2xl shadow-xl w-full h-auto"
            />
          </div>
        </div>
      </div>
    </section>
  );
}