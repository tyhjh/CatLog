package com.display.loglibrary.listener;


import com.display.loglibrary.exception.ExceptionEnum;

/**
 * 返回结果基类
 *
 * @author hanpei
 * @version 1.0, 2020/3/28
 * @since 产品模块版本
 */
public class BaseResultData<T> {

    /**
     * boolean类型的变量，一般用于判断是否成功执行
     */
    private boolean success;

    /**
     * 返回结果码
     */
    private int code;


    /**
     * 错误信息返回
     */
    private String errorMsg;


    /**
     * 泛型数据，用于返回具体数据
     */
    private T mT;


    public BaseResultData() {
    }

    public BaseResultData(boolean success, int code, T t) {
        this.success = success;
        this.code = code;
        mT = t;
    }

    /**
     * 错误返回构造函数
     *
     * @param data
     * @param code1
     */
    public BaseResultData(String data, int code1) {
        code = code1;
        errorMsg = data;
    }

    public BaseResultData(T t) {
        mT = t;
        success=true;
    }

    public BaseResultData(boolean success) {
        this.success = success;
    }




    /**
     * 成功快捷返回
     */
    public static final BaseResultData SUCCESS = new BaseResultData(true);

    /**
     * 错误快捷返回
     */
    public static final BaseResultData ERROR = new BaseResultData(false);

    /**
     * 返回错误信息
     *
     * @param exceptionEnum
     * @return
     */
    public static BaseResultData error(ExceptionEnum exceptionEnum) {
        return new BaseResultData(exceptionEnum.getMsg(), exceptionEnum.getCode());
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


    public T getT() {
        return mT;
    }

    public void setT(T t) {
        mT = t;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return "BaseResultData{" +
                "success=" + success +
                ", code=" + code +
                ", mT=" + mT +
                '}';
    }
}