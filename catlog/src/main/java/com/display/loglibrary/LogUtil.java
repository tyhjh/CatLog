package com.display.loglibrary;

import android.app.Application;

import androidx.annotation.MainThread;

import com.display.loglibrary.impl.MMapFileWriter;

import java.util.LinkedHashMap;

/**
 * @author hanpei
 * @version 1.0, 2020/4/17
 * @since 产品模块版本
 */
public class LogUtil {

    /**
     * 日志方法所在堆栈的位置
     */
    private static final int PRINT_STACK_TRACE_POSITION = 6;

    /**
     * 打印实现类
     */
    private static HLogUtil sHLogUtil = new HLogUtil(PRINT_STACK_TRACE_POSITION);

    /**
     * 初始化
     *
     * @param application
     */
    public static void init(Application application) {
        if (application != null) {
            //初始化
            sHLogUtil.init(application);
        }
    }


    /**
     * 设置文件保存地址
     *
     * @param filePath
     */
    public static void setLogFilepath(String filePath) {
        if (filePath != null) {
            //设置文件写入实现为文件映射
            sHLogUtil.setFileWrite(new MMapFileWriter());
            sHLogUtil.setLogFilePath(filePath);
            sHLogUtil.setWriteToFile(true);
        }
    }


    /**
     * 设置打印日志的等级
     *
     * @param level
     */
    public static void setPrintLevel(int level) {
        sHLogUtil.setPrintLevel(level);
    }

    /**
     * 设置日志文件打印等级
     * @param level
     */
    public static void setPrinttFileLevel(int level){
        sHLogUtil.setPrintFileLevel(level);
    }


    /**
     * 设置日志的存储空间
     *
     * @param fileSize
     */
    public static void setLogStorageSize(long fileSize) {
        sHLogUtil.setLogStorageSize(fileSize);
    }


    /**
     * 打印日志
     *
     * @param content
     */
    public static void e(String tag, String content) {
        sHLogUtil.e(tag, content);
    }

    public static void d(String tag, String content) {
        sHLogUtil.d(tag, content);
    }

    public static void i(String tag, String content) {
        sHLogUtil.i(tag, content);
    }

    public static void v(String tag, String content) {
        sHLogUtil.v(tag, content);
    }

    public static void w(String tag, String content) {
        sHLogUtil.w(tag, content);
    }

    public static void a(String tag, String content) {
        sHLogUtil.a(tag, content);
    }


    /**
     * 打印日志
     *
     * @param content
     */
    public static void e(String content) {
        sHLogUtil.e(content);
    }

    public static void d(String content) {
        sHLogUtil.d(content);
    }

    public static void i(String content) {
        sHLogUtil.i(content);
    }

    public static void v(String content) {
        sHLogUtil.v(content);
    }

    public static void w(String content) {
        sHLogUtil.w(content);
    }

    public static void a(String content) {
        sHLogUtil.a(content);
    }


}
