package com.display.loglibrary;

import com.display.loglibrary.entity.LogInfo;

/**
 * 保存日志到文件的
 *
 * @author hanpei
 * @version 1.0, 2020/4/20
 * @since 产品模块版本
 */
public interface IFileWrite {


    /**
     * 保存日志到文件
     *
     * @param logInfo
     * @return
     */
    void writeLogToFile(LogInfo logInfo);


    /**
     * 设置文件路径
     *
     * @param filePath
     */
    void setLogPath(String filePath);


    /**
     * 设置日志所占空间大小
     *
     * @param fileMaxSize
     */
    void setLogStorageSize(long fileMaxSize);


}
