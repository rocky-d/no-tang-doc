terraform {
  required_version = ">= 1.5.0"
  required_providers {
    digitalocean = {
      source  = "digitalocean/digitalocean"
      version = ">= 2.38.0"
    }
  }
}

provider "digitalocean" {
  token = var.digitalocean_token
}

# 方式 A：创建一个新的 DOCR（如果还没有）
resource "digitalocean_container_registry" "this" {
  name                   = var.registry_name
  region                 = var.registry_region
  subscription_tier_slug = "basic"
}

# 方式 B：如果你已经在控制台里创建了，就用 data 源读取（与上面二选一）
# data "digitalocean_container_registry" "this" {
#   name = var.registry_name
# }

variable "digitalocean_token" {
  description = "DigitalOcean API token (敏感信息)"
  type        = string
  sensitive   = true
}

variable "registry_name" {
  type    = string
  default = "ntdoc"
}

variable "registry_region" {
  type    = string
  default = "sgp1"
}
