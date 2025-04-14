package com.fwwb.app;

import com.fwwb.app.entity.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fwwb.app")
public class FwwbRunApplication {
    private static final Logger log = LoggerFactory.getLogger(FwwbRunApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FwwbRunApplication.class, args);
        log.info("项目启动成功");
        log.info("视频路径: {}", Constants.VIDEO_FOLDER);
        log.info("图像路径: {}", Constants.IMAGE_FOLDER);
        log.info("脚本路径: {}", Constants.SCRIPT_PATH);
    }
}
