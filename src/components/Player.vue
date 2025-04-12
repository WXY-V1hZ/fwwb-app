<template>
  <div class="player-panel">
    <div ref="playerRef" class="player-style"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, defineProps, watch } from 'vue'
import Hls from 'hls.js'
import Artplayer from 'artplayer'

const props = defineProps<{
  src: string;
  poster?: string;
  dateFolder: string | null;
  folderPath: string | null;
}>()

const playerRef = ref<HTMLElement | null>(null)
let player: any = null

const initPlayer = () => {
  // Hide right-click menu
  Artplayer.CONTEXTMENU = false
  // Maximum number of records for auto playback, default is 10
  Artplayer.AUTO_PLAYBACK_MAX = 20
  // Minimum record duration for auto playback, in seconds, default is 5
  Artplayer.AUTO_PLAYBACK_MIN = 10

  player = new Artplayer({
    container: playerRef.value as HTMLElement,
    url: props.src,
    poster: props.poster,
    type: 'm3u8',
    customType: {
      m3u8: function (video: HTMLVideoElement, url: string, art: any) {
        if (Hls.isSupported()) {
          if (art.hls) art.hls.destroy()
          
          const hls = new Hls({
            // Override the load method to fix TS segment URLs
            xhrSetup: function(xhr, url) {
              // Check if this is a TS file request
              if (url.endsWith('.ts')) {
                // Get the folder path from original HLS source URL
                const baseUrl = url.substring(0, url.lastIndexOf('/') + 1);
                // Get just the TS filename
                const tsFile = url.substring(url.lastIndexOf('/') + 1);
                
                // Skip if already has the full path
                if (!url.includes(props.folderPath)) {
                  // Construct correct URL with date and folderPath
                  const correctedUrl = `${baseUrl}${props.folderPath}/${tsFile}`;
                  console.log('Corrected TS URL:', correctedUrl);
                  xhr.open('GET', correctedUrl, true);
                  return;
                }
              }
              // Default behavior for non-TS files or already corrected URLs
              xhr.open('GET', url, true);
            }
          });
          
          console.log('Loading HLS source:', url);
          hls.loadSource(url);
          hls.attachMedia(video);
          art.hls = hls;
          art.on('destroy', () => hls.destroy());
        } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
          video.src = url
        } else {
          art.notice.show = '浏览器不支持该播放器'
        }
      },
    },
    theme: '#03a9f4', // Primary theme color for progress bar and highlighted elements
    volume: 0.7, // Default volume
    autoplay: false, // Don't autoplay by default
    autoMini: false, // Don't minimize when scrolled out of view
    fullscreen: true, // Allow fullscreen
    fullscreenWeb: true, // Allow web fullscreen
    setting: true, // Show settings panel
    pip: true, // Enable picture-in-picture
    playbackRate: true, // Show playback speed controls
    flip: true, // Enable video flip
    aspectRatio: true, // Enable aspect ratio controls
    screenshot: true, // Enable screenshot
    autoPlayback: true, // Enable auto playback
    controls: [
      {
        name: 'replay',
        position: 'right',
        html: '<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="1 4 1 10 7 10"></polyline><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"></path></svg>',
        tooltip: '重新播放',
        style: {
          color: '#fff',
          display: 'flex',
        },
        click: function () {
          player.seek = 0
          player.play = true
        },
      }
    ],
  })

  // Show controls on hover
  player.on('hover', (state: boolean) => {
    let display = 'none'
    if (state) {
      display = 'flex'
    }
    player.template.$bottom.style.display = display
  })

  // Video end event
  player.on('video:ended', () => {
    console.log('Video playback ended')
  })
  
  // Adjust player dimensions on window resize
  window.addEventListener('resize', () => {
    if (player) {
      player.reSize()
    }
  })
}

// Cleanup on component unmount
onBeforeUnmount(() => {
  if (player) {
    player.destroy(false)
  }
  window.removeEventListener('resize', () => {
    console.log('Removed resize listener')
  })
})

onMounted(() => {
  nextTick(() => {
    if (playerRef.value) {
      initPlayer()
    }
  })
})

// Watch for changes to the source
watch(() => props.src, (newSrc, oldSrc) => {
  if (newSrc !== oldSrc && player) {
    player.switchUrl(newSrc, props.poster)
  }
})

// Method to switch videos
const switchVideo = (url: string, poster?: string) => {
  if (player) {
    player.switchUrl(url, poster)
  }
}

// Expose methods to parent component
defineExpose({
  switchVideo
})
</script>

<style scoped>
.player-panel {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 确保播放器完全适应容器 */
.player-style {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
  background-color: #000;
}

/* 确保视频容器正确显示 */
:deep(.art-video-player) {
  width: 100% !important;
  height: 100% !important;
  border-radius: 8px !important;
  overflow: hidden !important;
}

:deep(.art-video) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 控制条样式调整 */
:deep(.art-control) {
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent) !important;
}

/* 响应式处理 */
@media (max-width: 768px) {
  .player-panel {
    height: 100%;
  }
}
</style>