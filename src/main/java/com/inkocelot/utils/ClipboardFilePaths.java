package com.inkocelot.utils;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;

@Slf4j
public class ClipboardFilePaths {
    public static String get() {
        try {
            // 获取系统剪贴板
            var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            // 获取剪贴板内容
            var transferable = clipboard.getContents(null);

            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                @SuppressWarnings("unchecked")
                var files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                var path = files.get(0).getAbsolutePath();
                log.info("文件路径已获取, 路径为{}", path);
                return path;
            } else {
                log.warn("警告, 剪贴板中不存在文件");
                return null;
            }
        } catch (Exception e) {
            if (!e.getMessage().contains("java.lang.ClassNotFoundException: com/intellij/")) {
                // 忽略IntelliJ相关的异常
                log.error("读取中发生异常 - {}", e.getMessage(), e);
            }
        }
        return null;
    }
}
