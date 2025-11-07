resource "digitalocean_spaces_bucket" "docs" {
  name   = "ntdoc-docs"
  region = "sgp1"
  acl    = "private"
  # 开启版本控制（用于误删回滚）
  versioning {
    enabled = true
  }
  # 如需在 destroy 时自动清桶（有风险）
  force_destroy = false
}


resource "digitalocean_spaces_bucket" "cdn" {
  name   = "ntdoc-cdn"
  region = "sgp1"
  acl    = "public-read"
  # 开启版本控制（用于误删回滚）
  versioning {
    enabled = true
  }
  # 如需在 destroy 时自动清桶（有风险）
  force_destroy = false
}

# 让 Terraform 管理 Spaces 的 CORS（独立资源）
resource "digitalocean_spaces_bucket_cors_configuration" "cors" {
  bucket = digitalocean_spaces_bucket.cdn.name
  region = digitalocean_spaces_bucket.cdn.region

  # 供前端 APP 获取静态资源或预签名 GET
  cors_rule {
    allowed_origins = [var.ntdoc_site]           # 前端域名
    allowed_methods = ["GET", "HEAD"] # 读取常用 不要加OPTIONS 会报错
    allowed_headers = ["*"]
    max_age_seconds = 3000
  }

  # 若以后支持浏览器直传（预签名 PUT/POST），再加这个：
  # cors_rule {
  #   allowed_origins = [var.ntdoc_site]
  #   allowed_methods = ["PUT", "POST", "DELETE", "HEAD"]
  #   allowed_headers = ["*"]
  #   max_age_seconds = 3000
  # }
}

# 开启 CDN（origin 指向 Space FQDN）
resource "digitalocean_cdn" "assets_cdn" {
  origin = "${digitalocean_spaces_bucket.cdn.name}.${digitalocean_spaces_bucket.cdn.region}.digitaloceanspaces.com"
  ttl    = 3600
}

variable "ntdoc_site" {
  description = "NTDoc 站点域名"
  type        = string
  default     = "https://www.ntdoc.site"
}

variable "do_token" {
  description = "Please enter your DigitalOcean API Token"
  type        = string
  sensitive   = true
}

variable "spaces_access_id" {
  description = "Please enter your DigitalOcean API Token"
  type        = string
  default     = "DO00WG9THD7G4C9YRBFT"
}

variable "spaces_secret_key" {
  description = "Please enter your Space Access Key Secret"
  type        = string
  sensitive   = true
}