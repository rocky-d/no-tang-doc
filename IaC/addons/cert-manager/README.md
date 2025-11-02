# 需要分两步terraform apply
第一步创建cert-manager和 CRD资源
`terraform apply -target="helm_release.cert_manager"`
第二步在 `terraform apply` 创建ClusterIssuer