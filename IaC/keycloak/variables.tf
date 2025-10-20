variable "do_spaces_access_key" {
  description = "DigitalOcean Spaces access key"
  type        = string
  sensitive   = true
}

variable "do_spaces_secret_key" {
  description = "DigitalOcean Spaces secret key"
  type        = string
  sensitive   = true
}

variable "kc_admin_password" {
  description = "Keycloak admin password"
  type        = string
  sensitive   = true
}
variable "kubeconfig_path" {
  description = "kubeconfig 文件路径（来自 infra/cluster 输出）"
  type        = string
  default     = "../cluster/kubeconfig.yaml"
}