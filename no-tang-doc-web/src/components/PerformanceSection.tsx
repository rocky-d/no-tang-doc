import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Server, Shield, Layers, Zap, Globe, Lock } from "lucide-react";

const architectureFeatures = [
    {
        icon: Server,
        title: "Digital Ocean Infrastructure",
        description: "Deployed on Digital Ocean's robust infrastructure for optimal performance and reliability."
    },
    {
        icon: Layers,
        title: "DOKS (Kubernetes)",
        description: "Leveraging Digital Ocean Kubernetes Service for automated scaling and high availability."
    },
    {
        icon: Globe,
        title: "App Platform",
        description: "Built on Digital Ocean's App Platform ensuring seamless deployment and continuous integration."
    },
    {
        icon: Shield,
        title: "Keycloak Authentication",
        description: "Enterprise-grade authentication and authorization powered by Keycloak."
    },
    {
        icon: Lock,
        title: "OAuth 2.0 Security",
        description: "Industry-standard OAuth 2.0 protocol ensures secure access to your documents."
    },
    {
        icon: Zap,
        title: "Auto-Scaling",
        description: "Intelligent auto-scaling adapts to your workload, ensuring consistent performance."
    }
];

export function PerformanceSection() {
    return (
        <section id="performance" className="py-20 bg-background">
            <div className="container mx-auto px-4">
                <div className="text-center space-y-4 mb-16">
                    <h2 className="text-3xl lg:text-4xl font-bold">Enterprise-Grade Performance</h2>
                    <p className="text-lg text-muted-foreground max-w-2xl mx-auto">
                        Built on cutting-edge cloud infrastructure with security and scalability at its core.
                    </p>
                </div>

                <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 mb-16">
                    {architectureFeatures.map((feature, index) => (
                        <Card key={index} className="border-0 shadow-sm hover:shadow-md transition-shadow">
                            <CardHeader>
                                <div className="w-12 h-12 bg-blue-500/10 rounded-lg flex items-center justify-center mb-4">
                                    <feature.icon className="h-6 w-6 text-blue-600 dark:text-blue-400" />
                                </div>
                                <CardTitle className="text-xl">{feature.title}</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-muted-foreground">
                                    {feature.description}
                                </p>
                            </CardContent>
                        </Card>
                    ))}
                </div>

                {/* Architecture Diagram Section */}
                <div className="bg-muted/30 rounded-2xl p-8 lg:p-12">
                    <div className="grid lg:grid-cols-2 gap-8 items-center">
                        <div className="space-y-6">
                            <h3 className="text-2xl lg:text-3xl font-bold">
                                Scalable & Secure Architecture
                            </h3>
                            <div className="space-y-4">
                                <div className="flex items-start space-x-3">
                                    <div className="w-6 h-6 bg-primary rounded-full flex items-center justify-center flex-shrink-0 mt-1">
                                        <span className="text-primary-foreground text-xs">1</span>
                                    </div>
                                    <div>
                                        <h4 className="font-medium">Cloud-Native Deployment</h4>
                                        <p className="text-sm text-muted-foreground">
                                            Hosted on Digital Ocean with DOKS for container orchestration and seamless scaling.
                                        </p>
                                    </div>
                                </div>
                                <div className="flex items-start space-x-3">
                                    <div className="w-6 h-6 bg-primary rounded-full flex items-center justify-center flex-shrink-0 mt-1">
                                        <span className="text-primary-foreground text-xs">2</span>
                                    </div>
                                    <div>
                                        <h4 className="font-medium">Secure Authentication</h4>
                                        <p className="text-sm text-muted-foreground">
                                            Keycloak integration with OAuth 2.0 ensures enterprise-grade security and SSO capabilities.
                                        </p>
                                    </div>
                                </div>
                                <div className="flex items-start space-x-3">
                                    <div className="w-6 h-6 bg-primary rounded-full flex items-center justify-center flex-shrink-0 mt-1">
                                        <span className="text-primary-foreground text-xs">3</span>
                                    </div>
                                    <div>
                                        <h4 className="font-medium">High Availability</h4>
                                        <p className="text-sm text-muted-foreground">
                                            Multi-node clusters and automated failover guarantee 99.9% uptime for your documents.
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="bg-gradient-to-br from-blue-500/10 via-purple-500/10 to-pink-500/10 rounded-xl p-8 border">
                            <div className="space-y-6">
                                <div className="text-center space-y-2">
                                    <div className="inline-block px-4 py-2 bg-background rounded-lg shadow-sm border">
                                        <Globe className="h-8 w-8 text-primary mx-auto mb-2" />
                                        <p className="text-sm font-medium">Digital Ocean Cloud</p>
                                    </div>
                                </div>

                                <div className="flex justify-center gap-4">
                                    <div className="text-center space-y-2">
                                        <div className="px-4 py-2 bg-background rounded-lg shadow-sm border">
                                            <Layers className="h-6 w-6 text-blue-600 dark:text-blue-400 mx-auto mb-1" />
                                            <p className="text-xs font-medium">DOKS</p>
                                        </div>
                                    </div>
                                    <div className="text-center space-y-2">
                                        <div className="px-4 py-2 bg-background rounded-lg shadow-sm border">
                                            <Server className="h-6 w-6 text-purple-600 dark:text-purple-400 mx-auto mb-1" />
                                            <p className="text-xs font-medium">App Platform</p>
                                        </div>
                                    </div>
                                </div>

                                <div className="text-center">
                                    <div className="inline-block px-4 py-2 bg-background rounded-lg shadow-sm border">
                                        <Shield className="h-6 w-6 text-green-600 dark:text-green-400 mx-auto mb-1" />
                                        <p className="text-xs font-medium">Keycloak + OAuth 2.0</p>
                                    </div>
                                </div>

                                <div className="grid grid-cols-3 gap-2">
                                    <div className="text-center p-2 bg-background rounded-lg shadow-sm border">
                                        <p className="text-xs text-muted-foreground">Frontend</p>
                                    </div>
                                    <div className="text-center p-2 bg-background rounded-lg shadow-sm border">
                                        <p className="text-xs text-muted-foreground">Backend</p>
                                    </div>
                                    <div className="text-center p-2 bg-background rounded-lg shadow-sm border">
                                        <p className="text-xs text-muted-foreground">Database</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}
