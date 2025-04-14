#!/bin/bash

# 设置后端和前端路径
backendPath="backend"
frontendPath="frontend"

# 启动后端服务
echo "启动后端服务..."
cd $backendPath || exit
nohup java -jar app.jar &

# 启动前端服务
echo "安装前端依赖并启动前端服务..."
cd $frontendPath || exit
npm install
npm run dev &

# 提示启动成功
echo "启动成功，请访问 http://localhost:3001 进行查看"
