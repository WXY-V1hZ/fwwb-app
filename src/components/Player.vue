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
}>();

const playerContainer = ref<HTMLElement | null>(null);
let art: any = null;

// 创建HLS实例并加载视频
const createHls = (video: HTMLVideoElement, url: string) => {
  if (Hls.isSupported()) {
    const hls = new Hls();
    hls.loadSource(url);
    hls.attachMedia(video);
    
    hls.on(Hls.Events.MANIFEST_PARSED, function() {
      video.play().catch(() => {
        // 自动播放失败，可能是因为浏览器策略
        console.log('自动播放被阻止，请点击播放按钮开始播放');
      });
    });
    
    art.hls = hls;
  } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
    // 对于原生支持HLS的浏览器（如Safari）
    video.src = url;
  }
};

const initializePlayer = () => {
  if (playerContainer.value && props.src) {
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
      controls: [
        {
          position: 'right',
          html: '字幕',
          tooltip: '开关字幕',
          style: {
            color: '#fff',
          },
          click: function () {
            art.subtitle.show = !art.subtitle.show;
          },
        },
      ],
      icons: {
        loading: '<svg xmlns="http://www.w3.org/2000/svg" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><path d="M16 12a4 4 0 1 1-8 0 4 4 0 0 1 8 0z"></path></svg>',
      },
      customType: {
        m3u8: function(video: HTMLVideoElement, url: string) {
          createHls(video, url);
        },
      },
    });
  }
};

// 当组件挂载时创建播放器
onMounted(() => {
  if (props.src) {
    initializePlayer();
  }
});

// 监听src变化
watch(() => props.src, (newSrc) => {
  // 销毁之前的实例
  if (art) {
    art.destroy();
    art = null;
  }
  
  // 如果有新的src，创建新的播放器实例
  if (newSrc) {
    setTimeout(() => {
      initializePlayer();
    }, 0);
  }
});

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
}
</style>