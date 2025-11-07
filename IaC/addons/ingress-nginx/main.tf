resource "helm_release" "ingress_nginx" {
  name             = "ingress-nginx"
  repository       = "https://kubernetes.github.io/ingress-nginx"
  chart            = "ingress-nginx"
  namespace        = "ingress-nginx"
  create_namespace = true
  version          = "4.13.3"

  values = [yamlencode({
    controller = {
      publishService = { enabled = true }
      service        = { type = "LoadBalancer" }
    }
  })]
}