
# Private: docs（预签名直连，不走 CDN）
output "docs_bucket_name" {
  description = "私有文档桶名称"
  value       = digitalocean_spaces_bucket.docs.name
}

output "docs_region" {
  description = "私有文档桶区域"
  value       = digitalocean_spaces_bucket.docs.region
}

output "docs_origin" {
  description = "私有文档桶原始域名（预签名 URL 建议直连此域名）"
  value       = "${digitalocean_spaces_bucket.docs.name}.${digitalocean_spaces_bucket.docs.region}.digitaloceanspaces.com"
}

output "docs_base_url" {
  description = "私有文档桶 HTTPS 基础地址（可拼接对象 Key 使用）"
  value       = "https://${digitalocean_spaces_bucket.docs.name}.${digitalocean_spaces_bucket.docs.region}.digitaloceanspaces.com"
}

output "docs_acl" {
  description = "私有文档桶的 ACL（应为 private）"
  value       = digitalocean_spaces_bucket.docs.acl
}


# Public: cdn（公共静态资源 + CDN）

output "cdn_bucket_name" {
  description = "公共静态资源桶名称"
  value       = digitalocean_spaces_bucket.cdn.name
}

output "cdn_region" {
  description = "公共静态资源桶区域"
  value       = digitalocean_spaces_bucket.cdn.region
}

output "cdn_origin" {
  description = "CDN 回源地址（空间原域名）"
  value       = "${digitalocean_spaces_bucket.cdn.name}.${digitalocean_spaces_bucket.cdn.region}.digitaloceanspaces.com"
}

output "cdn_endpoint" {
  description = "DigitalOcean 分配的 CDN 端点（可直接给前端使用）"
  value       = digitalocean_cdn.assets_cdn.endpoint
}

output "cdn_base_url" {
  description = "CDN HTTPS 基础地址"
  value       = "https://${digitalocean_cdn.assets_cdn.endpoint}"
}

output "cdn_ttl_seconds" {
  description = "CDN 缓存 TTL（秒）"
  value       = digitalocean_cdn.assets_cdn.ttl
}


# CORS / 站点信息（便于核对）

output "frontend_origin_for_cors" {
  description = "为 CORS 放行的前端站点 Origin"
  value       = var.ntdoc_site
}