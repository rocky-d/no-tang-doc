# 获取连接信息!!!
数据库集群的连接信息存放在/tfstate文件中, 并且已经交由远端DO Spaces管理, 直接前往使用Api获取该文件是不推荐的做法
请在`terraform apply`之后使用 `terraform output -json` 命令获取连接信息关键敏感字段 如:uri, host, port, username, password, database

# 配置公网连接!!!
数据库集群默认开启了Public Network访问, 但是需要在防火墙中添加允许的IP地址段白名单, 否则无法通过公网访问数据库
可以自行进入Control Panel UI -> Databases -> ntdoc-mysql-cluster -> Settings -> Trusted sources进行配置
可以配置本机IP或配置DO中的其他资源如Droplets DOKS集群

# Liquibase注意事项
Digital Ocean MySql集群默认会开启sql_require_primary_key = true, 这要求数据库的每张表都需要有主键PK, 
但是当我们在使用Liquibase进行数据库管理的时候, 会默认为我们初始化DATABASECHANGELOG表用来记录运行过的changelog, 
这张表是没有主键的, 虽然它有ID, 因此在启动的时候会报错SQLERROR
因此我们需要使用Terraform创建一个config资源, 并在其中为集群关闭sql_require_primary_key参数

**需要注意的是, sql_require_primary_key是无法通过Digital Ocean Control Panel UI进行修改的, 只能通过API或者Terraform进行修改**

## 使用API关闭sql_require_primary_key参数
```powershell
curl -X PATCH -H "Content-Type: application/json" -H "Authorization: Bearer <YOUR_DIGITAL_OCEAN_TOKEN>" -d '{"config": {"sql_require_primary_key": false}}' "https://api.digitalocean.com/v2/databases/<YOUR_CLUSTER_ID>/config"
```

## 使用Terraform创建config资源 -> main.tf
```hcl
# Based on Liquibase requirement, we need to disable sql_require_primary_key via config
# https://docs.digitalocean.com/reference/terraform/reference/resources/database_mysql_config/
resource "digitalocean_database_mysql_config" "mysql_cfg" {
  cluster_id               = digitalocean_database_cluster.ntdoc-mysql-cluster.id
  sql_require_primary_key  = false
}
```