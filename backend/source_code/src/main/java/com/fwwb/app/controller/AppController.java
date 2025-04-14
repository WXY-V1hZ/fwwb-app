package com.fwwb.app.controller;

import com.fwwb.app.entity.constants.Constants;
import com.fwwb.app.entity.enums.ResponseCodeEnum;
import com.fwwb.app.entity.vo.ResponseVO;
import com.fwwb.app.exception.BusinessException;
import com.fwwb.app.utils.DateUtil;
import com.fwwb.app.utils.FFmpegUtils;
import com.fwwb.app.utils.ProcessUtils;
import com.fwwb.app.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

@RestController
@RequestMapping("/")
@Validated
public class AppController extends ABaseController {

    private static final Logger log = LoggerFactory.getLogger(AppController.class);

    @Resource
    private FFmpegUtils ffmpegUtils;

    @RequestMapping("/imageResource")
    public void getResource(HttpServletResponse response, @NotEmpty String imagePath) {
        if (!StringTools.pathIsOk(imagePath)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String suffix = StringTools.getFileSuffix(imagePath);
        if (suffix != null) {
            response.setContentType("image/" + suffix.replace(".", ""));
        }
        response.setHeader("Cache-Control", "max-age=259200");

        readFile(response, Constants.IMAGE_FOLDER + imagePath);
    }

    @RequestMapping("/uploadVideo")
    public ResponseVO uploadVideo(@NotNull MultipartFile file) throws IOException {
        String day = DateUtil.format(new Date(), "yyyyMMdd");
        String folder = Constants.VIDEO_FOLDER + day;

        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }

        String fileName =file.getOriginalFilename();
        String fileSuffix = null;

        if (fileName != null && fileName.contains(".")) {
            fileSuffix = StringTools.getFileSuffix(fileName);
        } else {
            throw new IllegalArgumentException("无效的文件名");
        }

        String realFileName = StringTools.getRandomString(20) + fileSuffix;
        String filePath = folder + "/" + realFileName;

        file.transferTo(new File(filePath));

        createVideoThumbnail(filePath);

        return getSuccessResponseVO(day + "/" + realFileName);
    }

    @RequestMapping("/getResult")
    public ResponseVO getResult(@NotEmpty String sourceName1, @NotEmpty String sourceName2) {
        String scriptPath = Constants.SCRIPT_PATH;
        String videoFolder = Constants.VIDEO_FOLDER;
        String cmd = String.format(Constants.CMD, scriptPath,
                Constants.VIDEO_FOLDER + sourceName1,
                Constants.VIDEO_FOLDER + sourceName2);

        ProcessUtils.executeCommand(cmd, true);

        String date = sourceName1.substring(0, sourceName1.lastIndexOf("/"));
        String fileName1 = sourceName1.substring(sourceName1.lastIndexOf("/") + 1, sourceName1.lastIndexOf("."));
        String fileName2 = sourceName2.substring(sourceName2.lastIndexOf("/") + 1, sourceName2.lastIndexOf("."));

        String outputFolder = date + "/" + fileName1 + fileName2;

        convertVideo2Ts(Constants.VIDEO_FOLDER + outputFolder + "/" + Constants.OUTPUT_NAME);

        createOutputVideoThumbnail(Constants.VIDEO_FOLDER + outputFolder + "/" + Constants.FIRST_TS_NAME);

        return getSuccessResponseVO(outputFolder + "/");
    }

    @RequestMapping("/videoResource/{date}/{folderPath}")
    public void videoResource(HttpServletResponse response,
                              @PathVariable @NotEmpty String date,
                              @PathVariable @NotEmpty String folderPath) {
        String filePath = Constants.VIDEO_FOLDER + date + "/" + folderPath + "/" + Constants.M3U8_NAME;
        readFile(response, filePath);
    }

    @RequestMapping("/videoResource/{date}/{folderPath}/{fileName}")
    public void videoResourceTs(HttpServletResponse response,
                                @PathVariable @NotEmpty String date,
                                @PathVariable @NotEmpty String folderPath,
                                @PathVariable @NotEmpty String fileName) {
        String filePath = Constants.VIDEO_FOLDER + date + "/" + folderPath + "/" + fileName;
        readFile(response, filePath);
    }

    @RequestMapping("/videoResource/examples/{id}/{folderPath}")
    public void videoSampleResource(HttpServletResponse response,
                              @PathVariable @NotEmpty String id,
                              @PathVariable @NotEmpty String folderPath) {
        String filePath = Constants.VIDEO_FOLDER + Constants.EXAMPLES_FOLDER_NAME + "/" + id + "/" + folderPath + "/" + Constants.M3U8_NAME;
        readFile(response, filePath);
        log.info("index");
        log.info(filePath);
    }

    @RequestMapping("/videoResource/examples/{id}/{folderPath}/{fileName}")
    public void videoSampleResourceTs(HttpServletResponse response,
                                @PathVariable @NotEmpty String id,
                                @PathVariable @NotEmpty String folderPath,
                                @PathVariable @NotEmpty String fileName) {
        String filePath = Constants.VIDEO_FOLDER + Constants.EXAMPLES_FOLDER_NAME + "/" + id + "/" + folderPath + "/" + fileName;
        readFile(response, filePath);
        log.info("ts");
        log.info(filePath);
    }

    private void readFile(HttpServletResponse response, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return ;
        }

        try (OutputStream out = response.getOutputStream()) {
            FileInputStream in = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件异常", e);
        }
    }

    private void convertVideo2Ts(String videoPath) {
        File videoFile = new File(videoPath);
        File tsFolder = videoFile.getParentFile();
        String codec = ffmpegUtils.getVideoCodec(videoPath);
        if (Constants.VIDEO_CODE_HEVC.equals(codec)) {
            String tempFileName = videoPath + Constants.VIDEO_CODE_TEMP_FILE_SUFFIX;
            new File(videoPath).renameTo(new File(tempFileName));
            ffmpegUtils.convertHevc2Mp4(tempFileName, videoPath);
            new File(tempFileName).delete();
        }
        ffmpegUtils.convertVideo2Ts(tsFolder, videoPath);
        videoFile.delete();
    }

    // D:/Programs/FFWB/ffwb-app/backend/assets/video/20250412/7B2xvTWVcBBKT2xTmG9R.mp4
    private void createVideoThumbnail(String videoPath) {
        String imagePath = Constants.IMAGE_FOLDER +
                videoPath.substring(
                        Constants.VIDEO_FOLDER.length(),
                        videoPath.lastIndexOf(".")
                ) + Constants.IMAGE_SUFFIX;
        ffmpegUtils.createVideoThumbnail(videoPath, imagePath);
    }

    // D:/Programs/FFWB/ffwb-app/backend/assets/video/20250412/bt8QEFvafHHfsXUaXV7t7B2xvTWVcBBKT2xTmG9R/0000.ts
    private void createOutputVideoThumbnail(String videoPath) {
        String imagePath = Constants.IMAGE_FOLDER + videoPath.substring(
                Constants.VIDEO_FOLDER.length(),
                videoPath.lastIndexOf("/")
        ) + Constants.IMAGE_SUFFIX;
        ffmpegUtils.createVideoThumbnail(videoPath, imagePath);
    }
}
