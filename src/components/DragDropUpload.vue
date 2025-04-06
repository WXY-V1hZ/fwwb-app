<template>
    <div class="drag-drop-container" :class="{ 'drag-active': isDragActive, 'with-preview': !!previewImage }"
        @dragenter="onDragEnter" @dragleave="onDragLeave" @dragover.prevent @drop="onDrop">
        <div v-if="previewImage" class="preview-container">
            <img :src="previewImage" alt="图像预览" />
        </div>
        <div v-else class="upload-placeholder">
            <div class="upload-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none"
                    stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="17 8 12 3 7 8"></polyline>
                    <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
            </div>
            <div class="upload-text">
                <p>拖拽或点击上传{{ label }}图像</p>
                <p class="upload-hint">支持 JPG、PNG 等常见图像格式</p>
            </div>
        </div>

        <input type="file" class="file-input" ref="fileInput" accept="image/*" @change="handleFileChange"
            :disabled="disabled" />

        <div class="controls" v-if="previewImage">
            <button class="remove-button" @click.stop="removeFile" :disabled="disabled">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                    stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="3 6 5 6 21 6"></polyline>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
                删除
            </button>
            <button class="browse-button" @click.stop="triggerFileInput" :disabled="disabled">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none"
                    stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                    <polyline points="17 8 12 3 7 8"></polyline>
                    <line x1="12" y1="3" x2="12" y2="15"></line>
                </svg>
                更换
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits } from 'vue';

const props = defineProps<{
    label: string;
    previewImage: string | null;
    disabled: boolean;
}>();

const emit = defineEmits<{
    'file-selected': [file: File];
    'file-removed': [];
}>();

const isDragActive = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);

function onDragEnter(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    if (props.disabled) return;
    isDragActive.value = true;
}

function onDragLeave(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    isDragActive.value = false;
}

function onDrop(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
    if (props.disabled) return;

    isDragActive.value = false;

    if (!e.dataTransfer) return;

    const files = e.dataTransfer.files;
    if (files.length > 0) {
        const file = files[0];
        if (file.type.startsWith('image/')) {
            emit('file-selected', file);
        } else {
            alert('请上传图片文件');
        }
    }
}

function handleFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];
    emit('file-selected', file);

    // 重置 input 值，允许选择相同文件
    if (fileInput.value) {
        fileInput.value.value = '';
    }
}

function triggerFileInput() {
    if (props.disabled) return;
    if (fileInput.value) {
        fileInput.value.click();
    }
}

function removeFile() {
    if (props.disabled) return;
    emit('file-removed');
    if (fileInput.value) {
        fileInput.value.value = '';
    }
}
</script>

<style scoped>
.drag-drop-container {
    width: 100%;
    height: 140px;
    border: 2px dashed #ccc;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
    transition: all 0.3s ease;
    cursor: pointer;
    background-color: #f8f9fa;
}

.drag-drop-container:hover {
    background-color: #f0f7fa;
    border-color: var(--secondary-color);
}

.drag-active {
    border-color: var(--primary-color);
    background-color: rgba(3, 169, 244, 0.05);
    transform: scale(1.02);
}

.file-input {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    opacity: 0;
    cursor: pointer;
    z-index: 1;
}

.upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 20px;
    text-align: center;
}

.upload-icon {
    color: var(--secondary-color);
    margin-bottom: 10px;
}

.upload-text {
    color: var(--text-secondary);
}

.upload-hint {
    font-size: 12px;
    color: #999;
    margin-top: 5px;
}

.with-preview {
    padding: 0;
    border-style: solid;
    border-color: var(--border-color);
}

.preview-container {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}

.preview-container img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
}

.controls {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: space-between;
    padding: 8px;
    background-color: rgba(0, 0, 0, 0.5);
    opacity: 0;
    transition: opacity 0.2s ease;
}

.drag-drop-container:hover .controls {
    opacity: 1;
}

.remove-button,
.browse-button {
    background-color: rgba(255, 255, 255, 0.9);
    border: none;
    border-radius: 4px;
    padding: 5px 10px;
    font-size: 12px;
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
    transition: all 0.2s ease;
}

.remove-button {
    color: #f44336;
}

.browse-button {
    color: var(--primary-color);
}

.remove-button:hover {
    background-color: #fee;
}

.browse-button:hover {
    background-color: #e9f5fb;
}

.remove-button:disabled,
.browse-button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}
</style>