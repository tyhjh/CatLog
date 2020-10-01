package com.display.loglibrary;

import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件处理工具类
 *
 * @author hanpei
 * @version 1.0, 2020/4/20
 * @since 产品模块版本
 */
public class FileUtil {

    /**
     * 压缩日志文件
     *
     * @param file
     * @param zipFile
     * @return
     */
    public static boolean compressFile(File file, File zipFile) {
        //文件不存在
        if (!file.exists()) {
            Log.e("compressFile", "file is not exist");
            return false;
        }
        ZipOutputStream zos = null;
        BufferedInputStream bis = null;
        try {
            FileOutputStream fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);
            bis = new BufferedInputStream(new FileInputStream(file));
            ZipEntry entry = new ZipEntry("" + file.getName());
            zos.putNextEntry(entry);
            int count;
            byte[] buf = new byte[1024];
            while ((count = bis.read(buf)) != -1) {
                zos.write(buf, 0, count);
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            //在finally里面关流
            try {
                if (bis != null) {
                    bis.close();
                }
                if (bis != null) {
                    zos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 对文件按修改时间从远到近进行排序
     *
     * @return
     */
    public static List<File> getFileSort(List<File> list) {
        if (list != null && list.size() > 0) {
            Collections.sort(list, new Comparator<File>() {
                @Override
                public int compare(File file, File newFile) {
                    if (newFile.lastModified() < file.lastModified()) {
                        return 1;
                    } else if (file.lastModified() == newFile.lastModified()) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
            });

        }
        return list;
    }

}
