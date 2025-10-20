# Terraform IaC指南
## 启用DO Spaces作为远程状态存储
在使用Terraform管理基础设施时，建议将状态文件存储在远程存储中，以便团队协作和状态备份。Digital Ocean Spaces可以作为远程状态存
储的后端（backend）。以下是配置步骤：
需要在你已经管理的终端手动注入以下环境变量，以便Terraform能够访问DO Spaces：
https://docs.digitalocean.com/products/spaces/reference/terraform-backend/
bash 命令
``` bash
export AWS_ACCESS_KEY_ID="<ntdoc_do_spaces_access_key_id>"
export AWS_SECRET_ACCESS_KEY="<ntdoc_do_spaces_access_key_secret>"
```
powershell 命令
```powershell
$env:AWS_ACCESS_KEY_ID="<ntdoc_do_spaces_access_key_id>"
$env:AWS_SECRET_ACCESS_KEY="<ntdoc_do_spaces_access_key_secret>"
```
之后该终端操作的Terraform命令行会自动读取这两个环境变量。主要用来作为backend.tf访问和管理DO Spaces中资源对应的.tfstate文件

## 常用命令与参数
### init
初始化当前目录为 Terraform 工作目录：下载 providers、初始化/迁移远程状态（backend）、下载模块。
```powershell
terraform init
```

### validate
静态检查配置文件的语法与内部引用是否正确（不访问云 API，不做变更计算）
```powershell
terraform validate
```

### fmt
格式化 .tf 文件，保证风格一致
```powershell
terraform fmt
```

### plan
计算将要执行的变更，不会实际修改资源
```powershell
# 生成变更计划并保存到 PLANFILE
terraform plan
# 生成销毁计划并保存到 PLANFILE
terraform plan -destroy
```

### apply
实际执行变更（创建/更新/删除），可直接读取上一步的 PLANFILE
```powershell
terraform apply
```
### destroy
```powershell
# 销毁所有由当前配置创建的资源 相当于 terraform plan -destroy + terraform apply
terraform destroy
```

## 文件结构
- main.tf: 主要的资源定义文件
- variables.tf: 变量定义文件
- outputs.tf: 输出定义文件
- backend.tf: 远程状态配置文件
- providers.tf: Provider 配置文件
- terraform.tfvars: 变量值文件（可选）
- terraform.tfstate: 状态文件（自动生成，不要手动修改）-> 已经通过backend.tf同步到DO Spaces进行远程管理
- .terraform/: Terraform 相关文件夹（自动生成，不要手动修改）
- .terraform.lock.hcl: Provider 版本锁定文件（自动生成，不要手动修改）
- .terraform.backup: 状态备份文件（destroy后自动生成，不要手动修改）