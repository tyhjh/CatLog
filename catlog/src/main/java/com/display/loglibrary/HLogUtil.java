package com.display.loglibrary;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.display.loglibrary.entity.LogInfo;
import com.display.loglibrary.threadpool.AppExecutors;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * 日志工具
 *
 * @author hanpei
 * @version 1.0, 2020/4/21
 * @since 产品模块版本
 */
public class HLogUtil {

    /**
     * 全局的TAG
     */
    private String sGlobalTag = "TAG";

    /**
     * 调用打印方法堆栈跟踪元素数组位置
     */
    private int stackTracePosition = 5;

    /**
     * 填充信息
     */
    public static final String TOP_BORDER = "╔═══════════════════════════════════════════════════════════════════════════════════════════════════";
    private static final String SPLIT_BORDER = "╟───────────────────────────────────────────────────────────────────────────────────────────────────";
    private static final String LEFT_BORDER = "║ ";
    private static final String BOTTOM_BORDER = "╚═══════════════════════════════════════════════════════════════════════════════════════════════════";

    /**
     * 是否进行保存日志到文件，默认不保存
     */
    private boolean mWriteToFile = false;

    /**
     * 循环写入是否开启
     */
    private boolean loopOpened = false;

    /**
     * 保存的文件路径
     */
    private String mLogFilePath;


    /**
     * 日志空间大小
     */
    private long mLogFileMaxSize;


    /**
     * 打印日志等级
     */
    private int mPrintLevel;


    /**
     * 日志写入文件的等级
     */
    private int mPrintFileLevel;

    /**
     * 文件编辑器
     */
    private IFileWrite mFileWrite;


    /**
     * 日志写入文件队列
     */
    private ArrayBlockingQueue<LogInfo> mLogInfos = new ArrayBlockingQueue<>(2000);


    public HLogUtil(int stackTracePosition) {
        this.stackTracePosition = stackTracePosition;
    }

    public HLogUtil() {
    }


    /**
     * 开启写入文件线程
     */
    private synchronized void startLoop() {
        if (loopOpened) {
            return;
        }
        loopOpened = true;
        AppExecutors.getInstance().getDiskIo().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        //获取队首元素，如果没有则阻塞
                        LogInfo logInfo = mLogInfos.take();
                        //如果写入到文件
                        if (mWriteToFile) {
                            if (mFileWrite != null) {
                                mFileWrite.writeLogToFile(logInfo);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 设置文件编辑器
     *
     * @param iFileWrite
     */
    public void setFileWrite(IFileWrite iFileWrite) {
        mFileWrite = iFileWrite;
        if (mLogFilePath != null) {
            mFileWrite.setLogPath(mLogFilePath);
        }
        if (mLogFileMaxSize != 0) {
            mFileWrite.setLogStorageSize(mLogFileMaxSize);
        }
    }


    public void setWriteToFile(boolean writeToFile) {
        mWriteToFile = writeToFile;
        //开启写入文件线程
        if (mWriteToFile) {
            startLoop();
        }
    }

    /**
     * 初始化
     *
     * @param application
     */
    public void init(Application application) {
        if (application != null) {
            sGlobalTag = getAppName(application);
        }
    }

    /**
     * 设置打印文件保存的位置
     *
     * @param path
     */
    public void setLogFilePath(String path) {
        mLogFilePath = path;
        if (mFileWrite != null) {
            mFileWrite.setLogPath(path);
        }
    }

    public void setLogStorageSize(long fileMaxSize) {
        mLogFileMaxSize = fileMaxSize;
        if (mFileWrite != null) {
            mFileWrite.setLogStorageSize(fileMaxSize);
        }
    }

    /**
     * 设置打印日志的等级
     *
     * @param level
     */
    public void setPrintLevel(int level) {
        mPrintLevel = level;
    }


    /**
     * 设置打印方法在堆栈信息中的位置
     *
     * @param position
     */
    public void setStackTracePosition(int position) {
        if (position > 0) {
            stackTracePosition = position;
        }
    }

    /**
     * 打印日志
     *
     * @param content
     */
    public void e(String tag, String content) {
        log(tag, content, Log.ERROR);
    }

    public void d(String tag, String content) {
        log(tag, content, Log.DEBUG);
    }

    public void i(String tag, String content) {
        log(tag, content, Log.INFO);
    }

    public void v(String tag, String content) {
        log(tag, content, Log.VERBOSE);
    }

    public void w(String tag, String content) {
        log(tag, content, Log.WARN);
    }

    public void a(String tag, String content) {
        log(tag, content, Log.ASSERT);
    }


    /**
     * 打印日志
     *
     * @param content
     */
    public void e(String content) {
        log(sGlobalTag, content, Log.ERROR);
    }

    public void d(String content) {
        log(sGlobalTag, content, Log.DEBUG);
    }

    public void i(String content) {
        log(sGlobalTag, content, Log.INFO);
    }

    public void v(String content) {
        log(sGlobalTag, content, Log.VERBOSE);
    }

    public void w(String content) {
        log(sGlobalTag, content, Log.WARN);
    }

    public void a(String content) {
        log(sGlobalTag, content, Log.ASSERT);
    }


    /**
     * 日志打印
     *
     * @param content
     * @param tag
     * @param type
     */
    private synchronized void  log(String tag, String content, int type) {
        //日志等级大于设定，开始打印
        if (TextUtils.isEmpty(tag)) {
            tag = sGlobalTag;
        }
        String logFrom = getTargetStackTraceElement();
        //输出日志到控制台
        if (type >= mPrintLevel) {
            Log.println(type, tag, TOP_BORDER);
            Log.println(type, tag, LEFT_BORDER + logFrom);
            Log.println(type, tag, SPLIT_BORDER);
            Log.println(type, tag, LEFT_BORDER + content);
            Log.println(type, tag, BOTTOM_BORDER);
        }
        //输出日志到文件夹
        if (mWriteToFile && type >= mPrintFileLevel) {
            writeLogToFile(type, tag, logFrom, content);
        }

    }


    /**
     * 写日志到文件里面
     *
     * @param msg
     */
    private void writeLogToFile(int level, String tag, String from, String msg) {
        //存储路径为空，返回失败
        if (mLogFilePath == null) {
            return;
        }
        if (mFileWrite != null) {
            //日志信息
            LogInfo logInfo = new LogInfo(msg, tag, level, from);
            try {
                mLogInfos.put(logInfo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取日志出处信息
     *
     * @return
     */
    private String getTargetStackTraceElement() {
        StackTraceElement targetStackTrace = null;
        //获取线程堆栈转储的堆栈跟踪元素数组
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        //这里根据代码的实现，直接取第五个，比较快速
        if (stackTrace.length > stackTracePosition) {
            targetStackTrace = stackTrace[stackTracePosition];
        } else {
            //遍历元素数组
            boolean shouldTrace = false;
            for (StackTraceElement stackTraceElement : stackTrace) {
                //该对象是否是打印日志自身
                boolean isLogMethod = stackTraceElement.getClassName().equals(HLogUtil.class.getName());
                //如果上一个对象是日志工具本身并且该对象不是则证明该对象就是使用日志工具的类
                if (shouldTrace && !isLogMethod) {
                    //保存调用日志工具的对象
                    targetStackTrace = stackTraceElement;
                    break;
                }
                //保存上一个对象是不是打印工具本身
                shouldTrace = isLogMethod;
            }
        }
        //获取线程名
        String tName = Thread.currentThread().getName();
        //获取调用日志工具执行的方法
        String methodName = targetStackTrace.getMethodName();
        //进行拼接
        return tName + " -> " + methodName + "(" + targetStackTrace.getFileName() + ":"
                + targetStackTrace.getLineNumber() + ")";
    }


    /**
     * 获取应用程序名称
     *
     * @param context
     * @return
     */
    private static synchronized String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public int getPrintFileLevel() {
        return mPrintFileLevel;
    }

    public void setPrintFileLevel(int printFileLevel) {
        mPrintFileLevel = printFileLevel;
    }
}
