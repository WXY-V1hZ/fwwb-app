export interface ApiResponse<T> {
  status: string;
  code: number;
  info: string;
  data: T;
}

export interface VideoData {
  infraredVideo: string | null;
  thermalVideo: string | null;
  processedVideo: string | null;
}

export interface HistoryItem extends VideoData {
  timestamp: number;
}

export interface ProcessingStatus {
  isProcessing: boolean;
  progress: number;
}