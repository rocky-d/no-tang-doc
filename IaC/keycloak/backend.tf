terraform {
  required_version = ">= 1.6.3" # DO 文档要求的最低版本

  backend "s3" {
    endpoints = {
      s3 = "https://sgp1.digitaloceanspaces.com" # 例: https://sgp1.digitaloceanspaces.com
    }

    bucket = "ntdoc-tfstate-spaces"
    key    = "ntdoc/keycloak/keycloak.tfstate" # 文件名和文件路径，可自定义

    # 关闭 AWS 特有校验（Spaces 是 S3 兼容）
    skip_credentials_validation = true
    skip_requesting_account_id  = true
    skip_metadata_api_check     = true
    skip_region_validation      = true
    skip_s3_checksum            = true
    region                      = "sgp1" # 仅占位用于通过校验

    # 推荐开启锁（Terraform 1.11+）
    # 防止多人同时操作破坏 state
    use_lockfile = true
  }
}
