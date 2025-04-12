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
  dateFolder: string | null;
  folderPath: string | null;
}>()

const playerRef = ref<HTMLElement | null>(null)
let player: any = null

const initPlayer = () => {
  Artplayer.CONTEXTMENU = false
  Artplayer.AUTO_PLAYBACK_MAX = 20
  Artplayer.AUTO_PLAYBACK_MIN = 10

  player = new Artplayer({
    container: playerRef.value as HTMLElement,
    url: props.src,
    type: 'm3u8',
    customType: {
      m3u8: function (video: HTMLVideoElement, url: string, art: any) {
        if (Hls.isSupported()) {
          if (art.hls) art.hls.destroy()

          const hls = new Hls({
            xhrSetup: function (xhr, url) {
              if (url.endsWith('.ts')) {
                const baseUrl = url.substring(0, url.lastIndexOf('/') + 1);
                // Get just the TS filename
                const tsFile = url.substring(url.lastIndexOf('/') + 1);

                if (!url.includes(props.folderPath)) {
                  const correctedUrl = `${baseUrl}${props.folderPath}/${tsFile}`;
                  console.log('Corrected TS URL:', correctedUrl);
                  xhr.open('GET', correctedUrl, true);
                  return;
                }
              }
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
    theme: '#03a9f4',
    volume: 0.7,
    autoplay: false,
    autoMini: false,
    fullscreen: true,
    fullscreenWeb: true,
    setting: true,
    pip: true,
    playbackRate: true,
    flip: true,
    aspectRatio: true,
    screenshot: true,
    autoPlayback: true,
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

  player.on('hover', (state: boolean) => {
    let display = 'none'
    if (state) {
      display = 'flex'
    }
    player.template.$bottom.style.display = display
  })

  player.on('video:ended', () => {
    console.log('Video playback ended')
  })

  window.addEventListener('resize', () => {
    if (player) {
      player.reSize()
    }
  })
}

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

watch(() => props.src, (newSrc, oldSrc) => {
  if (newSrc !== oldSrc && player) {
    player.switchUrl(newSrc)
  }
})

const switchVideo = (url: string) => {
  if (player) {
    player.switchUrl(url)
  }
}

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

.player-style {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
  background-color: #000;
}

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

:deep(.art-control) {
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent) !important;
}

@media (max-width: 768px) {
  .player-panel {
    height: 100%;
  }
}
</style>