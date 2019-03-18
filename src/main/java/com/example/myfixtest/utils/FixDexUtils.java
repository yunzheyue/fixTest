package com.example.myfixtest.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class FixDexUtils {
    public static final String DEX_DIR = "odex";

    private static HashSet<File> loadedDex = new HashSet<File>();

    static {
        loadedDex.clear();
    }


    public static void copyFileToPackageAndInstall(Context context) {

        //目录：/data/data/packageName/odex
        File fileDir = context.getDir(DEX_DIR, Context.MODE_PRIVATE);
        //往该目录下面放置我们修复好的dex文件。
        String name = "classes2.dex";
        String filePath = fileDir.getAbsolutePath() + File.separator + name;
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
            Toast.makeText(context, "有重复文件删除", Toast.LENGTH_SHORT).show();
        }
        //搬家：把下载好的在SD卡里面的修复了的classes2.dex搬到应用目录filePath
        InputStream is = null;
        FileOutputStream os = null;
        try {

            Log.e("TAG", "查找文件的路径===" + Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + name);
            is = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + name);
            os = new FileOutputStream(filePath);

            if (is == null) {
                Toast.makeText(context, "文件没找到", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(context, "找到文件了", Toast.LENGTH_SHORT).show();
            }
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }

            File f = new File(filePath);
            if (f.exists()) {
                Toast.makeText(context, "dex 重写成功", Toast.LENGTH_SHORT).show();
            }
            //热修复
            loadFixedDex(context);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void loadFixedDex(Context context) {
        if (context == null) {
            return;
        }
        //遍历所有的修复的dex
        File fileDir = context.getDir(DEX_DIR, Context.MODE_PRIVATE);

        File[] listFiles = fileDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().startsWith("classes") && file.getName().endsWith(".dex")) {
                loadedDex.add(file);//存入集合
            }
        }
        //dex合并之前的dex
        doDexInject(context, fileDir);
    }

    private static void doDexInject(final Context appContext, File filesDir) {
        String optimizeDir = filesDir.getAbsolutePath() + File.separator + "opt_dex";
        File fopt = new File(optimizeDir);
        if (!fopt.exists()) {
            fopt.mkdirs();
        }
        //1.加载应用程序的dex
        try {
            PathClassLoader pathClassLoader = (PathClassLoader) appContext.getClassLoader();

            for (File dex : loadedDex) {
                //2.加载指定的修复的dex文件。

                //这是进行优化
                DexClassLoader dexClassLoader = new DexClassLoader(
                        dex.getAbsolutePath(),//String dexPath, apk的路径
                        fopt.getAbsolutePath(),//String optimizedDirectory,  优化后的dex的地址
                        null,//String libraryPath,
                        pathClassLoader//ClassLoader parent
                );
                //3.合并
                Object dexObj = getPathList(dexClassLoader);
                Object pathObj = getPathList(pathClassLoader);
                //获取到dexElements的对象
                Object mDexElementsList = getDexElements(dexObj);
                Object pathDexElementsList = getDexElements(pathObj);
                //将dexElements合并完成
                Object dexElements = combineArray(mDexElementsList, pathDexElementsList);
                //重写给PathList里面的Element[] dexElements;赋值
                Object pathList = getPathList(pathClassLoader);
                setField(pathList, pathList.getClass(), "dexElements", dexElements);
                Log.e("TAG", "替换成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object getPathList(Object baseDexClassLoader) throws Exception {
        return getField(baseDexClassLoader, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    private static Object getDexElements(Object obj) throws Exception {
        return getField(obj, obj.getClass(), "dexElements");
    }

    /**
     * 两个数组合并
     */
    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }

    private static void setField(Object obj, Class<?> cl, String field, Object value) throws Exception {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        localField.set(obj, value);
    }

    private static Object getField(Object obj, Class<?> cl, String field)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field localField = cl.getDeclaredField(field);
        localField.setAccessible(true);
        return localField.get(obj);
    }
}
