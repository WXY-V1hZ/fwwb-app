# 浓烟环境下人体目标判别系统

## 📌 项目简介

本项目为服务外包大赛参赛作品，旨在为消防员提供一个**浓烟环境下的人体目标智能识别系统**。系统集成了去烟算法与双视频流处理机制（红外相机 + 热成像相机），可对视频流中的人体目标进行识别与置信度判别，并通过直观的用户界面实时展示处理结果。

- **输入形式**：红外 + 热成像双视频流（并行）
- **输出形式**：融合后的视频流，包含目标检测框与置信度标注
- **核心能力**：浓烟环境下的人体检测与可视化结果输出

## 🚀 快速启动

### ✅ 环境依赖

- Node.js (建议版本 ≥ 16)
- openjdk17
- ffmpeg

## ▶️ 启动项目

获取源代码

```bash
git clone git@github.com:WXY-V1hZ/fwwb-app.git
cd fwwb-app
```

在项目根目录下直接运行以下命令，一键启动前后端服务：

```bash
# Windows
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
startup.ps1

# Linux
chmod +x start.sh
startup.sh
```

打开浏览器访问 `localhost:3001` 即可体验系统。

## 🛠️ 技术栈

- **前端**：Vue 3 + Vite + Axios
    
- **后端**：SpringBoot + 多视频流处理
    
- **通信方式**：REST API + 静态资源服务
    
