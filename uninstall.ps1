# 卸载 Node.js
scoop uninstall nodejs

# 卸载 OpenJDK 17
scoop uninstall openjdk17

# 删除 Scoop 安装目录
Remove-Item -Recurse -Force "$env:USERPROFILE\scoop"

# 删除 Scoop 配置文件
Remove-Item -Recurse -Force "$env:USERPROFILE\AppData\Local\scoop"
Remove-Item -Recurse -Force "$env:USERPROFILE\AppData\Roaming\scoop"
Remove-Item -Recurse -Force "$env:USERPROFILE\scoop.sh"

