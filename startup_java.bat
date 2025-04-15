@echo off
echo Starting FFWB Smoke Detection...

# 启动后台服务
start powershell -NoExit -Command "cd backend; java -jar app.jar"

# 启动前端服务
start powershell -NoExit -Command "cd frontend; npm install; npm run dev"

# 打开浏览器
start http://127.0.0.1:3001

echo Application started!
