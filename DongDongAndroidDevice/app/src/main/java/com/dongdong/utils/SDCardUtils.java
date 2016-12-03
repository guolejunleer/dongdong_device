package com.dongdong.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.os.StatFs;

/**
 * SD卡相关的辅助类
 */
public class SDCardUtils {
    private SDCardUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 判断SDCard是否可用
     *
     * @return state
     */
    public static boolean isSDCardEnable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡路径
     *
     * @return path
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    }

    /**
     * 获取SD卡的剩余容量 单位byte
     *
     * @return size
     */
    public static long getSDCardAllSize() {
        if (isSDCardEnable()) {
            StatFs stat = new StatFs(getSDCardPath());
            // 获取空闲的数据块的数量
            long availableBlocks = (long) stat.getAvailableBlocks() - 4;
            // 获取单个数据块的大小（byte）
            long freeBlocks = stat.getAvailableBlocks();
            return freeBlocks * availableBlocks;
        }
        return 0;
    }

    /**
     * 获取指定路径所在空间的剩余可用容量字节数，单位byte
     *
     * @param filePath
     * @return 容量字节 SDCard可用空间，内部存储可用空间
     */
    public static long getFreeBytes(String filePath) {
        // 如果是sd卡的下的路径，则获取sd卡可用容量
        if (filePath.startsWith(getSDCardPath())) {
            filePath = getSDCardPath();
        } else {// 如果是内部存储的路径，则获取内存存储的可用容量
            filePath = Environment.getDataDirectory().getAbsolutePath();
        }
        StatFs stat = new StatFs(filePath);
        long availableBlocks = (long) stat.getAvailableBlocks() - 4;
        return stat.getBlockSize() * availableBlocks;
    }

    /**
     * 获取系统存储路径
     *
     * @return
     */
    public static String getRootDirectoryPath() {
        return Environment.getRootDirectory().getAbsolutePath();
    }

    /**
     * 在SD卡上创建文件
     */
    public static File createDirOnSDCard(String dir) throws IOException {
        File file = new File(getSDCardPath() + dir);
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    /**
     * 写入数据到SD卡中
     */
    public static void writeData2SDCard(File desFile, InputStream data) {
        OutputStream output = null;
        try {
            output = new FileOutputStream(desFile);
            byte buffer[] = new byte[1024]; // 每次写1K数据
            int temp;
            while ((temp = data.read(buffer)) != -1) {
                output.write(buffer, 0, temp);
            }
            output.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                output.close(); // 关闭数据流操作
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * 写入数据到SD卡中
     */
    public static void writeData2SDCard(File desFile, byte[] data) {
        OutputStream output = null;
        try {
            output = new FileOutputStream(desFile);
            output.write(data);
            output.flush();

        } catch (Exception e) {
            e.printStackTrace();
            DDLog.i("SDCardUtils.class writeData2SDCard had exception:"
                    + e);
        } finally {
            try {
                output.close(); // 关闭数据流操作
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * 写入字符串数据到SD卡中
     */
    public static void writeStringData2SDCard(FileWriter desFile, String data) {
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(desFile);
            printWriter.println(data);
            printWriter.flush();

        } catch (Exception e) {
            e.printStackTrace();
            DDLog.i("SDCardUtils.class writeStringData2SDCard had exception:" + e);
        } finally {
            try {
                printWriter.close(); // 关闭数据流操作
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public static List<String> readStringData4SDard(FileReader fileReader) {
        BufferedReader bfr = new BufferedReader(fileReader);
        List<String> dataList = new ArrayList<String>();
        try {
            String line = null;
            while ((line = bfr.readLine()) != null) {
                DDLog.i("readStringData4SDard : " + line);
                dataList.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readData4SDCard(String path) {
        File file;
        InputStream input = null;
        ByteArrayOutputStream baos = null;
        String data = "0";
        try {
            file = new File(path);
            DDLog.i("SDCardUtils.class readData4SDCard file:" + file + "; exists:" + file.exists());
            if (!file.exists()) {
                return data;
            }
            input = new FileInputStream(file);
            byte buffer[] = new byte[1024]; // 每次写1K数据
            int temp;
            baos = new ByteArrayOutputStream((int) file.length());
            while ((temp = input.read(buffer)) != -1) {
                baos.write(buffer, 0, temp);
            }
            data = new String(baos.toByteArray());
            DDLog.i("SDCardUtils.class readData4SDCard data:" + data);
        } catch (Exception e) {
            e.printStackTrace();
            DDLog.i("SDCardUtils.class readData4SDCard had exception:"
                    + e);
        } finally {
            try {
                if (input != null)
                    input.close(); // 关闭数据流操作
                if (baos != null)
                    baos.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return data;
    }

}
