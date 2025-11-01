terraform {
  required_providers {
    digitalocean = {
      source  = "digitalocean/digitalocean"
      version = "~> 2.37"
    }
  }
}
# We need digitalocean provider to create database cluster
provider "digitalocean" {
  token = var.do_token
}
# Create mysql cluster with name "ntdoc-mysql-cluster"
resource "digitalocean_database_cluster" "ntdoc-mysql-cluster" {
  name       = "ntdoc-mysql-cluster"
  engine     = "mysql"
  version    = "8"
  size       = "db-s-1vcpu-2gb" # can not be the minimum db-s-1vcpu-1gb at least 2gb for high availability or it will be rejected
  region     = "sgp1"
  node_count = 2 # node_count >=2 will automatically enable high availability
}
# Create database "ntdoc" in the mysql cluster
resource "digitalocean_database_db" "ntdoc" {
  cluster_id = digitalocean_database_cluster.ntdoc-mysql-cluster.id
  name       = "ntdoc"
}
# Based on Liquibase requirement, we need to disable sql_require_primary_key via config
# https://docs.digitalocean.com/reference/terraform/reference/resources/database_mysql_config/
resource "digitalocean_database_mysql_config" "mysql_cfg" {
  cluster_id              = digitalocean_database_cluster.ntdoc-mysql-cluster.id
  sql_require_primary_key = false
}

resource "digitalocean_database_cluster" "keycloak-postgres-cluster" {
  name       = "ntdoc-postgres-cluster"
  engine     = "pg"
  version    = "16"             # 建议使用最新稳定版 PostgreSQL
  size       = "db-s-1vcpu-1gb" # 对 Keycloak 足够；若 Realm 较多可改 2GB
  region     = "sgp1"
  node_count = 1 # 1 节点即可；生产环境建议 >= 2 开启 HA
}

# 创建数据库 ntdoc_keycloak
resource "digitalocean_database_db" "ntdoc_keycloak" {
  cluster_id = digitalocean_database_cluster.keycloak-postgres-cluster.id
  name       = "keycloak"
}

resource "digitalocean_database_firewall" "pg_fw" {
  cluster_id = digitalocean_database_cluster.keycloak-postgres-cluster.id
  rule {
    type  = "k8s"
    value = data.terraform_remote_state.cluster.outputs.doks_cluster_id # 传入你的 DOKS 集群ID
  }
}

resource "digitalocean_database_firewall" "mysql_fw" {
  cluster_id = digitalocean_database_cluster.ntdoc-mysql-cluster.id
  rule {
    type  = "k8s"
    value = data.terraform_remote_state.cluster.outputs.doks_cluster_id # 传入你的 DOKS 集群ID
  }
}


# Digital Ocean API Token - u could set it in .tfvars file and keep it secret
variable "do_token" {
  description = "Please enter your DigitalOcean API Token"
  type        = string
  sensitive   = true
}
