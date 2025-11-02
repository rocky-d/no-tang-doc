import { FileText, Github } from "lucide-react";

export function Footer() {
  return (
    <footer className="bg-muted/50 border-t">
      <div className="container mx-auto px-4 py-12">
        <div className="grid md:grid-cols-4 gap-8">
          <div className="space-y-4">
            <div className="flex items-center space-x-2">
              <FileText className="h-6 w-6 text-primary" />
              <span className="font-semibold">NTDoc</span>
            </div>
            <p className="text-sm text-muted-foreground">
              Your secure digital document repository for modern teams and individuals.
            </p>
            <div className="flex space-x-4">
              <a href="https://github.com/rocky-d/no-tang-doc" className="text-muted-foreground hover:text-foreground transition-colors">
                <Github className="h-5 w-5" />
              </a>
              {/*<a href="#" className="text-muted-foreground hover:text-foreground transition-colors">*/}
              {/*  <Twitter className="h-5 w-5" />*/}
              {/*</a>*/}
              {/*<a href="#" className="text-muted-foreground hover:text-foreground transition-colors">*/}
              {/*  <Linkedin className="h-5 w-5" />*/}
              {/*</a>*/}
            </div>
          </div>

          <div>
            <h3 className="font-medium mb-4">Product</h3>
            <ul className="space-y-2 text-sm">
              <li><a href="/#features">Features</a></li>
              <li><a href="/#performance">Performance</a></li>
              <li><a href="/#agent">AI Agent</a></li>
              <li><a href="http://api.ntdoc.site/swagger-ui.html" target="_blank" rel="noopener noreferrer">API</a></li>
            </ul>
          </div>

          <div>
            <h3 className="font-medium mb-4">Support</h3>
            <ul className="space-y-2 text-sm">
              <li><a href="mailto:chendoshowcn@gmail.com">Contact Us</a></li>
            </ul>
          </div>

          <div>
            <h3 className="font-medium mb-4">Company</h3>
            <ul className="space-y-2 text-sm">
              <li><a href="/#about">About</a></li>
            </ul>
          </div>
        </div>

        <div className="border-t mt-8 pt-8 text-center">
          <p className="text-sm text-muted-foreground">
            © 2025 NTDoc. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
}