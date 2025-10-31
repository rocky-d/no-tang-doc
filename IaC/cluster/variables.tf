# variables.tf for infra/cluster

variable "digitalocean_token" {
  description = "DigitalOcean API token (敏感信息)"
  type        = string
  sensitive   = true
}

variable "cluster_name" {
  type        = string
  default     = "ntdoc-doks"
  description = "DOKS 集群名称"
}

variable "region" {
  type    = string
  default = "sgp1"
}

variable "kubernetes_version" {
  type        = string
  default     = "" # 留空则自动使用 DO 最新支持版本
  description = "DigitalOcean Kubernetes (DOKS) 版本（留空自动选最新）"
}

variable "node_size" {
  type    = string
  default = "s-2vcpu-4gb"
}

variable "node_count" {
  type    = number
  default = 2
}

variable "max_nodes" {
  type    = number
  default = 2
}

variable "min_nodes" {
  type    = number
  default = 1
}

