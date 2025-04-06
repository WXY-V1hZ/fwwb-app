<template>
  <div class="control-panel" :class="{ 'collapsed': isCollapsed }">
    <div class="toggle-button" @click="togglePanel" title="展开/收起面板">
      <span v-if="isCollapsed">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="9 18 15 12 9 6"></polyline>
        </svg>
      </span>
      <span v-else>
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </span>
    </div>

    <div class="panel-content" v-if="!isCollapsed">
      <h2 class="panel-title">控制面板</h2>

      <div class="upload-section">
        <div class="upload-item">
          <div class="upload-label">红外图像</div>
          <div class="upload-preview">
            <img v-if="previewInfrared" :src="previewInfrared" alt="红外图像预览" />
            <div v-else class="no-preview">未上传图像</div>
          </div>
          <div class="upload-actions">
            <label class="upload-button">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
              选择文件
              <input type="file" accept="image/*" @change="handleInfraredUpload" hidden
                :disabled="processingStatus.isProcessing" />
            </label>
          </div>
        </div>

        <div class="upload-item">
          <div class="upload-label">热成像图像</div>
          <div class="upload-preview">
            <img v-if="previewThermal" :src="previewThermal" alt="热成像图像预览" />
            <div v-else class="no-preview">未上传图像</div>
          </div>
          <div class="upload-actions">
            <label class="upload-button">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
              选择文件
              <input type="file" accept="image/*" @change="handleThermalUpload" hidden
                :disabled="processingStatus.isProcessing" />
            </label>
          </div>
        </div>
      </div>

      <div class="process-section">
        <button class="process-button" @click="startProcessing"
          :disabled="!canProcess || processingStatus.isProcessing">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
          </svg>
          开始处理
        </button>

        <div class="progress-container" v-if="processingStatus.isProcessing">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: `${processingStatus.progress}%` }"></div>
          </div>
          <div class="progress-text">{{ processingStatus.progress }}%</div>
        </div>
      </div>

      <!-- 历史记录组件 -->
      <History :current-images="{
        infraredImage: infraredFile,
        thermalImage: thermalFile,
        processedImage: processedImage
      }" @load-history="loadFromHistory" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, defineEmits } from 'vue';
import api from '../api';
import History from './History.vue';
import type { ProcessingStatus, ImageData } from '../types';

const emit = defineEmits<{
  'update:images': [{ infraredImage: string | null; thermalImage: string | null; processedImage: string | null }],
  'update:processingStatus': [ProcessingStatus]
}>();

const isCollapsed = ref(false);
const infraredFile = ref<string | null>(null);
const thermalFile = ref<string | null>(null);
const previewInfrared = ref<string | null>(null);
const previewThermal = ref<string | null>(null);
const processedImage = ref<string | null>(null);

const processingStatus = ref<ProcessingStatus>({
  isProcessing: false,
  progress: 0
});

const canProcess = computed(() => {
  return infraredFile.value !== null && thermalFile.value !== null;
});

function togglePanel() {
  isCollapsed.value = !isCollapsed.value;
  // 通知父组件调整布局
  emit('update:images', {
    infraredImage: infraredFile.value,
    thermalImage: thermalFile.value,
    processedImage: null
  });
}

function loadFromHistory(historyImages: ImageData) {
  if (processingStatus.value.isProcessing) {
    alert('正在处理中，请稍后再试');
    return;
  }

  // 更新当前数据
  infraredFile.value = historyImages.infraredImage;
  thermalFile.value = historyImages.thermalImage;
  processedImage.value = historyImages.processedImage;

  // 更新预览
  if (infraredFile.value) {
    previewInfrared.value = api.getResource(infraredFile.value);
  }
  if (thermalFile.value) {
    previewThermal.value = api.getResource(thermalFile.value);
  }

  // 更新显示
  emit('update:images', {
    infraredImage: infraredFile.value,
    thermalImage: thermalFile.value,
    processedImage: processedImage.value
  });
}

async function handleInfraredUpload(event: Event) {
  const input = event.target as HTMLInputElement;
  if (!input.files || input.files.length === 0) return;

  const file = input.files[0];
  try {
    const response = await api.uploadImage(file);
    if (response.data.status === 'success') {
      infraredFile.value = response.data.data;
      previewInfrared.value = api.getResource(response.data.data);

      emit('update:images', {
        infraredImage: infraredFile.value,
        thermalImage: thermalFile.value,
        processedImage: null
      });
    } else {
      alert('上传失败: ' + response.data.info);
    }
  } catch (error) {
    console.error('上传失败:', error);
    alert('上传失败，请检查网络连接');
  }
}

async function handleThermalUpload(event: Event) {
  const input = event.target as HTMLInputElement;
  if (!input.files || input.files.length === 0) return;

  const file = input.files[0];
  try {
    const response = await api.uploadImage(file);
    if (response.data.status === 'success') {
      thermalFile.value = response.data.data;
      previewThermal.value = api.getResource(response.data.data);

      emit('update:images', {
        infraredImage: infraredFile.value,
        thermalImage: thermalFile.value,
        processedImage: null
      });
    } else {
      alert('上传失败: ' + response.data.info);
    }
  } catch (error) {
    console.error('上传失败:', error);
    alert('上传失败，请检查网络连接');
  }
}

async function startProcessing() {
  if (!canProcess.value || processingStatus.value.isProcessing) return;
  
  try {
    processingStatus.value = {
      isProcessing: true,
      progress: 30
    };
    
    emit('update:processingStatus', processingStatus.value);
    
    const response = await api.getResult(infraredFile.value!, thermalFile.value!);
    
    if (response.data.status === 'success') {
      processingStatus.value.progress = 70;
      emit('update:processingStatus', processingStatus.value);
      
      // 更新处理后的图像
      processedImage.value = response.data.data;
      
      // 完成处理
      setTimeout(() => {
        processingStatus.value = {
          isProcessing: false,
          progress: 100
        };
        
        emit('update:processingStatus', processingStatus.value);
        
        const resultImages = {
          infraredImage: infraredFile.value,
          thermalImage: thermalFile.value,
          processedImage: processedImage.value
        };
        
        emit('update:images', resultImages);
        
        // 重置进度条
        setTimeout(() => {
          processingStatus.value.progress = 0;
        }, 1000);
      }, 500);
    } else {
      processingStatus.value.isProcessing = false;
      alert('处理失败: ' + response.data.info);
    }
  } catch (error) {
    console.error('处理失败:', error);
    processingStatus.value.isProcessing = false;
    alert('处理失败，请检查网络连接');
  }
}
</script>

<style scoped>
.control-panel {
  position: relative;
  background-color: #f8f9fa;
  border-right: 1px solid #e0e0e0;
  height: 100%;
  transition: width 0.3s ease;
  width: 300px;
  overflow: hidden;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
}

.control-panel.collapsed {
  width: 40px;
}

.toggle-button {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
}

.toggle-button:hover {
  background-color: var(--dark-color);
  transform: scale(1.05);
}

.panel-content {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.panel-title {
  color: #333;
  margin-top: 20px;
  margin-bottom: 20px;
  font-size: 20px;
  text-align: center;
}

.upload-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.upload-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 10px;
  background-color: white;
}

.upload-label {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 10px;
  color: #333;
}

.upload-preview {
  width: 100%;
  height: 120px;
  border: 1px dashed #ccc;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  margin-bottom: 10px;
}

.upload-preview img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.no-preview {
  color: #999;
  font-size: 14px;
}

.upload-actions {
  display: flex;
  justify-content: center;
}

.upload-button {
  background-color: var(--secondary-color);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.upload-button:hover {
  background-color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.process-section {
  margin-top: auto;
  padding-top: 20px;
}

.process-button {
  width: 100%;
  background-color: var(--accent-color);
  color: white;
  border: none;
  padding: 12px;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.process-button:hover:not(:disabled) {
  background-color: #f08500;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.process-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  box-shadow: none;
}

.progress-container {
  margin-top: 15px;
}

.progress-bar {
  height: 10px;
  background-color: #e0e0e0;
  border-radius: 5px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: var(--secondary-color);
  transition: width 0.5s ease;
  background-image: linear-gradient(45deg,
      rgba(255, 255, 255, 0.15) 25%,
      transparent 25%,
      transparent 50%,
      rgba(255, 255, 255, 0.15) 50%,
      rgba(255, 255, 255, 0.15) 75%,
      transparent 75%,
      transparent);
  background-size: 1rem 1rem;
  animation: progress-bar-stripes 1s linear infinite;
}

@keyframes progress-bar-stripes {
  from {
    background-position: 1rem 0;
  }

  to {
    background-position: 0 0;
  }
}

.progress-text {
  text-align: center;
  margin-top: 5px;
  font-size: 12px;
  color: #666;
}
</style>