variable "digitalocean_token" {
  description = "DigitalOcean API token (敏感信息)"
  type        = string
  sensitive   = true
}
variable "kubeconfig_path" {
  type    = string
  default = "../cluster/kubeconfig.yaml"
}
# 相对路径
variable "namespace" {
  type    = string
  default = "dev"
}
variable "secret_name" {
  type    = string
  default = "docr-pull"
}

# 从 registry stack 传入（或直接写死）
variable "registry_name" {
  type    = string
  default = "ntdoc"
}

