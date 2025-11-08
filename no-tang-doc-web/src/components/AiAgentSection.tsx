import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Bot, Sparkles, Zap, Brain, MessageSquare, FileSearch, ArrowRight } from "lucide-react";

const aiFeatures = [
    {
        icon: Brain,
        title: "Claude AI Model",
        description: "Powered by Anthropic's Claude, providing advanced natural language understanding and document analysis."
    },
    {
        icon: MessageSquare,
        title: "Intelligent Conversations",
        description: "Ask questions about your documents and get accurate, context-aware answers instantly."
    },
    {
        icon: FileSearch,
        title: "Smart Document Analysis",
        description: "Automatically extract key information, summaries, and insights from your documents."
    },
    {
        icon: Zap,
        title: "MCP Server Integration",
        description: "Built on Model Context Protocol (MCP) Server for seamless AI integration and extensibility."
    }
];

export function AIAgentSection() {
    return (
        <section id="agent" className="py-20 bg-muted/30">
            <div className="container mx-auto px-4">
                <div className="text-center space-y-4 mb-16">
                    <Badge variant="secondary" className="mb-2">
                        <Sparkles className="w-3 h-3 mr-1" />
                        AI-Powered
                    </Badge>
                    <h2>Intelligent AI Agent</h2>
                    <p className="text-muted-foreground max-w-2xl mx-auto">
                        Experience the future of document management with our AI agent powered by Claude and MCP Server.
                    </p>
                </div>

                {/* Main Feature Showcase */}
                <div className="grid lg:grid-cols-2 gap-12 items-center mb-16">
                    <div className="relative order-2 lg:order-1">
                        <div className="absolute inset-0 bg-gradient-to-br from-purple-500/20 via-blue-500/20 to-cyan-500/20 rounded-2xl blur-3xl"></div>
                        <div className="relative bg-background border rounded-2xl p-8 shadow-xl">
                            <div className="space-y-6">
                                <div className="flex items-center gap-3">
                                    <div className="w-10 h-10 bg-gradient-to-br from-purple-500 to-blue-600 rounded-full flex items-center justify-center">
                                        <Bot className="h-6 w-6 text-black" />
                                    </div>
                                    <div>
                                        <h4>NTDoc AI Assistant</h4>
                                        <p className="text-xs text-muted-foreground">Powered by Claude</p>
                                    </div>
                                </div>

                                <div className="space-y-4">
                                    {/* User Message */}
                                    <div className="flex justify-end">
                                        <div className="bg-primary text-primary-foreground rounded-2xl rounded-br-sm px-4 py-3 max-w-[80%]">
                                            <p className="text-sm">Can you summarize the Q4 financial report?</p>
                                        </div>
                                    </div>

                                    {/* AI Response */}
                                    <div className="flex justify-start">
                                        <div className="bg-muted rounded-2xl rounded-bl-sm px-4 py-3 max-w-[80%]">
                                            <p className="text-sm">I've analyzed the Q4 financial report. Here's a summary:</p>
                                            <ul className="text-sm mt-2 space-y-1 list-disc list-inside text-muted-foreground">
                                                <li>Revenue increased by 23%</li>
                                                <li>Operating costs down 12%</li>
                                                <li>Net profit: $2.4M</li>
                                            </ul>
                                        </div>
                                    </div>

                                    {/* User Message */}
                                    <div className="flex justify-end">
                                        <div className="bg-primary text-primary-foreground rounded-2xl rounded-br-sm px-4 py-3 max-w-[80%]">
                                            <p className="text-sm">What were the main cost savings?</p>
                                        </div>
                                    </div>
                                </div>

                                <div className="flex items-center gap-2 pt-4 border-t">
                                    <div className="flex-1 bg-muted rounded-full px-4 py-2 text-sm text-muted-foreground">
                                        Ask me anything about your documents...
                                    </div>
                                    <Button size="sm" className="rounded-full">
                                        <Sparkles className="h-4 w-4" />
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="space-y-6 order-1 lg:order-2">
                        <div className="space-y-4">
                            <h3>Chat with Your Documents</h3>
                            <p className="text-muted-foreground">
                                Our AI agent understands context and can answer complex questions about your documents.
                                Built on cutting-edge technology including Claude AI and MCP Server architecture.
                            </p>
                        </div>

                        <div className="space-y-3">
                            <div className="flex items-center gap-3 p-4 bg-background border rounded-lg">
                                <div className="w-8 h-8 bg-purple-500/10 rounded-lg flex items-center justify-center flex-shrink-0">
                                    <Brain className="h-4 w-4 text-purple-600 dark:text-purple-400" />
                                </div>
                                <div>
                                    <h4 className="text-sm">Claude AI Model</h4>
                                    <p className="text-xs text-muted-foreground">Advanced language understanding</p>
                                </div>
                            </div>

                            <div className="flex items-center gap-3 p-4 bg-background border rounded-lg">
                                <div className="w-8 h-8 bg-blue-500/10 rounded-lg flex items-center justify-center flex-shrink-0">
                                    <Zap className="h-4 w-4 text-blue-600 dark:text-blue-400" />
                                </div>
                                <div>
                                    <h4 className="text-sm">MCP Server Protocol</h4>
                                    <p className="text-xs text-muted-foreground">Seamless AI integration framework</p>
                                </div>
                            </div>

                            <div className="flex items-center gap-3 p-4 bg-background border rounded-lg">
                                <div className="w-8 h-8 bg-green-500/10 rounded-lg flex items-center justify-center flex-shrink-0">
                                    <FileSearch className="h-4 w-4 text-green-600 dark:text-green-400" />
                                </div>
                                <div>
                                    <h4 className="text-sm">Context-Aware Search</h4>
                                    <p className="text-xs text-muted-foreground">Understands document relationships</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* AI Features Grid */}
                <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
                    {aiFeatures.map((feature, index) => (
                        <Card key={index} className="border-0 shadow-sm hover:shadow-md transition-shadow">
                            <CardHeader>
                                <div className="w-12 h-12 bg-gradient-to-br from-purple-500/10 to-blue-500/10 rounded-lg flex items-center justify-center mb-4">
                                    <feature.icon className="h-6 w-6 text-purple-600 dark:text-purple-400" />
                                </div>
                                <CardTitle>{feature.title}</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-sm text-muted-foreground">
                                    {feature.description}
                                </p>
                            </CardContent>
                        </Card>
                    ))}
                </div>
                <h3 className="text-center mb-12"></h3>
                {/* CTA */}
                <div className="text-center mt-12">
                    <Button size="lg" className="gap-2">
                        Try AI Agent
                        <ArrowRight className="h-4 w-4" />
                    </Button>
                </div>
            </div>
        </section>
    );
}
