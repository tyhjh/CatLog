package com.display.loglibrary.impl;

import android.util.Log;

import com.display.loglibrary.FileUtil;
import com.display.loglibrary.IFileWrite;
import com.display.loglibrary.entity.LogInfo;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 内存映射实现日志文件写入
 *
 * @author hanpei
 * @version 1.0, 2020/4/20
 * @since 产品模块版本
 */
public class MMapFileWriter implements IFileWrite {


    private static final String LINE_SEP = System.getProperty("line.separator");


    /**
     * 日志文件的开头
     */
    private static final String LOG_FILE_NAME = "LOG-";

    /**
     * 日志文件保存的文件类型
     */
    private static final String LOG_FILE_TYPE = ".txt";


    /**
     * 日志文件保存的文件类型
     */
    private static final String LOG_ZIP_FILE_TYPE = ".zip";

    /**
     * 当前日志文件名
     */
    private String currentFileName;


    /**
     * 文件存储路径
     */
    private String mFilePath;


    private MappedByteBuffer mByteBuffer;
    private RandomAccessFile mRandomAccessFile;
    private File mLogFile;

    /**
     * 用于映射头部文件大小信息
     */
    private MappedByteBuffer mByteBufferHead;

    /**
     * 每次映射文件大小单位为byte
     */
    private static long MAP_FILE_SIZE = 1 * 50 * 1024;


    /**
     * 日志文件最大空间单位为byte
     */
    private long logFileMaxSize = 50 * 1024 * 1024;

    /**
     * 可进行压缩的文件大小
     */
    private static long ZIP_FILE_SIZE = 1024 * 1024;

    /**
     * 一个long类型占的字节数
     */
    private static final int LONG_SIZE = 8;

    @Override
    public void writeLogToFile(LogInfo logInfo) {
        if (logInfo == null) {
            return;
        }
        try {
            //日志文件名改变，日期改变时重新创建日志文件
            if (currentFileName == null || !getFileName().equals(currentFileName)) {
                if (!getFileName().equals(currentFileName)) {
                    //对日志进行检查、清除过期日志
                    checkLogFile();
                }
                //创建日志文件
                createLogFile();
            }

            //日志内容的大小
            long logContentSize = 0;

            //文件大小映射
            if (mByteBufferHead == null) {
                //日志文件存在
                if (mLogFile.exists()) {
                    mRandomAccessFile = new RandomAccessFile(mLogFile, "rw");
                    //映射日志文件到内存
                    mByteBufferHead = mRandomAccessFile.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, LONG_SIZE);
                    logContentSize = mByteBufferHead.getLong(0);
                    Log.i("TAG", "logContentSize is " + logContentSize);
                }
            }

            //映射未开启
            if (mByteBuffer == null) {
                //日志文件存在
                if (mLogFile.exists()) {
                    if (mRandomAccessFile == null) {
                        mRandomAccessFile = new RandomAccessFile(mLogFile, "rw");
                    }
                    //从头部信息以外的空间开始映射
                    long fileSize = Math.max(logContentSize, LONG_SIZE);
                    //映射日志文件到内存
                    mByteBuffer = mRandomAccessFile.getChannel().map(FileChannel.MapMode.READ_WRITE, fileSize, MAP_FILE_SIZE);
                }
            }

            Log.i("TAG", "mByteBufferHead is " + mByteBufferHead.getLong(0));

            //映射开启成功
            if (mByteBuffer != null) {
                //获取已经写入的文件的大小
                int position = mByteBuffer.position();
                //日志内容
                String msg = getLogInfo(logInfo);
                byte[] msgByte = msg.getBytes();
                int remainSize = (int) (MAP_FILE_SIZE - position);
                //映射空间大小超出了设置大小
                if (remainSize < msgByte.length) {
                    //写完剩余空间
                    byte[] bytes = Arrays.copyOfRange(msgByte, 0, remainSize);
                    put(bytes);
                    //剩下的部分
                    byte[] remain = Arrays.copyOfRange(msgByte, remainSize, msgByte.length);
                    mByteBuffer.force();
                    mByteBuffer = null;
                    //对文件大小进行判断
                    checkLogFile();
                    //如果文件被压缩了
                    if (mLogFile == null || !mLogFile.exists()) {
                        createLogFile();
                    }
                    long fileSize = mLogFile.length();
                    //映射日志文件到内存
                    mByteBuffer = mRandomAccessFile.getChannel().map(FileChannel.MapMode.READ_WRITE, fileSize, MAP_FILE_SIZE);
                    //写入剩下的部分
                    put(remain);
                } else {
                    //写入文件
                    put(msgByte);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (mByteBufferHead != null) {
                    mByteBufferHead.force();
                    mByteBufferHead = null;
                }
                if (mByteBuffer != null) {
                    mByteBuffer.force();
                    mRandomAccessFile.close();
                    mByteBuffer = null;
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }


    private void put(byte[] msgByte) {
        //写入文件
        mByteBuffer.put(msgByte);
        long size = mByteBufferHead.getLong(0);
        if (size == 0) {
            size = LONG_SIZE;
        }
        mByteBufferHead.putLong(0, size + msgByte.length);
    }

    @Override
    public void setLogStorageSize(long logFileMaxSize) {
        this.logFileMaxSize = logFileMaxSize;
    }

    @Override
    public void setLogPath(String filePath) {
        this.mFilePath = filePath;
        //进行文件夹路径补充
        if (!mFilePath.endsWith(File.separator)) {
            mFilePath = mFilePath + File.separator;
        }
        //创建日志文件
        createLogFile();
        //对日志进行清理
        checkLogFile();
    }


    /**
     * 对日志文件进行整理
     */
    private void checkLogFile() {
        //压缩日志
        zipLogFile();
        //清理日志
        clearLogFile();
    }


    /**
     * 清理过期日志
     */
    private void clearLogFile() {
        if (mLogFile == null) {
            return;
        }
        File[] files = mLogFile.getParentFile().listFiles();
        if (files == null) {
            return;
        }
        long fileSize = 0;
        List<File> logFileList = new ArrayList<>();
        for (File file : files) {
            if (file.getName().startsWith(LOG_FILE_NAME)) {
                //计算日志文件的大小
                fileSize = fileSize + file.length();
                logFileList.add(file);
            }
        }
        if (logFileMaxSize <= fileSize) {
            List<File> fileList = FileUtil.getFileSort(logFileList);
            //获取最后创建的日志文件
            File writingFile = fileList.get(fileList.size() - 1);
            //日志文件为正在打印的文件,并且大于1M进行压缩
            if (writingFile.getName().equals(getFileName()) && writingFile.length() > ZIP_FILE_SIZE) {
                Date now = new Date(System.currentTimeMillis());
                Format FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS ", Locale.getDefault());
                String format = FORMAT.format(now);
                String time = format.substring(6);
                //进行文件压缩
                FileUtil.compressFile(writingFile, new File(mFilePath + writingFile.getName().replace(LOG_FILE_TYPE, "_" + time + LOG_ZIP_FILE_TYPE)));
            }
            fileSize = fileSize - writingFile.length();
            writingFile.delete();
            if (logFileMaxSize > fileSize) {
                //空间足够退出
                return;
            }
            fileList.remove(fileList.size() - 1);
            for (File file : fileList) {
                if (file.getName().startsWith(LOG_FILE_NAME)) {
                    fileSize = fileSize - file.length();
                    file.delete();
                    if (logFileMaxSize > fileSize) {
                        //空间足够退出
                        break;
                    }
                }
            }
        }
    }


    /**
     * 压缩日志文件
     */
    private void zipLogFile() {
        if (mLogFile == null) {
            return;
        }
        //获取当前文件夹的文件
        File[] files = mLogFile.getParentFile().listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            //如果是日志文件，并且不是当前写入日志文件
            if (file.getName().startsWith(LOG_FILE_NAME) && file.getName().endsWith(LOG_FILE_TYPE) && !file.getName().equals(getFileName())) {
                //进行文件压缩
                FileUtil.compressFile(file, new File(mFilePath + file.getName().replace(LOG_FILE_TYPE, LOG_ZIP_FILE_TYPE)));
                //删除源文件
                file.delete();
            }
        }
    }

    /**
     * 拼接日志内容
     *
     * @param logInfo
     * @return
     */
    private String getLogInfo(LogInfo logInfo) {
        Date now = new Date(System.currentTimeMillis());
        Format FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS ", Locale.getDefault());
        String format = FORMAT.format(now);
        String time = format.substring(6);
        StringBuilder sb = new StringBuilder();
        sb.append(time)
                .append(getLogLevel(logInfo.getLevel()))
                .append("/")
                .append(logInfo.getTag() + " ")
                .append(logInfo.getFrom() + "：")
                .append(logInfo.getContent())
                .append(LINE_SEP);
        final String content = sb.toString();
        return content;
    }


    /**
     * 获取日志的等级
     *
     * @param level
     * @return
     */
    private String getLogLevel(int level) {
        switch (level) {
            case Log.VERBOSE:
                return "V";
            case Log.DEBUG:
                return "D";
            case Log.INFO:
                return "I";
            case Log.WARN:
                return "W";
            case Log.ERROR:
                return "E";
            case Log.ASSERT:
                return "A";
            default:
                return "I";
        }
    }


    /**
     * 创建日志文件
     */
    private boolean createLogFile() {
        try {
            //获取文件名
            String fileName = getFileName();
            currentFileName = fileName;
            File logDir = new File(mFilePath);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            //创建日志文件
            mLogFile = new File((mFilePath + fileName));
            if (!mLogFile.exists()) {
                mLogFile.createNewFile();
            }
            if (mLogFile.exists()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取文件名字
     */
    private String getFileName() {
        Date now = new Date(System.currentTimeMillis());
        Format FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS ", Locale.getDefault());
        String format = FORMAT.format(now);
        String date = format.substring(0, 5);
        return LOG_FILE_NAME + date + LOG_FILE_TYPE;
    }


}
