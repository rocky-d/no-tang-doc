resource "kubernetes_namespace" "external_dns" {
  metadata { name = "external-dns" }
}
resource "kubernetes_secret" "do_token" {
  metadata {
    name      = "do-token"
    namespace = kubernetes_namespace.external_dns.metadata[0].name
  }
  type       = "Opaque"
  data       = { DO_TOKEN = var.do_token } # 自动转 base64
  depends_on = []
}
resource "helm_release" "external_dns" {
  name             = "external-dns"
  namespace        = kubernetes_namespace.external_dns.metadata[0].name
  create_namespace = false
  atomic           = true
  cleanup_on_fail  = true
  timeout          = 60
  repository       = "https://kubernetes-sigs.github.io/external-dns/"
  chart            = "external-dns"
  version          = "1.18.0" # 对应 AppVersion v0.18.0，兼容 K8s 1.33
  depends_on       = [kubernetes_secret.do_token]
  values = [yamlencode({
    provider      = { name = "digitalocean" } # 新 Chart 用 provider.name
    policy        = "upsert-only"
    txtOwnerId    = "ntdoc-doks"
    domainFilters = ["ntdoc.site"]

    # 通过环境变量把 DO_TOKEN 注入（官方教程要求 DO_TOKEN）
    env = [{
      name      = "DO_TOKEN",
      valueFrom = { secretKeyRef = { name = "do-token", key = "DO_TOKEN" } }
    }]
  })]
}