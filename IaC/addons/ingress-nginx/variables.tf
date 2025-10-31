variable "kubeconfig_path" {
  description = "kubeconfig 文件路径（来自 infra/cluster 输出）"
  type        = string
  default     = "../../cluster/kubeconfig.yaml"
}

variable "namespace" {
  description = "ingress-nginx 安装命名空间"
  type        = string
  default     = "ingress-nginx"
}

