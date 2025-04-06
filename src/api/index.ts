import axios from 'axios';

const api = {
  // 上传图片
  uploadImage(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return axios.post('/api/uploadImage', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      }
    });
  },

  // 获取资源
  getResource(sourceName: string) {
    return `/api/getResource?sourceName=${sourceName}`;
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