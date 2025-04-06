<template>
    <div class="results-summary" v-if="showSummary">
      <div class="summary-header">
        <h3>检测结果分析</h3>
        <button class="close-summary" @click="closeSummary">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>
      
      <div class="summary-content">
        <div class="detection-stats">
          <div class="stat-item">
            <div class="stat-icon person-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </div>
            <div class="stat-details">
              <div class="stat-value">{{ detectedPeople }}</div>
              <div class="stat-label">检测到人体目标</div>
            </div>
          </div>
          
          <div class="stat-item">
            <div class="stat-icon confidence-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
            </div>
            <div class="stat-details">
              <div class="stat-value">{{ averageConfidence }}%</div>
              <div class="stat-label">平均置信度</div>
            </div>
          </div>
          
          <div class="stat-item">
            <div class="stat-icon time-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
            </div>
            <div class="stat-details">
              <div class="stat-value">{{ processingTime }}ms</div>
              <div class="stat-label">处理耗时</div>
            </div>
          </div>
        </div>
        
        <div class="detection-details">
          <div class="detail-title">检测详情：</div>
          <div class="detail-list">
            <div v-for="(person, index) in peopleDetails" :key="index" class="detail-item">
              <div class="detail-indicator" :style="{ backgroundColor: getColorForConfidence(person.confidence) }"></div>
              <div class="detail-text">
                人体 #{{ index + 1 }} - 置信度: {{ person.confidence }}%
                <span class="detail-location">位置: {{ person.location }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="summary-notes">
          <div class="note-item">
            <div class="note-icon warning-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
            </div>
            <div class="note-text">
              置信度低于 70% 的检测结果可能需要人工确认
            </div>
          </div>
          
          <div class="note-item">
            <div class="note-icon info-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
            </div>
            <div class="note-text">
              浓烟环境下的检测结果仅供参考，请结合实际情况判断
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, defineProps } from 'vue';
  
  // 在实际应用中，这些数据应该从处理结果中获取
  // 这里使用模拟数据进行示例
  const props = defineProps<{
    visible: boolean;
  }>();
  
  const showSummary = ref(props.visible);
  
  // 模拟数据
  const detectedPeople = ref(3);
  const averageConfidence = ref(85);
  const processingTime = ref(467);
  const peopleDetails = ref([
    { confidence: 92, location: '中下方' },
    { confidence: 87, location: '右上方' },
    { confidence: 76, location: '左上方' }
  ]);
  
  function getColorForConfidence(confidence: number): string {
    if (confidence >= 90) return '#4caf50'; // 绿色
    if (confidence >= 75) return '#2196f3'; // 蓝色
    if (confidence >= 60) return '#ff9800'; // 橙色
    return '#f44336'; // 红色
  }
  
  function closeSummary() {
    showSummary.value = false;
  }
  </script>
  
  <style scoped>
  .results-summary {
    position: absolute;
    bottom: 20px;
    right: 20px;
    width: 350px;
    background-color: white;
    border-radius: 10px;
    box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);
    overflow: hidden;
    animation: slide-up 0.3s ease-out;
    z-index: 100;
  }
  
  @keyframes slide-up {
    from {
      opacity: 0;
      transform: translateY(30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  .summary-header {
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--dark-color) 100%);
    color: white;
    padding: 12px 15px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .summary-header h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 500;
  }
  
  .close-summary {
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    display: flex;
    padding: 2px;
    border-radius: 50%;
    transition: background-color 0.2s;
  }
  
  .close-summary:hover {
    background-color: rgba(255, 255, 255, 0.2);
  }
  
  .summary-content {
    padding: 15px;
  }
  
  .detection-stats {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
  }
  
  .stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
    padding: 10px;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 10px;
  }
  
  .person-icon {
    background-color: #e3f2fd;
    color: #1976d2;
  }
  
  .confidence-icon {
    background-color: #e8f5e9;
    color: #388e3c;
  }
  
  .time-icon {
    background-color: #fff3e0;
    color: #f57c00;
  }
  
  .stat-details {
    text-align: center;
  }
  
  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #333;
    line-height: 1.2;
  }
  
  .stat-label {
    font-size: 12px;
    color: #777;
  }
  
  .detection-details {
    background-color: #f5f5f5;
    border-radius: 6px;
    padding: 12px;
    margin-bottom: 15px;
  }
  
  .detail-title {
    font-weight: 500;
    margin-bottom: 8px;
    color: #555;
  }
  
  .detail-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .detail-item {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .detail-indicator {
    width: 8px;
    height: 8px;
    border-radius: 50%;
  }
  
  .detail-text {
    font-size: 14px;
    color: #333;
  }
  
  .detail-location {
    font-size: 12px;
    color: #777;
    margin-left: 8px;
  }
  
  .summary-notes {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .note-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
  }
  
  .note-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
  
  .warning-icon {
    color: #f57c00;
  }
  
  .info-icon {
    color: #0288d1;
  }
  
  .note-text {
    font-size: 13px;
    color: #666;
    line-height: 1.4;
  }
  
  @media (max-width: 768px) {
    .results-summary {
      width: 90%;
      left: 5%;
      right: 5%;
    }
    
    .detection-stats {
      flex-direction: column;
      gap: 15px;
    }
    
    .stat-item {
      flex-direction: row;
      gap: 15px;
      align-items: center;
    }
    
    .stat-icon {
      margin-bottom: 0;
    }
    
    .stat-details {
      text-align: left;
    }
  }
  </style>