# 命名空间
resource "kubernetes_namespace" "ns" {
  metadata { name = var.namespace }
}

# 生成 dockerconfigjson（只读足够）
resource "digitalocean_container_registry_docker_credentials" "creds" {
  registry_name = var.registry_name
}

# 写入 K8s Secret（imagePullSecret）
resource "kubernetes_secret" "pull" {
  metadata {
    name      = var.secret_name
    namespace = kubernetes_namespace.ns.metadata[0].name
  }
  type = "kubernetes.io/dockerconfigjson"
  data = {
    ".dockerconfigjson" = digitalocean_container_registry_docker_credentials.creds.docker_credentials
  }
}

output "image_pull_secret_name" {
  value = kubernetes_secret.pull.metadata[0].name
}
