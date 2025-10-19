# main.tf for infra/cluster

# Query available Kubernetes versions on DO
data "digitalocean_kubernetes_versions" "this" {}

locals {
  doks_version = var.kubernetes_version != "" ? var.kubernetes_version : coalesce(
    data.digitalocean_kubernetes_versions.this.latest_version,
    element(data.digitalocean_kubernetes_versions.this.valid_versions, 0)
  )
}

# Create DOKS cluster
resource "digitalocean_kubernetes_cluster" "this" {
  name    = var.cluster_name
  region  = var.region
  version = local.doks_version

  node_pool {
    name       = "ntdoc-pool"
    size       = var.node_size
    # 如果auto_scale为true，则不需要设置node_count
    # node_count = var.node_count
    auto_scale = true
    max_nodes = var.max_nodes
    min_nodes = var.min_nodes
  }
}

# Read cluster details (includes kubeconfig)
data "digitalocean_kubernetes_cluster" "this" {
  name       = digitalocean_kubernetes_cluster.this.name
  depends_on = [digitalocean_kubernetes_cluster.this]
}

# Write kubeconfig to a file for consumers (addons)
resource "local_file" "kubeconfig" {
  content  = data.digitalocean_kubernetes_cluster.this.kube_config[0].raw_config
  filename = "${path.module}/kubeconfig.yaml"
}

