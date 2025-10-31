terraform {
  required_version = ">= 1.5.0"
  required_providers {
    digitalocean = {
      source  = "digitalocean/digitalocean"
      version = ">= 2.38.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = ">= 2.27.0"
    }
  }
}
provider "digitalocean" {
  token = var.digitalocean_token
}
provider "kubernetes" {
  config_path = var.kubeconfig_path
}