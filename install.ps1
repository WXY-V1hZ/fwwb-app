# 设置执行策略
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force

# 安装 Scoop
Invoke-RestMethod -Uri https://get.scoop.sh | Invoke-Expression

# 添加 java bucket
scoop bucket add java

# 使用 Scoop 安装 Node.js
scoop install nodejs

# 使用 Scoop 安装 OpenJDK 17
scoop install openjdk17

# 重置 OpenJDK 17 到最新版本
scoop reset openjdk17
