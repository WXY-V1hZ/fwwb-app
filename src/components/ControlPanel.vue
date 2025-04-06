<template>
  <div class="control-panel" :class="{ 'collapsed': isCollapsed }" :style="panelStyle">
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
          <DragDropUpload
            label="红外"
            :preview-image="previewInfrared"
            :disabled="processingStatus.isProcessing"
            @file-selected="handleInfraredUpload"
            @file-removed="removeInfraredImage"
          />
        </div>

        <div class="upload-item">
          <div class="upload-label">热成像图像</div>
          <DragDropUpload
            label="热成像"
            :preview-image="previewThermal"
            :disabled="processingStatus.isProcessing"
            @file-selected="handleThermalUpload"
            @file-removed="removeThermalImage"
          />
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
import DragDropUpload from './DragDropUpload.vue';
import type { ProcessingStatus, ImageData } from '../types';

const emit = defineEmits<{
  'update:images': [{ infraredImage: string | null; thermalImage: string | null; processedImage: string | null }],
  'update:processingStatus': [ProcessingStatus],
  'update:isPanelCollapsed': [boolean]
}>();

const isCollapsed = ref(false);
const infraredFile = ref<string | null>(null);
const thermalFile = ref<string | null>(null);
const previewInfrared = ref<string | null>(null);
const previewThermal = ref<string | null>(null);
const processedImage = ref<string | null>(null);

// 使用computed属性确保侧边栏宽度一致
const panelStyle = computed(() => {
  return {
    width: isCollapsed.value ? '40px' : '300px'
  };
});

const processingStatus = ref<ProcessingStatus>({
  isProcessing: false,
  progress: 0
});

const canProcess = computed(() => {
  return infraredFile.value !== null && thermalFile.value !== null;
});

function togglePanel() {
  isCollapsed.value = !isCollapsed.value;
  // 通知父组件侧边栏状态变化，使用单独的事件
  emit('update:isPanelCollapsed', isCollapsed.value);

  // 更新图像数据，但不影响侧边栏宽度
  updateImages();
}

// 单独的方法来更新图像数据
function updateImages() {
  emit('update:images', {
    infraredImage: infraredFile.value,
    thermalImage: thermalFile.value,
    processedImage: processedImage.value
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

  // 更新显示但不影响侧边栏宽度
  updateImages();
}

async function handleInfraredUpload(file: File) {
  try {
    const response = await api.uploadImage(file);
    if (response.data.status === 'success') {
      infraredFile.value = response.data.data;
      previewInfrared.value = api.getResource(response.data.data);

      // 使用单独的方法更新图像数据，避免影响侧边栏宽度
      updateImages();
    } else {
      alert('上传失败: ' + response.data.info);
    }
  } catch (error) {
    console.error('上传失败:', error);
    alert('上传失败，请检查网络连接');
  }
}

function removeInfraredImage() {
  infraredFile.value = null;
  previewInfrared.value = null;
  updateImages();
}

async function handleThermalUpload(file: File) {
  try {
    const response = await api.uploadImage(file);
    if (response.data.status === 'success') {
      thermalFile.value = response.data.data;
      previewThermal.value = api.getResource(response.data.data);

      // 使用单独的方法更新图像数据，避免影响侧边栏宽度
      updateImages();
    } else {
      alert('上传失败: ' + response.data.info);
    }
  } catch (error) {
    console.error('上传失败:', error);
    alert('上传失败，请检查网络连接');
  }
}

function removeThermalImage() {
  thermalFile.value = null;
  previewThermal.value = null;
  updateImages();
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

        // 使用单独的方法更新图像数据
        updateImages();

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
  overflow: hidden;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
  /* 防止侧边栏被压缩 */
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
  width: 100%;
  /* 确保内容宽度充满父容器 */
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