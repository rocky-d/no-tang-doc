import { Card, CardContent } from "./ui/card";
import { Badge } from "./ui/badge";
import { Mail, Users, Target, Lightbulb } from "lucide-react";
import { ImageWithFallback } from "./figma/ImageWithFallback";

const teamMembers = [
    {
        name: "Xin Yuchen",
        role: "FE & DevSecOps Tech Lead",
        bio: "Work on Gen-AI FE & IaC & CICD"
    },
    {
        name: "Zhong Yi",
        role: "BE Tech Lead & PM",
        bio: "Work on Spring Boot MicroServices and Project Management"
    },
    {
        name: "Du HanTian",
        role: "AI Tech Lead & FE Developer",
        bio: "Focus on AI Agent & MCP Server Integration"
    },
    {
        name: "Kong Yikai",
        role: "Senior BE Developer & DBA",
        bio: "Work on building RESTful APIs with Spring Boot and optimizing database performance."
    },
    {
        name: "Chen Sirui",
        role: "Senior BE Developer",
        bio: "SpringBoot specialist"
    },
    {
        name: "Song Jinze",
        role: "Senior BE Developer",
        bio: "SpringBoot specialist"
    }
];

const values = [
    {
        icon: Target,
        title: "Mission-Driven",
        description: "We're committed to simplifying document management for teams worldwide."
    },
    {
        icon: Lightbulb,
        title: "Innovation First",
        description: "Constantly exploring new technologies to deliver cutting-edge solutions."
    },
    {
        icon: Users,
        title: "Customer-Centric",
        description: "Every decision we make is guided by our users' needs and feedback."
    }
];

export function AboutSection() {
    return (
        <section id="about" className="py-20 bg-background">
            <div className="container mx-auto px-4">
                {/* Header */}
                <div className="text-center space-y-4 mb-16">
                    <h2>Meet Our Team</h2>
                    <p className="text-muted-foreground max-w-2xl mx-auto">
                        We're a passionate group of engineers, designers, and problem-solvers dedicated to revolutionizing document management.
                    </p>
                </div>

                {/* Story Section */}
                <div className="grid lg:grid-cols-2 gap-12 items-center mb-20">
                    <div className="space-y-6">
                        <Badge variant="secondary" className="mb-2">Our Story</Badge>
                        <h3>Building the Future of Document Management</h3>
                        <div className="space-y-4 text-muted-foreground">
                            <p>
                                Founded in 2025, NTDoc Repo was born from a simple NUS ISS SE School Project: teams were struggling
                                with fragmented document management solutions that didn't talk to each other.
                            </p>
                            <p>
                                We set out to build a platform that combines powerful features with an intuitive
                                interface, backed by enterprise-grade security and scalability. Today, we're proud
                                to serve teams of all sizes with our cloud-native, AI-powered solution.
                            </p>
                            <p>
                                Our commitment to innovation drives us to continuously improve and adapt to the
                                evolving needs of modern workplaces.
                            </p>
                        </div>
                    </div>

                    <div className="relative">
                        <ImageWithFallback
                            src="https://images.unsplash.com/photo-1522071820081-009f0129c71c?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&w=800"
                            alt="Team collaboration"
                            className="rounded-2xl shadow-xl w-full h-auto"
                        />
                    </div>
                </div>

                {/* Team Members */}
                <div>
                    <h3 className="text-center mb-12"></h3>
                    <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
                        {teamMembers.map((member, index) => (
                            <Card key={index} className="border-0 shadow-sm hover:shadow-md transition-shadow">
                                <CardContent className="pt-6">
                                    <div className="space-y-3">
                                        <div>
                                            <h4>{member.name}</h4>
                                            <p className="text-sm text-primary">{member.role}</p>
                                        </div>
                                        <p className="text-sm text-muted-foreground">{member.bio}</p>
                                    </div>
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                </div>


            </div>
        </section>
    );
}
