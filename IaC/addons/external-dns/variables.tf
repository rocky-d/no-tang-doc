variable "kubeconfig_path" {
  description = "kubeconfig 文件路径（来自 infra/cluster 输出）"
  type        = string
  default     = "../../cluster/kubeconfig.yaml"
}

variable "do_token" {
  description = "ExternalDNS 使用的 DO Token"
  type        = string
  sensitive   = true
}

