<template>
  <div class="app-container">
    <Banner />
    <div class="main-content">
      <ControlPanel 
        @update:videos="updateVideos" 
        @update:processingStatus="updateProcessingStatus"
        @update:isPanelCollapsed="updatePanelCollapsed"
        @update:sampleMode="updateSampleMode"
      />
      <DisplayArea 
        :videos="videos" 
        :processingStatus="processingStatus" 
        :isPanelCollapsed="isPanelCollapsed"
        :isSampleMode="isSampleMode"
        :currentSampleId="currentSampleId"
      />
    </div>
    <HelpOverlay />
    <WelcomeModal />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Banner from './components/Banner.vue';
import ControlPanel from './components/ControlPanel.vue';
import DisplayArea from './components/DisplayArea.vue';
import HelpOverlay from './components/HelpOverlay.vue';
import WelcomeModal from './components/WelcomeModal.vue';
import type { VideoData, ProcessingStatus } from './types';

const isPanelCollapsed = ref(false);
const videos = ref<VideoData>({
  infraredVideo: null,
  thermalVideo: null,
  processedVideo: null
});

const processingStatus = ref<ProcessingStatus>({
  isProcessing: false,
  progress: 0
});

// 样例模式状态
const isSampleMode = ref(false);
const currentSampleId = ref<number | null>(null);

function updateVideos(newVideos: VideoData) {
  videos.value = {...newVideos};
}

function updateProcessingStatus(status: ProcessingStatus) {
  processingStatus.value = {...status};
}

function updatePanelCollapsed(collapsed: boolean) {
  isPanelCollapsed.value = collapsed;
}

function updateSampleMode(sampleMode: boolean, sampleId: number | null) {
  isSampleMode.value = sampleMode;
  currentSampleId.value = sampleId;
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}

html, body {
  height: 100%;
  width: 100%;
}

#app {
  height: 100%;
  width: 100%;
}

.app-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

.main-content {
  display: flex;
  flex-grow: 1;
  overflow: hidden;
}

:root {
  --primary-color: #0284b9;
  --dark-color: #015f86;
  --secondary-color: #03a9f4;
  --accent-color: #ff9800;
  --text-primary: #333333;
  --text-secondary: #666666;
  --background-color: #f5f5f7;
  --card-background: #ffffff;
  --border-color: #e0e0e0;
  --light-color: #f0f7fa;
}
</style>