$backendPath = "backend"
$frontendPath = "frontend"

# 启动后端
Write-Host "启动后端服务..."
Start-Process powershell -ArgumentList "cd $backendPath; java -jar app.jar" -NoNewWindow

# 启动前端
Write-Host "安装前端依赖并启动前端服务..."
Start-Process powershell -ArgumentList "cd $frontendPath; npm install; npm run dev" -NoNewWindow

Write-Host "启动成功，请访问 http://localhost:3001 进行查看"
