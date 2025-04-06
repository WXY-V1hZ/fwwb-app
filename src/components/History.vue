<template>
    <div class="history-panel">
      <div class="history-header">
        <h3 class="history-title">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="history-icon">
            <circle cx="12" cy="12" r="10"></circle>
            <polyline points="12 6 12 12 16 14"></polyline>
          </svg>
          历史记录
        </h3>
        <button class="clear-history-btn" @click="clearHistory" title="清空历史记录">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          </svg>
        </button>
      </div>
      <div v-if="historyItems.length === 0" class="empty-history">
        <p>暂无处理记录</p>
      </div>
      <div v-else class="history-list">
        <div v-for="(item, index) in historyItems" :key="index" class="history-item" @click="loadHistory(item)">
          <div class="history-item-preview">
            <div class="preview-image">
              <img :src="getResourceUrl(item.processedImage)" alt="处理结果" />
            </div>
          </div>
          <div class="history-item-info">
            <div class="history-item-date">{{ formatDate(item.timestamp) }}</div>
            <button class="delete-btn" @click.stop="deleteHistoryItem(index)" title="删除记录">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </template>
    
  <script setup lang="ts">
  import { ref, onMounted, watch } from 'vue';
  import api from '../api';
  import type { ImageData } from '../types';
    
  interface HistoryItem extends ImageData {
    timestamp: number;
  }
    
  const props = defineProps<{
    currentImages: ImageData;
  }>();
    
  const emit = defineEmits<{
    'load-history': [images: ImageData];
  }>();
    
  const historyItems = ref<HistoryItem[]>([]);
    
  // 从本地存储加载历史记录
  onMounted(() => {
    const savedHistory = localStorage.getItem('smoke-detection-history');
    if (savedHistory) {
      try {
        historyItems.value = JSON.parse(savedHistory);
      } catch (e) {
        console.error('Failed to parse history:', e);
        historyItems.value = [];
      }
    }
  });
    
  // 监视当前图像，如果有处理结果则添加到历史记录
  watch(() => props.currentImages, (newImages) => {
    if (newImages.processedImage && newImages.infraredImage && newImages.thermalImage) {
      // 添加到历史记录
      addToHistory(newImages);
    }
  }, { deep: true });
    
  // 添加到历史记录
  function addToHistory(images: ImageData) {
    // 检查是否已存在相同的处理结果（避免重复记录）
    if (historyItems.value.some(item => 
      item.processedImage === images.processedImage &&
      item.infraredImage === images.infraredImage && 
      item.thermalImage === images.thermalImage
    )) {
      return;
    }
    
    const historyItem: HistoryItem = {
      ...images,
      timestamp: Date.now()
    };
    
    historyItems.value.unshift(historyItem);
    
    // 限制历史记录数量，保留最近的10条
    if (historyItems.value.length > 10) {
      historyItems.value = historyItems.value.slice(0, 10);
    }
    
    // 保存到本地存储
    saveHistory();
  }
    
  // 保存历史记录到本地存储
  function saveHistory() {
    localStorage.setItem('smoke-detection-history', JSON.stringify(historyItems.value));
  }
    
  // 加载历史记录项目
  function loadHistory(item: HistoryItem) {
    emit('load-history', {
      infraredImage: item.infraredImage,
      thermalImage: item.thermalImage,
      processedImage: item.processedImage
    });
  }
    
  // 删除历史记录项目
  function deleteHistoryItem(index: number) {
    historyItems.value.splice(index, 1);
    saveHistory();
  }
    
  // 清空所有历史记录
  function clearHistory() {
    if (confirm('确定要清空所有历史记录吗？')) {
      historyItems.value = [];
      localStorage.removeItem('smoke-detection-history');
    }
  }
    
  // 格式化日期
  function formatDate(timestamp: number): string {
    const date = new Date(timestamp);
    return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${date.getMinutes().toString().padStart(2, '0')}`;
  }
  
  // 获取资源URL
  function getResourceUrl(sourceName: string | null) {
    if (!sourceName) return '';
    return api.getResource(sourceName);
  }
  </script>
    
  <style scoped>
  .history-panel {
    background-color: var(--card-background);
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    margin-top: 20px;
    overflow: hidden;
  }
    
  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 15px;
    background-color: var(--primary-color);
    color: white;
  }
    
  .history-title {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 6px;
  }
    
  .history-icon {
    opacity: 0.9;
  }
    
  .clear-history-btn {
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 4px;
    border-radius: 4px;
    transition: background-color 0.2s;
  }
    
  .clear-history-btn:hover {
    background-color: rgba(255, 255, 255, 0.2);
  }
    
  .empty-history {
    padding: 20px;
    text-align: center;
    color: var(--text-secondary);
    font-style: italic;
  }
    
  .history-list {
    max-height: 300px;
    overflow-y: auto;
    padding: 10px;
  }
    
  .history-item {
    display: flex;
    background-color: var(--light-color);
    border-radius: 6px;
    margin-bottom: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
  }
    
  .history-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
  }
    
  .history-item-preview {
    width: 80px;
    height: 60px;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
  }
    
  .preview-image {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
    
  .preview-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
    
  .history-item-info {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 10px;
  }
    
  .history-item-date {
    font-size: 14px;
    color: var(--text-primary);
  }
    
  .delete-btn {
    background: none;
    border: none;
    color: var(--text-secondary);
    cursor: pointer;
    padding: 5px;
    border-radius: 50%;
    transition: background-color 0.2s;
    display: flex;
    align-items: center;
    justify-content: center;
  }
    
  .delete-btn:hover {
    background-color: rgba(0, 0, 0, 0.05);
    color: var(--primary-color);
  }
  </style>