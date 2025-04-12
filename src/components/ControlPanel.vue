<template>
  <div class="control-panel" :class="{ 'collapsed': isCollapsed }" :style="panelStyle" @click="handlePanelClick">
    <div class="toggle-button" @click.stop="togglePanel" :title="isCollapsed ? '展开面板' : '收起面板'">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
        stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <polyline :points="isCollapsed ? '9 18 15 12 9 6' : '15 18 9 12 15 6'"></polyline>
      </svg>
    </div>

    <div v-if="isCollapsed" class="collapsed-hint">
      <div class="collapsed-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white"
          stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 20h9"></path>
          <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
        </svg>
      </div>
    </div>

    <div class="panel-content" v-if="!isCollapsed">
      <h2 class="panel-title">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 20h9"></path>
          <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path>
        </svg>
        控制面板
      </h2>

      <div class="upload-section">
        <div class="upload-item">
          <div class="upload-label">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <circle cx="8.5" cy="8.5" r="1.5"></circle>
              <polyline points="21 15 16 10 5 21"></polyline>
            </svg>
            红外视频
          </div>
          <DragDropUpload label="红外" :preview-image="previewInfrared" :disabled="processingStatus.isProcessing"
            @file-selected="handleInfraredUpload" @file-removed="removeInfraredVideo" />
        </div>

        <div class="upload-item">
          <div class="upload-label">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
              <circle cx="8.5" cy="8.5" r="1.5"></circle>
              <path d="M21 15 Z v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2"></path>
            </svg>
            热成像视频
          </div>
          <DragDropUpload label="热成像" :preview-image="previewThermal" :disabled="processingStatus.isProcessing"
            @file-selected="handleThermalUpload" @file-removed="removeThermalVideo" />
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

        <div class="progress-container" v-if="processingStatus.isProcessing || displayProgress > 0">
          <div class="progress-bar">
            <div class="progress-fill" :style="{
              width: `${displayProgress}%`,
              backgroundPosition: `${100 - displayProgress}% 0`
            }"></div>
          </div>
          <div class="progress-text">{{ displayProgress }}%</div>
        </div>
      </div>

      <!-- 历史记录组件 -->
      <History :current-videos="{
        infraredVideo: infraredFile,
        thermalVideo: thermalFile,
        processedVideo: processedVideo
      }" @load-history="loadFromHistory" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import api from '../api';
import History from './History.vue';
import DragDropUpload from './DragDropUpload.vue';
import type { ProcessingStatus, VideoData } from '../types';

const emit = defineEmits<{
  'update:videos': [{ infraredVideo: string | null; thermalVideo: string | null; processedVideo: string | null }],
  'update:processingStatus': [ProcessingStatus],
  'update:isPanelCollapsed': [boolean]
}>();

const isCollapsed = ref(false);
const infraredFile = ref<string | null>(null);
const thermalFile = ref<string | null>(null);
const previewInfrared = ref<string | null>(null);
const previewThermal = ref<string | null>(null);
const processedVideo = ref<string | null>(null);
const displayProgress = ref(0); // 用于显示的进度值

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

// 监听处理状态变化
watch(() => processingStatus.value, (newStatus, oldStatus) => {
  // 实时更新显示进度
  displayProgress.value = newStatus.progress;

  // 如果处理完成（从处理中变为未处理状态，且进度为100）
  if (oldStatus.isProcessing && !newStatus.isProcessing && newStatus.progress === 100) {
    // 保持进度条显示100%状态1秒
    setTimeout(() => {
      // 1秒后将显示进度重置为0
      displayProgress.value = 0;
    }, 1000);
  }
}, { deep: true });

const canProcess = computed(() => {
  return infraredFile.value !== null && thermalFile.value !== null;
});

// 处理整个侧边栏的点击事件
function handlePanelClick() {
  // 只有当侧边栏是收起状态时才展开
  if (isCollapsed.value) {
    isCollapsed.value = false;
    emit('update:isPanelCollapsed', false);
    updateVideos();
  }
}

function togglePanel() {
  isCollapsed.value = !isCollapsed.value;
  // 通知父组件侧边栏状态变化
  emit('update:isPanelCollapsed', isCollapsed.value);
  updateVideos();
}

// 单独的方法来更新视频数据
function updateVideos() {
  emit('update:videos', {
    infraredVideo: infraredFile.value,
    thermalVideo: thermalFile.value,
    processedVideo: processedVideo.value
  });
}

function loadFromHistory(historyVideos: VideoData) {
  if (processingStatus.value.isProcessing) {
    alert('正在处理中，请稍后再试');
    return;
  }

  // 更新当前数据
  infraredFile.value = historyVideos.infraredVideo;
  thermalFile.value = historyVideos.thermalVideo;
  processedVideo.value = historyVideos.processedVideo;

  // 更新预览
  if (infraredFile.value) {
    // 获取对应的缩略图
    const infraredThumbnail = infraredFile.value.replace('.mp4', '.png');
    previewInfrared.value = api.getImageResource(infraredThumbnail);
  }
  if (thermalFile.value) {
    // 获取对应的缩略图
    const thermalThumbnail = thermalFile.value.replace('.mp4', '.png');
    previewThermal.value = api.getImageResource(thermalThumbnail);
  }

  // 更新显示但不影响侧边栏宽度
  updateVideos();
}

async function handleInfraredUpload(file: File) {
  try {
    const response = await api.uploadVideo(file);
    if (response.data.status === 'success') {
      infraredFile.value = response.data.data;
      // 获取对应的缩略图
      const infraredThumbnail = infraredFile.value!.replace('.mp4', '.png');
      previewInfrared.value = api.getImageResource(infraredThumbnail);
      updateVideos();
    } else {
      alert('上传失败: ' + response.data.info);
    }
  } catch (error) {
    console.error('上传失败:', error);
    alert('上传失败，请检查网络连接');
  }
}

function removeInfraredVideo() {
  infraredFile.value = null;
  previewInfrared.value = null;
  updateVideos();
}

async function handleThermalUpload(file: File) {
  try {
    const response = await api.uploadVideo(file);
    if (response.data.status === 'success') {
      thermalFile.value = response.data.data;
      // 获取对应的缩略图
      const thermalThumbnail = thermalFile.value!.replace('.mp4', '.png');
      previewThermal.value = api.getImageResource(thermalThumbnail);
      updateVideos();
    } else {
      alert('上传失败: ' + response.data.info);
    }
  } catch (error) {
    console.error('上传失败:', error);
    alert('上传失败，请检查网络连接');
  }
}

function removeThermalVideo() {
  thermalFile.value = null;
  previewThermal.value = null;
  updateVideos();
}

async function startProcessing() {
  if (!canProcess.value || processingStatus.value.isProcessing) return;

  try {
    // 开始处理，进度设为30%
    processingStatus.value = {
      isProcessing: true,
      progress: 30
    };

    emit('update:processingStatus', processingStatus.value);

    const response = await api.getResult(infraredFile.value!, thermalFile.value!);

    if (response.data.status === 'success') {
      // 更新进度为70%
      processingStatus.value.progress = 70;
      emit('update:processingStatus', processingStatus.value);

      // 更新处理后的视频
      processedVideo.value = response.data.data;

      // 完成处理
      setTimeout(() => {
        processingStatus.value = {
          isProcessing: false,
          progress: 100
        };

        emit('update:processingStatus', processingStatus.value);
        updateVideos();
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
  background: linear-gradient(145deg, #f8f9fa 0%, #f1f3f4 100%);
  border-right: 1px solid var(--border-color);
  height: 100%;
  transition: width 0.3s ease;
  overflow: hidden;
  box-shadow: 4px 0 12px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.control-panel.collapsed {
  width: 60px !important;
  background: linear-gradient(145deg, var(--primary-color) 0%, var(--dark-color) 100%);
  justify-content: center;
  cursor: pointer;
}

.toggle-button {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--dark-color) 100%);
  color: white;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.2);
}

.control-panel.collapsed .toggle-button {
  position: relative;
  top: 0;
  right: 0;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.control-panel.collapsed .toggle-button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.toggle-button:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.collapsed-hint {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  pointer-events: none;
}

.collapsed-icon {
  width: 40px;
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  opacity: 0.8;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.05); opacity: 1; }
  100% { transform: scale(1); opacity: 0.8; }
}

.collapsed-icon svg {
  width: 24px;
  height: 24px;
}

.panel-content {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  width: 100%;
  scrollbar-width: thin;
  scrollbar-color: var(--border-color) transparent;
  opacity: 1;
  transition: opacity 0.3s ease;
}

.panel-content.hidden {
  opacity: 0;
  pointer-events: none;
}

.panel-content::-webkit-scrollbar {
  width: 6px;
}

.panel-content::-webkit-scrollbar-track {
  background: transparent;
}

.panel-content::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 10px;
}

.panel-title {
  color: var(--primary-color);
  margin-top: 10px;
  margin-bottom: 30px;
  font-size: 22px;
  font-weight: 600;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding-bottom: 15px;
}

.panel-title svg {
  margin-bottom: 0;
  filter: drop-shadow(0 2px 3px rgba(0, 0, 0, 0.1));
}

.panel-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: linear-gradient(to right, var(--primary-color), var(--secondary-color));
  border-radius: 2px;
}

.upload-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 25px;
}

.upload-item {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 15px;
  background: linear-gradient(to bottom right, white, #f9f9f9);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.upload-item:hover {
  border-color: var(--secondary-color);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-3px);
}

.upload-label {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 12px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-label svg {
  color: var(--secondary-color);
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
}

.process-section {
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

.process-button {
  width: 100%;
  background: linear-gradient(135deg, var(--accent-color) 0%, #f57c00 100%);
  color: white;
  border: none;
  padding: 14px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  box-shadow: 0 4px 12px rgba(245, 124, 0, 0.3);
  position: relative;
  overflow: hidden;
}

.process-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(120deg, transparent, rgba(255,255,255,0.4), transparent);
  transition: all 0.5s;
}

.process-button:hover:not(:disabled)::before {
  left: 100%;
}

.process-button:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(245, 124, 0, 0.35);
}

.process-button:active:not(:disabled) {
  transform: translateY(-1px);
}

.process-button:disabled {
  background: linear-gradient(135deg, #ccc 0%, #999 100%);
  cursor: not-allowed;
  box-shadow: none;
  opacity: 0.7;
}

.progress-container {
  margin-top: 15px;
}

.progress-bar {
  height: 10px;
  background-color: #e0e0e0;
  border-radius: 5px;
  overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
}

.progress-fill {
  height: 100%;
  background: linear-gradient(to right, 
    #4CAF50, 
    #2196F3, 
    #FFC107
  );
  background-size: 200% 100%;
  animation: progress-bar-stripes 1.5s linear infinite;
  transition: width 0.3s ease;
  box-shadow: 0 0 8px rgba(33, 150, 243, 0.5);
}

@keyframes progress-bar-stripes {
  0% { background-position: 100% 0; }
  100% { background-position: 0 0; }
}

.progress-text {
  text-align: center;
  margin-top: 10px;
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}
</style>