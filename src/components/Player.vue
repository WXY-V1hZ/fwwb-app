<template>
  <div class="player-container" ref="playerContainer"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import Artplayer from 'artplayer';
import Hls from 'hls.js';

const props = defineProps<{
  src: string | null;
  poster: string | null;
  dateFolder: string | null;
  folderPath: string | null;
}>();

const playerContainer = ref<HTMLElement | null>(null);
let art: any = null;

// 修复HLS流播放问题
const createHls = (video: HTMLVideoElement, url: string) => {
  if (Hls.isSupported()) {
    const hls = new Hls({
      // 关键修复：添加 xhrSetup 处理 ts 文件请求路径
      xhrSetup: function(xhr, url) {
        // 检查是否是 ts 文件请求（.ts结尾）
        if (url.endsWith('.ts')) {
          // 不修改完整URL的请求
          if (url.startsWith('http')) return;
          
          // 构建完整的 ts 文件请求路径
          const baseUrl = props.src?.substring(0, props.src.lastIndexOf('/') + 1);
          const fullUrl = baseUrl + url;
          xhr.open('GET', fullUrl, true);
        }
      }
    });
    
    hls.loadSource(url);
    hls.attachMedia(video);
    
    hls.on(Hls.Events.MANIFEST_PARSED, function() {
      console.log('HLS 清单文件解析完成，准备播放');
      video.play().catch((e) => {
        console.log('自动播放被阻止，请点击播放按钮开始播放', e);
      });
    });
    
    hls.on(Hls.Events.ERROR, function(event, data) {
      console.error('HLS 错误:', data.type, data.details, data);
      
      if (data.fatal) {
        switch(data.type) {
          case Hls.ErrorTypes.NETWORK_ERROR:
            console.error('致命网络错误，尝试恢复...');
            hls.startLoad();
            break;
          case Hls.ErrorTypes.MEDIA_ERROR:
            console.error('致命媒体错误，尝试恢复...');
            hls.recoverMediaError();
            break;
          default:
            console.error('无法恢复的错误，销毁播放器');
            hls.destroy();
            break;
        }
      }
    });
    
    art.hls = hls;
  } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
    // 对于原生支持HLS的浏览器（如Safari）
    video.src = url;
  }
};

const initializePlayer = () => {
  if (playerContainer.value && props.src && props.dateFolder && props.folderPath) {
    console.log('初始化播放器，视频源:', props.src);
    
    // 创建Artplayer实例
    art = new Artplayer({
      container: playerContainer.value,
      url: props.src,
      poster: props.poster || '',
      volume: 0.5,
      autoplay: false,
      loop: false,
      muted: false,
      autoSize: true,
      autoMini: true,
      fastForward: true,
      flip: true,
      playbackRate: true,
      aspectRatio: true,
      setting: true,
      hotkey: true,
      pip: true,
      fullscreen: true,
      fullscreenWeb: true,
      lang: 'zh-cn',
      theme: '#03a9f4',
      layers: [],  // 不显示弹幕层
      icons: {
        loading: '<svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><path d="M16 12a4 4 0 1 1-8 0 4 4 0 0 1 8 0z"></path></svg>',
      },
      customType: {
        m3u8: function(video: HTMLVideoElement, url: string) {
          createHls(video, url);
        },
      },
    });
    
    // 调试事件
    art.on('ready', () => {
      console.log('播放器准备就绪');
    });
    
    art.on('error', (error: any) => {
      console.error('播放器错误:', error);
    });
  }
};

// 当组件挂载时创建播放器
onMounted(() => {
  if (props.src && props.dateFolder && props.folderPath) {
    initializePlayer();
  }
});

// 监听src变化
watch(() => [props.src, props.dateFolder, props.folderPath], ([newSrc, newDateFolder, newFolderPath]) => {
  // 销毁之前的实例
  if (art) {
    art.destroy();
    art = null;
  }
  
  // 如果有新的src，创建新的播放器实例
  if (newSrc && newDateFolder && newFolderPath) {
    setTimeout(() => {
      initializePlayer();
    }, 0);
  }
}, { deep: true });

// 组件销毁前清理
onBeforeUnmount(() => {
  if (art && art.hls) {
    art.hls.destroy();
  }
  if (art) {
    art.destroy();
    art = null;
  }
});
</script>

<style scoped>
.player-container {
  width: 100%;
  height: 100%;
  background-color: #000;
  overflow: hidden;
  position: relative;
  border-radius: 8px;
}

:deep(.art-video-player) {
  border-radius: 8px;
}

:deep(.art-danmuku) {
  display: none !important;
}
</style>