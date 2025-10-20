resource "helm_release" "cert_manager" {
  name = "cert-manager"
  # 推荐 OCI
  repository       = "oci://quay.io/jetstack/charts"
  chart            = "cert-manager"
  namespace        = var.namespace
  create_namespace = true
  version          = var.chart_version # 建议 pin: v1.19.0

  # Helm 等待安装完成 + 等待 Job 结束（CRD 安装 Hook 会跑 Job）
  wait            = true
  wait_for_jobs   = true
  atomic          = true
  cleanup_on_fail = true

  values = [yamlencode({
    crds = { enabled = var.install_crds } # 新语法！
    # 其他想调的值可放这里
  })]
}

resource "kubernetes_manifest" "cluster_issuer" {
  depends_on = [helm_release.cert_manager]

  manifest = {
    apiVersion = "cert-manager.io/v1"
    kind       = "ClusterIssuer"
    metadata   = { name = var.issuer_name }
    spec = {
      acme = {
        email               = var.email
        server              = var.server
        privateKeySecretRef = { name = var.private_key_secret_name }
        solvers = [
          {
            http01 = {
              ingress = { class = var.ingress_class } # "nginx"
            }
          }
        ]
      }
    }
  }
}

