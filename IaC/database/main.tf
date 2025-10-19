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

# Digital Ocean API Token - u could set it in .tfvars file and keep it secret
variable "do_token" {
  description = "Please enter your DigitalOcean API Token"
  type        = string
  sensitive   = true
}