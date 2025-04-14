<template>
  <div class="display-area" :class="{ 'panel-collapsed': isPanelCollapsed, 'sample-mode': isSampleMode }">
    <div class="display-container" :class="{ 'expanded-output': isOutputExpanded }">
      <div class="input-displays" :class="{ 'hidden': isOutputExpanded }">
        <div class="display-card">
          <div class="card-title">红外视频 (输入1)</div>
          <div class="video-container" v-if="isSampleMode && videos.infraredVideo">
            <Player :src="getVideoResourceUrl(videos.infraredVideo)" 
                  :dateFolder="getDateFolder(videos.infraredVideo)" 
                  :folderPath="getFolderPath(videos.infraredVideo)" />
          </div>
          <div v-else class="image-container">
            <img v-if="videos.infraredVideo" :src="getVideoThumbnail(videos.infraredVideo)" alt="红外视频" />
            <div v-else class="no-image">未上传红外视频</div>
          </div>
        </div>

        <div class="display-card">
          <div class="card-title">热成像视频 (输入2)</div>
          <div class="video-container" v-if="isSampleMode && videos.thermalVideo">
            <Player :src="getVideoResourceUrl(videos.thermalVideo)" 
                  :dateFolder="getDateFolder(videos.thermalVideo)" 
                  :folderPath="getFolderPath(videos.thermalVideo)" />
          </div>
          <div v-else class="image-container">
            <img v-if="videos.thermalVideo" :src="getVideoThumbnail(videos.thermalVideo)" alt="热成像视频" />
            <div v-else class="no-image">未上传热成像视频</div>
          </div>
        </div>
      </div>

      <div class="arrow-container" :class="{ 'hidden': isOutputExpanded }">
        <div class="process-arrow">
          <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="5" y1="12" x2="19" y2="12"></line>
            <polyline points="12 5 19 12 12 19"></polyline>
          </svg>
          <div class="processing-progress">
            <div class="processing-progress-bar" :style="{
              width: `${progressDisplay}%`,
              backgroundPosition: `${100 - progressDisplay}% 0`
            }"></div>
          </div>
        </div>
      </div>

      <div class="output-display" :class="{ 'expanded': isOutputExpanded }">
        <div class="display-card result-card">
          <div class="card-title" 
            @click="toggleOutputExpand" 
            :class="{ 'expandable': videos.processedVideo }" 
            :title="videos.processedVideo ? '点击切换全屏显示/还原' : ''">
            <div class="title-text">
              处理结果 (浓烟环境下人体目标检测)
              <svg v-if="videos.processedVideo" class="expand-indicator" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline v-if="!isOutputExpanded" points="17 11 12 6 7 11"></polyline>
                <polyline v-else points="7 13 12 18 17 13"></polyline>
              </svg>
            </div>
            <div class="title-actions" @click.stop>
              <button v-if="videos.processedVideo" class="summary-toggle-button" @click="toggleSummary"
                :class="{ 'active': showSummary }" title="显示/隐藏分析结果">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="11" cy="11" r="8"></circle>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                  <line x1="11" y1="8" x2="11" y2="14"></line>
                  <line x1="8" y1="11" x2="14" y2="11"></line>
                </svg>
              </button>
            </div>
          </div>
          <div class="video-container" v-if="videos.processedVideo">
            <Player :src="getVideoResourceUrl(videos.processedVideo)"
              :dateFolder="getDateFolder(videos.processedVideo)" 
              :folderPath="getFolderPath(videos.processedVideo)" />
          </div>
          <div v-else class="no-video">尚未处理</div>
        </div>
      </div>
    </div>

    <!-- 当有处理结果且用户点击分析按钮时显示结果分析 -->
    <ResultsSummary v-if="showSummary && videos.processedVideo" :visible="showSummary" />

    <!-- 检测完成提示 -->
    <div class="detection-complete-toast" v-if="showCompletionToast">
      <div class="toast-content">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
          <polyline points="22 4 12 14.01 9 11.01"></polyline>
        </svg>
        <span>检测完成! 点击分析按钮查看详细结果</span>
      </div>
      <div class="toast-progress">
        <div class="toast-progress-bar"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import api from '../api';
import ResultsSummary from './ResultsSummary.vue';
import Player from './Player.vue';
import type { VideoData, ProcessingStatus } from '../types';

const props = defineProps<{
  videos: VideoData;
  processingStatus: ProcessingStatus;
  isPanelCollapsed: boolean;
  isSampleMode?: boolean;
  currentSampleId?: number | null;
}>();

const showSummary = ref(false);
const showCompletionToast = ref(false);
const displayProgress = ref(0); // 用于显示的进度值
const isOutputExpanded = ref(false); // 新增: 输出展示框是否展开的状态

// 计算实际显示的进度
const progressDisplay = computed(() => {
  return displayProgress.value;
});

// 解析视频的日期和文件夹路径
function getDateFolder(videoPath: string | null): string | null {
  if (!videoPath) return null;
  
  // 如果是样例模式下的路径
  if (videoPath.includes('/examples/')) {
    return 'examples';
  }
  
  // 分析常规路径结构 (如: 20250411/8UH2qlyfeZ5iDWIIfc5WXM7qvKS5J3Yhm3mo0dIl/)
  const pathParts = videoPath.split('/');
  if (pathParts.length >= 2) {
    return pathParts[0];
  }
  
  return null;
}

// 获取文件夹路径
function getFolderPath(videoPath: string | null): string | null {
  if (!videoPath) return null;
  
  // 如果是样例模式路径，提取样例ID和类型
  if (videoPath.includes('/examples/')) {
    const parts = videoPath.split('/');
    // 通常格式: /api/videoResource/examples/1/input1
    // 我们需要返回 1/input1 作为文件夹路径
    if (parts.length >= 5) {
      return `${parts[3]}/${parts[4]}`;
    }
    return null;
  }
  
  // 常规路径解析
  const pathParts = videoPath.split('/');
  if (pathParts.length >= 2) {
    // 确保去掉末尾的斜杠
    return pathParts[1].endsWith('/') ? pathParts[1].slice(0, -1) : pathParts[1];
  }
  
  return null;
}

watch(() => props.processingStatus, (newStatus, oldStatus) => {
  displayProgress.value = newStatus.progress;

  if (oldStatus.isProcessing && !newStatus.isProcessing && newStatus.progress === 100) {
    showCompletionToast.value = true;

    setTimeout(() => {
      displayProgress.value = 0;
    }, 1000);

    setTimeout(() => {
      showCompletionToast.value = false;
    }, 5000);
  }
}, { deep: true });

// 获取视频缩略图
function getVideoThumbnail(videoPath: string | null) {
  if (!videoPath) return '';
  
  // 如果是样例路径，使用预设的缩略图
  if (videoPath.includes('/examples/')) {
    const parts = videoPath.split('/');
    if (parts.length >= 5) {
      const sampleId = parts[3];
      const inputType = parts[4];
      return `/api/imageResource/examples/sample${sampleId}_${inputType}.png`;
    }
    return '';
  }

  if (videoPath.endsWith('/')) {
    const thumbnailPath = videoPath.slice(0, -1) + '.png';
    return api.getImageResource(thumbnailPath);
  }

  const thumbnailPath = videoPath.replace('.mp4', '.png');
  return api.getImageResource(thumbnailPath);
}

// 获取视频资源URL用于播放
function getVideoResourceUrl(videoPath: string | null) {
  if (!videoPath) {
    console.error("无法构建视频URL: 路径为空");
    return '';
  }
  
  // 样例模式下直接返回路径
  if (videoPath.includes('/examples/')) {
    return videoPath;
  }
  
  // 解析常规路径
  const dateFolder = getDateFolder(videoPath);
  const folderPath = getFolderPath(videoPath);
  
  if (!dateFolder || !folderPath) {
    console.error("无法构建视频URL: 日期或文件夹路径为空", {videoPath, dateFolder, folderPath});
    return '';
  }

  const fullUrl = api.getVideoResource(dateFolder, folderPath);
  return fullUrl;
}

function toggleSummary() {
  showSummary.value = !showSummary.value;
}

// 切换输出区域展开/收起状态
function toggleOutputExpand() {
  isOutputExpanded.value = !isOutputExpanded.value;
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
  position: relative;
}

.display-area.panel-collapsed {
  margin-left: 0;
}

/* 样例模式样式 */
.display-area.sample-mode {
  background-color: #f7fafc;
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
  transition: all 0.3s ease;
}

/* 样例模式下的容器 */
.sample-mode .display-container {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(66, 153, 225, 0.2);
}

/* 新增: 输出展开时容器样式变更 */
.display-container.expanded-output {
  gap: 0;
}

.input-displays {
  display: flex;
  gap: 20px;
  height: 40%;
  animation: fadeIn 0.5s ease-in-out;
  transition: height 0.3s ease, opacity 0.3s ease;
}

/* 新增: 隐藏输入显示区域 */
.input-displays.hidden {
  height: 0;
  opacity: 0;
  overflow: hidden;
  margin: 0;
  padding: 0;
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

/* 样例模式下的卡片 */
.sample-mode .display-card {
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
}

.card-title {
  background-color: var(--primary-color);
  color: white;
  padding: 10px 15px;
  font-size: 16px;
  font-weight: 500;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 样例模式下的标题 */
.sample-mode .input-displays .card-title {
  background-color: #3182ce;
}

/* 新增: 可点击的标题栏样式 */
.card-title.expandable {
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
}

.card-title.expandable:hover {
  background-color: var(--accent-color-hover, #0d6efd);
}

.title-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.expand-indicator {
  margin-left: 8px;
  opacity: 0.7;
  transition: all 0.2s ease;
}

.card-title.expandable:hover .expand-indicator {
  opacity: 1;
}

/* 标题栏内的按钮组 */
.title-actions {
  display: flex;
  gap: 8px;
}

.summary-toggle-button {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
  transition: all 0.2s ease;
}

.summary-toggle-button:hover {
  background: rgba(255, 255, 255, 0.4);
}

.summary-toggle-button.active {
  background: white;
  color: var(--accent-color);
  transform: rotate(45deg);
}

.image-container {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 15px;
  overflow: hidden;
  position: relative;
}

.image-container img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 8px;
  transition: transform 0.3s ease;
}

.video-container {
  flex-grow: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
  padding: 0;
  overflow: hidden;
  background-color: black;
}

.no-image,
.no-video {
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
  transition: height 0.3s ease, opacity 0.3s ease;
}

/* 新增: 隐藏箭头容器 */
.arrow-container.hidden {
  height: 0;
  opacity: 0;
  overflow: hidden;
  margin: 0;
  padding: 0;
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
  background: linear-gradient(to right,
      #FFC107 0%,
      #CDDC39 50%,
      #4CAF50 100%);
  background-size: 200% 100%;
  width: 0%;
  transition: width 0.3s ease, background-position 0.3s ease;
}

.output-display {
  flex-grow: 1;
  height: 45%;
  animation: fadeIn 0.5s ease-in-out;
  animation-delay: 0.2s;
  animation-fill-mode: both;
  display: flex;
  transition: height 0.3s ease;
}

/* 新增: 输出区域展开状态 */
.output-display.expanded {
  height: 100%;
}

.result-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.result-card .card-title {
  background-color: var(--accent-color);
  flex-shrink: 0;
  z-index: 2;
}

/* 处理完成提示 */
.detection-complete-toast {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(56, 142, 60, 0.95);
  color: white;
  padding: 0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  width: 350px;
  overflow: hidden;
  animation: toast-in 0.5s ease;
  z-index: 1000;
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 15px;
}

.toast-progress {
  height: 4px;
  background-color: rgba(255, 255, 255, 0.3);
  width: 100%;
}

.toast-progress-bar {
  height: 100%;
  background-color: white;
  width: 100%;
  animation: toast-progress 5s linear;
}

@keyframes toast-in {
  from {
    transform: translate(-50%, 100px);
    opacity: 0;
  }

  to {
    transform: translate(-50%, 0);
    opacity: 1;
  }
}

@keyframes toast-progress {
  from {
    width: 100%;
  }

  to {
    width: 0%;
  }
}

/* 新增动画: 展开/收起过渡 */
@keyframes expandIn {
  from {
    max-height: 0;
    opacity: 0;
  }
  to {
    max-height: 1000px;
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .input-displays {
    flex-direction: column;
    height: auto;
  }

  .display-card {
    min-height: 200px;
  }

  .output-display {
    height: auto;
    min-height: 300px;
  }

  .video-container {
    min-height: 300px;
  }
}
</style>