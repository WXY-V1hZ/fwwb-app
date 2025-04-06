<template>
    <Teleport to="body">
      <div class="welcome-overlay" v-if="isVisible" @click="closeOnOverlayClick">
        <div class="welcome-modal fade-in" @click.stop>
          <div class="welcome-header">
            <h2>欢迎使用浓烟环境人体目标判别系统</h2>
            <button class="close-button" @click="close">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
          
          <div class="welcome-content">
            <div class="welcome-steps">
              <div class="step">
                <div class="step-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="17 8 12 3 7 8"></polyline>
                    <line x1="12" y1="3" x2="12" y2="15"></line>
                  </svg>
                </div>
                <div class="step-content">
                  <h3>1. 上传图片</h3>
                  <p>选择并上传红外图像和热成像图像，两种图像均需上传才能进行处理。</p>
                </div>
              </div>
              
              <div class="step">
                <div class="step-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
                  </svg>
                </div>
                <div class="step-content">
                  <h3>2. 开始处理</h3>
                  <p>点击"开始处理"按钮，系统将自动分析上传的图像并判别浓烟环境中的人体目标。</p>
                </div>
              </div>
              
              <div class="step">
                <div class="step-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                    <circle cx="8.5" cy="8.5" r="1.5"></circle>
                    <polyline points="21 15 16 10 5 21"></polyline>
                  </svg>
                </div>
                <div class="step-content">
                  <h3>3. 查看结果</h3>
                  <p>处理完成后，系统将在右侧显示带有人体目标标记的结果图像。</p>
                </div>
              </div>
              
              <div class="step">
                <div class="step-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <polyline points="12 6 12 12 16 14"></polyline>
                  </svg>
                </div>
                <div class="step-content">
                  <h3>4. 历史记录</h3>
                  <p>系统会自动保存您的处理结果，方便您随时查看历史记录或重新加载之前的结果。</p>
                </div>
              </div>
            </div>
          </div>
          
          <div class="welcome-footer">
            <label class="dont-show-again">
              <input type="checkbox" v-model="dontShowAgain" />
              <span>不再显示</span>
            </label>
            <button class="start-button" @click="close">开始使用</button>
          </div>
        </div>
      </div>
    </Teleport>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted } from 'vue';
  
  const isVisible = ref(true);
  const dontShowAgain = ref(false);
  
  onMounted(() => {
    // 检查本地存储中是否已设置不再显示欢迎页
    const hasSeenWelcome = localStorage.getItem('smoke-detection-welcome-seen');
    if (hasSeenWelcome === 'true') {
      isVisible.value = false;
    }
  });
  
  function close() {
    isVisible.value = false;
    
    // 如果用户选择不再显示，则保存到本地存储
    if (dontShowAgain.value) {
      localStorage.setItem('smoke-detection-welcome-seen', 'true');
    }
  }
  
  function closeOnOverlayClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('welcome-overlay')) {
      close();
    }
  }
  </script>
  
  <style scoped>
  .welcome-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    backdrop-filter: blur(3px);
  }
  
  .welcome-modal {
    background-color: white;
    border-radius: 10px;
    width: 90%;
    max-width: 600px;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
    box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);
    overflow: hidden;
  }
  
  .welcome-header {
    background: linear-gradient(135deg, #0284b9 0%, #015f86 100%);
    color: white;
    padding: 16px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 2px solid #03a9f4;
  }
  
  .welcome-header h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }
  
  .close-button {
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
  
  .close-button:hover {
    background-color: rgba(255, 255, 255, 0.2);
  }
  
  .welcome-content {
    padding: 24px;
    overflow-y: auto;
    flex-grow: 1;
  }
  
  .welcome-steps {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  
  .step {
    display: flex;
    gap: 16px;
    align-items: flex-start;
  }
  
  .step-icon {
    color: var(--primary-color);
    background-color: var(--light-color);
    border-radius: 50%;
    width: 56px;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
  
  .step-content h3 {
    margin: 0 0 8px 0;
    font-weight: 600;
    color: var(--primary-color);
  }
  
  .step-content p {
    margin: 0;
    line-height: 1.6;
    color: var(--text-secondary);
  }
  
  .welcome-footer {
    padding: 16px 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid var(--border-color);
    background-color: #f9f9f9;
  }
  
  .dont-show-again {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    user-select: none;
    color: var(--text-secondary);
    font-size: 14px;
  }
  
  .start-button {
    background-color: var(--accent-color);
    color: white;
    border: none;
    padding: 10px 24px;
    border-radius: 4px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
  }
  
  .start-button:hover {
    background-color: #f08500;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  }
  
  @media (max-width: 550px) {
    .step {
      flex-direction: column;
      align-items: center;
      text-align: center;
    }
    
    .welcome-footer {
      flex-direction: column-reverse;
      gap: 16px;
    }
  }
  </style>