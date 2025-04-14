<template>
    <Teleport to="body">
      <div class="samples-overlay" v-if="isVisible" @click="closeOnOverlayClick">
        <div class="samples-modal fade-in" @click.stop>
          <div class="samples-header">
            <h2>浓烟环境人体目标判别系统 - 样例展示</h2>
            <button class="close-button" @click="close">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
  
          <div class="samples-content">
            <p class="samples-intro">
              以下是系统预设的几组样例，您可以直接查看处理效果。选择后系统将展示相应的红外、热成像及处理结果视频。
            </p>
            
            <div class="samples-list">
              <div 
                v-for="(sample, index) in samples" 
                :key="index" 
                class="sample-item" 
                :class="{'selected': selectedSample === index + 1}"
                @click="selectSample(index + 1)"
              >
                <div class="sample-preview">
                  <img :src="sample.thumbnail" alt="样例预览" />
                </div>
                <div class="sample-info">
                  <h3 class="sample-title">样例 {{ index + 1 }}</h3>
                  <p class="sample-desc">{{ sample.description }}</p>
                </div>
              </div>
            </div>
          </div>
  
          <div class="samples-footer">
            <button class="view-sample-button" @click="loadSelectedSample" :disabled="!selectedSample">
              查看所选样例
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue';
  
  const props = defineProps<{
    isVisible: boolean;
  }>();
  
  const emit = defineEmits<{
    'close': [];
    'select-sample': [sampleId: number];
  }>();
  
  const selectedSample = ref<number | null>(null);
  
  // 样例数据
  const samples = ref([
    {
      id: 1,
      thumbnail: '/api/imageResource?imagePath=examples/1.png',
      description: '室内浓烟环境示例',
    },
    {
      id: 2,
      thumbnail: '/api/imageResource?imagePath=examples/2.png',
      description: '楼道浓烟环境示例',
    },
    {
      id: 3,
      thumbnail: '/api/imageResource?imagePath=examples/3.png',
      description: '开放空间浓烟环境示例',
    }
  ]);
  
  function selectSample(sampleId: number) {
    selectedSample.value = sampleId;
  }
  
  function loadSelectedSample() {
    if (selectedSample.value) {
      emit('select-sample', selectedSample.value);
      close();
    }
  }
  
  function close() {
    emit('close');
  }
  
  function closeOnOverlayClick(event: MouseEvent) {
    if ((event.target as HTMLElement).classList.contains('samples-overlay')) {
      close();
    }
  }
  </script>
  
  <style scoped>
  .samples-overlay {
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
  
  .samples-modal {
    background-color: white;
    border-radius: 10px;
    width: 90%;
    max-width: 800px;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
    box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);
    overflow: hidden;
  }
  
  .samples-header {
    background: linear-gradient(135deg, #0284b9 0%, #015f86 100%);
    color: white;
    padding: 16px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 2px solid #03a9f4;
  }
  
  .samples-header h2 {
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
  
  .samples-content {
    padding: 24px;
    overflow-y: auto;
    flex-grow: 1;
  }
  
  .samples-intro {
    margin-top: 0;
    margin-bottom: 20px;
    line-height: 1.6;
    color: var(--text-secondary);
  }
  
  .samples-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }
  
  .sample-item {
    border: 2px solid var(--border-color);
    border-radius: 8px;
    overflow: hidden;
    transition: all 0.2s ease;
    cursor: pointer;
    background-color: #fff;
  }
  
  .sample-item:hover {
    transform: translateY(-3px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
    border-color: var(--secondary-color);
  }
  
  .sample-item.selected {
    border-color: var(--accent-color);
    box-shadow: 0 0 0 2px rgba(255, 153, 0, 0.3);
  }
  
  .sample-preview {
    width: 100%;
    height: 160px;
    overflow: hidden;
  }
  
  .sample-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }
  
  .sample-item:hover .sample-preview img {
    transform: scale(1.05);
  }
  
  .sample-info {
    padding: 15px;
  }
  
  .sample-title {
    margin: 0 0 8px 0;
    font-size: 18px;
    color: var(--primary-color);
  }
  
  .sample-desc {
    margin: 0;
    font-size: 14px;
    color: var(--text-secondary);
    line-height: 1.4;
  }
  
  .samples-footer {
    padding: 16px 24px;
    display: flex;
    justify-content: flex-end;
    border-top: 1px solid var(--border-color);
    background-color: #f9f9f9;
  }
  
  .view-sample-button {
    background-color: var(--accent-color);
    color: white;
    border: none;
    padding: 10px 24px;
    border-radius: 4px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
  }
  
  .view-sample-button:hover:not(:disabled) {
    background-color: #f08500;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  }
  
  .view-sample-button:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    background-color: #ccc;
  }
  
  @media (max-width: 650px) {
    .samples-list {
      grid-template-columns: 1fr;
    }
  }
  </style>