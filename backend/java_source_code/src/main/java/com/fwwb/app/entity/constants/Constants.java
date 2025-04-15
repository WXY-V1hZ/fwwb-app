package com.fwwb.app.entity.constants;

import java.io.File;

public class Constants {

    public static final String BASE_PATH = getBasePath();

    // Asset paths relative to base path
    public static final String IMAGE_FOLDER = BASE_PATH + "assets/image/";
    public static final String VIDEO_FOLDER = BASE_PATH + "assets/video/";
    public static final String SCRIPT_PATH = BASE_PATH + "assets/scripts/test_run.py";

    public static final String EXAMPLES_FOLDER_NAME = "examples";

    public static final String OUTPUT_NAME = "output.mp4";
    public static final String FIRST_TS_NAME = "0000.ts";

    public static final String CMD = "python \"%s\" --folder1 \"%s\" --folder2 \"%s\"";

    public static final String M3U8_NAME = "index.m3u8";
    public static final String TS_NAME = "index.ts";
    public static final String IMAGE_SUFFIX = ".png";
    public static final String VIDEO_CODE_TEMP_FILE_SUFFIX = "_temp";
    public static final String VIDEO_CODE_HEVC = "hevc";

    private static String getBasePath() {
        try {
            String path = Constants.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            // If running from JAR, get its directory
            if (path.endsWith(".jar")) {
                path = path.substring(0, path.lastIndexOf("/") + 1);
            }

            // Ensure path ends with separator
            if (!path.endsWith(File.separator)) {
                path = path + File.separator;
            }

            // Create asset directories if they don't exist
            File imageDir = new File(path + "assets/image");
            File videoDir = new File(path + "assets/video");
            File scriptsDir = new File(path + "assets/scripts");

            if (!imageDir.exists()) imageDir.mkdirs();
            if (!videoDir.exists()) videoDir.mkdirs();
            if (!scriptsDir.exists()) scriptsDir.mkdirs();

            return path;
        } catch (Exception e) {
            // Fallback to current working directory if any error occurs
            return new File("").getAbsolutePath() + File.separator;
        }
    }
}
