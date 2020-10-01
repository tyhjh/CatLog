package com.display.loglibrary.entity;

/**
 * 日志信息类
 *
 * @author hanpei
 * @version 1.0, 2020/4/21
 * @since 产品模块版本
 */
public class LogInfo {

    /**
     * 日志内容
     */
    private String content;

    /**
     * 日志标签
     */
    private String tag;

    /**
     * 日志等级
     */
    private int level;

    /**
     * 日志出处
     */
    private String from;


    public LogInfo(String content, String tag, int level, String from) {
        this.content = content;
        this.tag = tag;
        this.level = level;
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
