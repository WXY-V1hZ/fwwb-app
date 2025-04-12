import axios from 'axios';

const api = {
  // 上传视频
  uploadVideo(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return axios.post('/api/uploadVideo', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      }
    });
  },

  // 获取图片资源（用于视频缩略图）
  getImageResource(imagePath: string) {
    return `/api/imageResource?imagePath=${imagePath}`;
  },

  // 获取视频资源的m3u8文件
  getVideoResource(date: string, folderPath: string) {
    return `/api/videoResource/${date}/${folderPath}`;
  },

  // 获取处理结果
  getResult(sourceName1: string, sourceName2: string) {
    return axios.get(`/api/getResult`, {
      params: {
        sourceName1,
        sourceName2
      }
    });
  }
};

export default api;