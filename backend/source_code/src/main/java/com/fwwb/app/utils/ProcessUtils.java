package com.fwwb.app.utils;

import com.fwwb.app.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProcessUtils {
    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);
    private static final String osName = System.getProperty("os.name").toLowerCase();

    public static String executeCommand(String cmd, boolean showLog) throws BusinessException {
        if (cmd == null || cmd.trim().isEmpty()) {
            throw new IllegalArgumentException("命令不能为空");
        }

        ProcessBuilder builder;
        if (osName.contains("win")) {
            builder = new ProcessBuilder("cmd.exe", "/c", cmd);
        } else {
            builder = new ProcessBuilder("/bin/sh", "-c", cmd);
        }

        builder.redirectErrorStream(true);

        try {
            Process process = builder.start();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<String> outputFuture = executor.submit(new StreamGobbler(process.getInputStream()));

            int exitCode = process.waitFor();
            executor.shutdown();
            String output = outputFuture.get();

            if (showLog) {
                logger.info("执行命令: {}\n结果: {}", cmd, output);
            }

            if (exitCode != 0) {
                throw new BusinessException("执行命令失败，退出码: " + exitCode);
            }

            return output;
        } catch (Exception e) {
            logger.error("执行命令失败: {}", cmd, e);
            throw new BusinessException("命令执行失败: " + e.getMessage());
        }
    }

    private static class StreamGobbler implements Callable<String> {
        private final InputStream inputStream;

        public StreamGobbler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public String call() throws Exception {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                return output.toString();
            }
        }
    }
}
