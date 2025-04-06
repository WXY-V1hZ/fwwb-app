<template>
  <div class="app-container">
    <Banner />
    <div class="main-content">
      <ControlPanel 
        @update:images="updateImages" 
        @update:processingStatus="updateProcessingStatus"
        @update:isPanelCollapsed="updatePanelCollapsed"
      />
      <DisplayArea 
        :images="images" 
        :processingStatus="processingStatus" 
        :isPanelCollapsed="isPanelCollapsed" 
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
import type { ImageData, ProcessingStatus } from './types';

const isPanelCollapsed = ref(false);
const images = ref<ImageData>({
  infraredImage: null,
  thermalImage: null,
  processedImage: null
});

const processingStatus = ref<ProcessingStatus>({
  isProcessing: false,
  progress: 0
});

function updateImages(newImages: ImageData) {
  images.value = {...newImages};
}

function updateProcessingStatus(status: ProcessingStatus) {
  processingStatus.value = {...status};
}

function updatePanelCollapsed(collapsed: boolean) {
  isPanelCollapsed.value = collapsed;
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
</style>