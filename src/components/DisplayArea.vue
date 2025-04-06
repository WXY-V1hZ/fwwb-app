<template>
  <div class="display-area" :class="{ 'panel-collapsed': isPanelCollapsed }">
    <div class="display-container">
      <div class="input-displays">
        <div class="display-card">
          <div class="card-title">红外图像 (输入1)</div>
          <div class="image-container">
            <img v-if="images.infraredImage" :src="getResourceUrl(images.infraredImage)" alt="红外图像" />
            <div v-else class="no-image">未上传红外图像</div>
          </div>
        </div>
        
        <div class="display-card">
          <div class="card-title">热成像图像 (输入2)</div>
          <div class="image-container">
            <img v-if="images.thermalImage" :src="getResourceUrl(images.thermalImage)" alt="热成像图像" />
            <div v-else class="no-image">未上传热成像图像</div>
          </div>
        </div>
      </div>
      
      <div class="arrow-container">
        <div class="process-arrow">
          <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="5" y1="12" x2="19" y2="12"></line>
            <polyline points="12 5 19 12 12 19"></polyline>
          </svg>
          <div class="processing-progress">
            <div 
              class="processing-progress-bar" 
              :style="{ width: processingStatus.isProcessing ? `${processingStatus.progress}%` : '0%' }"
            ></div>
          </div>
        </div>
      </div>
      
      <div class="output-display">
        <div class="display-card result-card">
          <div class="card-title">处理结果 (浓烟环境下人体目标检测)</div>
          <div class="image-container">
            <img v-if="images.processedImage" :src="getResourceUrl(images.processedImage)" alt="处理结果" />
            <div v-else class="no-image">尚未处理</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue';
import api from '../api';
import type { ImageData, ProcessingStatus } from '../types';

const props = defineProps<{
  images: ImageData;
  processingStatus: ProcessingStatus;
  isPanelCollapsed: boolean;
}>();

function getResourceUrl(sourceName: string | null) {
  if (!sourceName) return '';
  return api.getResource(sourceName);
}
</script>

<style scoped>
.display-area {
  flex-grow: 1;
  padding: 20px;
  background-color: var(--background-color);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.display-area.panel-collapsed {
  margin-left: 0;
}

.display-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 20px;
  background-color: var(--card-background);
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
  overflow: hidden;
}

.input-displays {
  display: flex;
  gap: 20px;
  height: 40%;
  animation: fadeIn 0.5s ease-in-out;
}

.display-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: all 0.3s ease;
}

.display-card:hover {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-title {
  background-color: var(--primary-color);
  color: white;
  padding: 10px 15px;
  font-size: 16px;
  font-weight: 500;
}

.image-container {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 15px;
  overflow: hidden;
}

.image-container img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.no-image {
  color: #999;
  font-size: 16px;
  text-align: center;
  padding: 40px;
  border: 2px dashed #e0e0e0;
  border-radius: 8px;
  width: 100%;
  height: 80%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 60px;
}

.process-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #666;
}

.process-arrow svg {
  color: var(--secondary-color);
  margin-bottom: 10px;
}

.processing-progress {
  width: 120px;
  height: 8px;
  background-color: var(--light-color);
  border-radius: 4px;
  overflow: hidden;
  margin-top: 5px;
}

.processing-progress-bar {
  height: 100%;
  background-color: var(--accent-color);
  width: 0%;
  transition: width 0.3s ease;
}

.output-display {
  flex-grow: 1;
  height: 45%;
  animation: fadeIn 0.5s ease-in-out;
  animation-delay: 0.2s;
  animation-fill-mode: both;
}

.result-card {
  height: 100%;
}

.result-card .card-title {
  background-color: var(--accent-color);
}

@media (max-width: 768px) {
  .input-displays {
    flex-direction: column;
    height: auto;
  }
  
  .display-card {
    min-height: 200px;
  }
}
</style>