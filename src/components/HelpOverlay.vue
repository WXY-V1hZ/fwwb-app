<template>
    <div class="help-button" @click="toggleHelp" :class="{ 'active': isHelpVisible }">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
        <line x1="12" y1="17" x2="12.01" y2="17"></line>
      </svg>
    </div>
    
    <Teleport to="body">
      <div class="help-overlay" v-if="isHelpVisible" @click="closeOnOverlayClick">
        <div class="help-content fade-in" @click.stop>
          <div class="help-header">
            <h2>使用帮助</h2>
            <button class="close-help-button" @click="toggleHelp">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
          
          <div class="help-sections">
            <div class="help-section">
              <h3>项目介绍</h3>
              <p>本系统旨在解决浓烟环境下人体目标难以识别的问题，通过结合红外图像和热成像图像，借助深度学习算法实现准确的人体目标检测，为消防救援工作提供技术支持。</p>
            </div>
            
            <div class="help-section">
              <h3>图像上传</h3>
              <p>系统需要同时上传红外图像和热成像图像才能进行处理。点击左侧控制面板中的"选择文件"按钮可以上传相应图像。</p>
              <div class="help-tips">
                <div class="tip-item">
                  <div class="tip-icon">提示</div>
                  <div class="tip-text">支持常见图像格式：JPG、PNG、JPEG等</div>
                </div>
              </div>
            </div>
            
            <div class="help-section">
              <h3>处理流程</h3>
              <ol class="help-list">
                <li>上传红外图像和热成像图像</li>
                <li>点击"开始处理"按钮</li>
                <li>等待处理完成（进度条显示处理进度）</li>
                <li>查看处理结果，带有人体目标标记的图像将显示在右侧输出区域</li>
              </ol>
            </div>
            
            <div class="help-section">
              <h3>历史记录</h3>
              <p>系统会自动保存处理结果到本地，您可以在左侧控制面板的历史记录区域查看和加载之前处理过的图像。</p>
              <div class="help-tips">
                <div class="tip-item">
                  <div class="tip-icon">提示</div>
                  <div class="tip-text">历史记录保存在浏览器本地存储中，清除浏览器数据可能会导致历史记录丢失</div>
                </div>
              </div>
            </div>
            
            <div class="help-section">
              <h3>控制面板</h3>
              <p>您可以通过点击左侧控制面板顶部的切换按钮来展开或收起控制面板，以获得更大的显示空间。</p>
            </div>
            
            <div class="help-section">
              <h3>技术原理</h3>
              <p>本系统采用深度学习目标检测算法，通过分析红外图像和热成像图像的特征，实现对浓烟环境中人体目标的精确识别和定位。系统会在输出图像中绘制人体目标的边界框并显示检测置信度。</p>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue';
  
  const isHelpVisible = ref(false);
  
  function toggleHelp() {
    isHelpVisible.value = !isHelpVisible.value;
  }
  
  function closeOnOverlayClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('help-overlay')) {
      isHelpVisible.value = false;
    }
  }
  </script>
  
  <style scoped>
  .help-button {
    position: fixed;
    top: 100px;
    right: 20px;
    width: 42px;
    height: 42px;
    border-radius: 50%;
    background-color: var(--primary-color);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 100;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;
  }
  
  .help-button:hover, .help-button.active {
    background-color: var(--dark-color);
    transform: rotate(15deg);
  }
  
  .help-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1001;
    backdrop-filter: blur(3px);
  }
  
  .help-content {
    background-color: white;
    border-radius: 10px;
    width: 90%;
    max-width: 700px;
    max-height: 80vh;
    display: flex;
    flex-direction: column;
    box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);
    overflow: hidden;
  }
  
  .help-header {
    background: linear-gradient(135deg, var(--accent-color) 0%, #e07e00 100%);
    color: white;
    padding: 16px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 2px solid #ffaa33;
  }
  
  .help-header h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
  
  .close-help-button {
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    padding: 5px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: background-color 0.2s;
  }
  
  .close-help-button:hover {
    background-color: rgba(255, 255, 255, 0.2);
  }
  
  .help-sections {
    padding: 20px;
    overflow-y: auto;
    max-height: calc(80vh - 60px);
  }
  
  .help-section {
    margin-bottom: 24px;
  }
  
  .help-section h3 {
    color: var(--primary-color);
    margin: 0 0 12px 0;
    font-size: 18px;
    font-weight: 600;
    border-bottom: 1px solid var(--border-color);
    padding-bottom: 8px;
  }
  
  .help-section p {
    margin: 0 0 12px 0;
    line-height: 1.6;
    color: var(--text-secondary);
  }
  
  .help-list {
    margin: 10px 0;
    padding-left: 24px;
  }
  
  .help-list li {
    margin-bottom: 8px;
    color: var(--text-secondary);
    line-height: 1.5;
  }
  
  .help-tips {
    background-color: var(--light-color);
    border-radius: 6px;
    padding: 12px;
    margin-top: 10px;
  }
  
  .tip-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
  }
  
  .tip-icon {
    background-color: var(--accent-color);
    color: white;
    font-size: 12px;
    font-weight: bold;
    padding: 2px 8px;
    border-radius: 4px;
  }
  
  .tip-text {
    color: var(--text-secondary);
    font-size: 14px;
    line-height: 1.5;
  }
  
  @media (max-width: 768px) {
    .help-button {
      top: 70px;
      right: 15px;
      width: 36px;
      height: 36px;
    }
    
    .help-content {
      width: 95%;
      max-height: 85vh;
    }
  }
  </style>