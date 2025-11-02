variable "kubeconfig_path" {
  description = "kubeconfig 文件路径（来自 infra/cluster 输出）"
  type        = string
  default     = "../../cluster/kubeconfig.yaml"
}

variable "namespace" {
  description = "cert-manager 安装命名空间"
  type        = string
  default     = "cert-manager"
}

variable "chart_version" {
  description = "cert-manager chart 版本"
  type        = string
  default     = "v1.19.0"
}

variable "install_crds" {
  description = "是否安装 CRDs（生产建议保留 true）"
  type        = bool
  default     = true
}

variable "issuer_name" {
  description = "ClusterIssuer 名称"
  type        = string
  default     = "letsencrypt"
}

variable "email" {
  description = "ACME 账户邮箱"
  type        = string
  default     = "dohnadohnacn@gmail.com"
}

variable "server" {
  description = "ACME 服务器（生产：Let's Encrypt prod；测试可用 staging）"
  type        = string
  default     = "https://acme-v02.api.letsencrypt.org/directory"
}

variable "private_key_secret_name" {
  description = "保存 ACME 私钥的 Secret 名称"
  type        = string
  default     = "acme-account-key"
}

variable "ingress_class" {
  description = "HTTP-01 Solver 使用的 Ingress Class"
  type        = string
  default     = "nginx"
}


