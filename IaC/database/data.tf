data "terraform_remote_state" "cluster" {
  backend = "s3"

  config = {
    bucket = "ntdoc-tfstate-spaces"
    key    = "ntdoc/doks/doks.tfstate"
    region = "sgp1"

    # DigitalOcean Spaces endpoint
    endpoints = {
      s3 = "https://sgp1.digitaloceanspaces.com"
    }

    skip_credentials_validation = true
    skip_requesting_account_id  = true
    skip_metadata_api_check     = true
    skip_region_validation      = true
    skip_s3_checksum            = true

    # Spaces 访问凭证（必须）
    access_key = var.do_spaces_access_key
    secret_key = var.do_spaces_secret_key
  }
}

variable "do_spaces_access_key" {
  description = "DigitalOcean Spaces access key"
  type        = string
  sensitive   = true
}

variable "do_spaces_secret_key" {
  description = "DigitalOcean Spaces secret key"
  type        = string
  sensitive   = true
}