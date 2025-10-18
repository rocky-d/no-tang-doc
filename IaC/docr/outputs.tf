output "registry_name" {
  value = digitalocean_container_registry.this.name
  # value = data.digitalocean_container_registry.this.name
  # 如果用 是新建的Resource 源，改成 digitalocean_container_registry.this.name
}
