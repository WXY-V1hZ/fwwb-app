package com.fwwb.app.utils;

import com.fwwb.app.entity.constants.Constants;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;

/**
 * FFmpegUtils 工具类
 * 用于调用 FFmpeg 生成图片缩略图
 */
@Component
public class FFmpegUtils {

    /**
     * 创建图片缩略图
     *
     * @param filePath 输入图片文件路径
     */
    public void createImageThumbnail(String filePath) {
        // 构造 FFmpeg 命令，缩放图片宽度为 200，高度按比例调整
        String CMD = "ffmpeg -i \"%s\" -vf scale=200:-1 \"%s\"";

        // 格式化命令，将 filePath 作为输入文件，并生成带有缩略图后缀的新文件路径
        CMD = String.format(CMD, filePath, filePath + Constants.IMAGE_SUFFIX);

        // 执行 FFmpeg 命令，生成缩略图
        ProcessUtils.executeCommand(CMD, true);
    }

    /**
     * 截取视频的第一帧并保存为图片。
     *
     * @param videoPath 视频文件的路径
     * @param imagePath 保存图片的路径
     */
    public void createVideoThumbnail(String videoPath, String imagePath) {
        // D:/Programs/FFWB/ffwb-app/backend/assets/image/20250412/E3w3lhFAUjGqTNTlsvWk.jpg
        String imageFolder = imagePath.substring(0, imagePath.lastIndexOf("/") + 1);
        File imageFolderFile = new File(imageFolder);
        if (!imageFolderFile.exists()) {
            imageFolderFile.mkdirs();
        }

        // 构造 FFmpeg 命令，提取视频的第一帧并保存为图片
        String CMD = "ffmpeg -i \"%s\" -frames:v 1 -q:v 2 \"%s\"";

        // 格式化命令，将 videoPath 和 imagePath 插入到命令中
        CMD = String.format(CMD, videoPath, imagePath);

        // 执行 FFmpeg 命令，生成视频的缩略图
        ProcessUtils.executeCommand(CMD, true);
    }

    /**
     * 获取视频文件的时长（单位：秒）。
     *
     * 此方法使用 `ffprobe` 工具来提取视频文件的时长信息。它通过构建命令行命令调用 `ffprobe`，然后处理输出结果。
     * 如果获取时长失败或返回为空，则返回 0。
     *
     * @param videoPath 视频文件的路径
     * @return 返回视频文件的时长，单位为秒。如果无法获取时长，返回 0
     */
    public Integer getVideoInfoDuration(String videoPath) {
        // 构造 ffprobe 命令，获取视频文件的时长
        String CMD = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"";
        // 将传入的视频路径插入命令中
        CMD = String.format(CMD, videoPath);

        // 执行命令并获取命令输出结果
        String result = ProcessUtils.executeCommand(CMD, true);

        // 如果结果为空，返回 0
        if (StringTools.isEmpty(result)) {
            return 0;
        }

        // 去除结果中的换行符
        result = result.replace("\n", "");

        // 将时长转换为 BigDecimal 类型，然后转换为整数，返回视频时长（秒）
        return new BigDecimal(result).intValue();
    }


    /**
     * 获取视频文件的编解码器名称。
     *
     * 此方法使用 `ffprobe` 工具来提取视频文件的编解码器信息。它通过构建命令行命令调用 `ffprobe`，然后处理输出结果。
     * 获取的编解码器信息用于标识视频文件使用的编码格式（如 `h264`、`vp9` 等）。
     *
     * @param videoPath 视频文件的路径
     * @return 返回视频文件的编解码器名称，如 `h264`。如果无法获取编解码器信息，返回空字符串
     */
    public String getVideoCodec(String videoPath) {
        // 构造 ffprobe 命令，获取视频流的编解码器名称
        String CMD = "ffprobe -v error -select_streams v:0 -show_entries stream=codec_name \"%s\"";
        // 将传入的视频路径插入命令中
        CMD = String.format(CMD, videoPath);

        // 执行命令并获取命令输出结果
        String result = ProcessUtils.executeCommand(CMD, true);

        // 去除命令输出中的换行符
        result = result.replace("\n", "");
        result = result.substring(result.indexOf("=") + 1);

        // 提取编解码器名称（去除其他附加信息部分）
        String codec = result.substring(0, result.indexOf("["));

        // 返回编解码器名称
        return codec;
    }

    /**
     * 将 HEVC 编码的视频转换为 MP4 格式（使用 H.264 编码）。
     *
     * 此方法使用 `ffmpeg` 工具将指定的 HEVC 编码视频转换为 H.264 编码的 MP4 格式。
     * 转换过程使用了 `libx264` 编解码器，并且设置了 `crf`（Constant Rate Factor）为 20，表示目标视频的质量。
     * 命令行参数中的 `-y` 表示如果输出文件已存在，自动覆盖它。
     *
     * @param newFileName 目标文件的路径和文件名，转换后的视频将保存为该文件。
     * @param videoPath 要转换的源视频文件路径，源视频应该是 HEVC 编码的。
     */
    public void convertHevc2Mp4(String newFileName, String videoPath) {
        // 构建 ffmpeg 命令，用于将 HEVC 视频转换为 H.264 编码的 MP4 文件
        String CMD = "ffmpeg -i \"%s\" -c:v libx264 -crf 20 \"%s\" -y";

        // 使用 String.format 将 newFileName 和 videoPath 插入到命令中
        CMD = String.format(CMD, newFileName, videoPath);

        // 执行命令并转换视频，获取命令输出
        ProcessUtils.executeCommand(CMD, true);
    }

    /**
     * 将指定的视频文件转换为 `.ts` 分片格式，并生成对应的 `.m3u8` 索引文件。
     * 该方法会先将原始视频转换为 H.264 + AAC 编码的中间 `.ts` 文件，
     * 然后再根据设置的时间段（如10秒）对该 `.ts` 文件进行分片处理。
     * 最终结果为一组 `.ts` 文件和一个 `.m3u8` 播放列表文件，适用于 HLS（HTTP Live Streaming）。
     *
     * @param tsFolder   输出文件夹，用于保存生成的 .ts 分片和 .m3u8 索引文件。
     * @param videoPath  输入视频文件的路径，支持 MP4、MOV、MKV 等常见格式。
     */
    public void convertVideo2Ts(File tsFolder, String videoPath) {
        String CMD_TRANSFER_2TS = "ffmpeg -y -i \"%s\" -vcodec copy -acodec copy -bsf:v h264_mp4toannexb \"%s\"";
        String CMD_CUT_TS = "ffmpeg -i \"%s\" -c copy -map 0 -f segment -segment_list \"%s\" -segment_time 10 %s/%%4d.ts";

        // 第一步：生成中间 .ts 文件
        String tsPath = tsFolder + "/" + Constants.TS_NAME;
        String CMD = String.format(CMD_TRANSFER_2TS, videoPath, tsPath);
        ProcessUtils.executeCommand(CMD, true);

        // 第二步：将中间 .ts 文件切片，并生成 m3u8 索引文件
        CMD = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath());
        ProcessUtils.executeCommand(CMD, true);

        // 删除中间 .ts 文件
        new File(tsPath).delete();
    }
}
