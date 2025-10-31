# 基本信息
output "mysql_conn" {
  value = {
    id           = digitalocean_database_cluster.ntdoc-mysql-cluster.id
    host         = digitalocean_database_cluster.ntdoc-mysql-cluster.host
    private_host = digitalocean_database_cluster.ntdoc-mysql-cluster.private_host
    port         = digitalocean_database_cluster.ntdoc-mysql-cluster.port
    uri          = digitalocean_database_cluster.ntdoc-mysql-cluster.uri
    database     = digitalocean_database_db.ntdoc.name
    user         = digitalocean_database_cluster.ntdoc-mysql-cluster.user
    password     = digitalocean_database_cluster.ntdoc-mysql-cluster.password

  }
  sensitive = true
}

output "keycloak_db_connection" {
  description = "Connection parameters for Keycloak PostgreSQL"
  value = {
    host     = digitalocean_database_cluster.keycloak-postgres-cluster.private_host
    port     = 25060
    database = digitalocean_database_db.ntdoc_keycloak.name
    user     = "doadmin"
    password = digitalocean_database_cluster.keycloak-postgres-cluster.password
  }
  sensitive = true
}