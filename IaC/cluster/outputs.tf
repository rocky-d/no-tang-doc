# outputs.tf for infra/cluster

output "kubeconfig_raw" {
  description = "集群 kubeconfig 内容（敏感）"
  value       = data.digitalocean_kubernetes_cluster.this.kube_config[0].raw_config
  sensitive   = true
}

output "kubeconfig_path" {
  description = "本地写入的 kubeconfig 文件路径（供 addons 使用）"
  value       = local_file.kubeconfig.filename
}

output "cluster_name" {
  description = "集群名称"
  value       = digitalocean_kubernetes_cluster.this.name
}

output "actual_node_count" {
  description = "集群实际节点数量"
  value       = digitalocean_kubernetes_cluster.this.node_pool[0].actual_node_count
}

output "doks_cluster_id" {
  value = digitalocean_kubernetes_cluster.this.id
}

