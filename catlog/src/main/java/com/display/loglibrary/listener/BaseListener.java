package com.display.loglibrary.listener;

/**
 * 监听基类
 *
 * @author hanpei
 * @version 1.0, 2019/10/30
 * @since 产品模块版本
 */
public interface BaseListener<T> {

    /**
     * 数据返回
     *
     * @param data
     */
    void onDataBack(BaseResultData<T> data);

}
