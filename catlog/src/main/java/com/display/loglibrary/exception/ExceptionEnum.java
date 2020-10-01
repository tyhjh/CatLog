package com.display.loglibrary.exception;

/**
 * 异常枚举类
 *
 * @author hanpei
 * @version 1.0, 2020/3/28
 * @since 产品模块版本
 */
public enum ExceptionEnum {

    ;


    /**
     * 错误码
     */
    private int code;

    /**
     * 错误信息
     */
    private String msg;


    ExceptionEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
