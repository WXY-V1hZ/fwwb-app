export interface ApiResponse<T> {
  status: string;
  code: number;
  info: string;
  data: T;
}

export interface ImageData {
  infraredImage: string | null;
  thermalImage: string | null;
  processedImage: string | null;
}

export interface HistoryItem extends ImageData {
  timestamp: number;
}

export interface ProcessingStatus {
  isProcessing: boolean;
  progress: number;
}