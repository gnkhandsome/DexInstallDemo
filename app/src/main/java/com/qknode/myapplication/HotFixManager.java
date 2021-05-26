package com.qknode.myapplication;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 负责运行时注入补丁包
 */
public class HotFixManager {

    public static final String FIXED_DEX_SDCARD_PATH = Environment.getExternalStorageDirectory().getPath() + "/fixed.dex";

    /**
     * 注入补丁包
     */
    public static void installFixedDex(Context context) {

        try {
            //获取手机根目录的补丁包
            File fixedDexFile = new File(FIXED_DEX_SDCARD_PATH);
            // 补丁包不存在
            Log.i("fixedDexFile", String.valueOf(fixedDexFile.exists()));
            if (!fixedDexFile.exists()) {
                return;
            }
            // 获取PathClassLoader的pathList属性
            Field pathListFiled = ReflectUtil.findField(context.getClassLoader(), "pathList");
            Log.i("pathListFiled", String.valueOf(null == pathListFiled));
            Object dexPathList = pathListFiled.get(context.getClassLoader());
            Log.i("dexPathList", String.valueOf(null == dexPathList));
            // 获取 DexPathList 中的makeDexElements方法
            // 注意：每个系统中该方法的参数列表可能不一样，需要查看DexPathList这个类的源码才可以确定。
            Method makeDexElements = ReflectUtil.findMethod(dexPathList, "makeDexElements",
                    List.class,
                    File.class,
                    List.class,
                    ClassLoader.class);
            Log.i("makeDexElements", String.valueOf(null == makeDexElements));
            // 把待加载的补丁文件添加到一个列表中
            ArrayList<File> filesToBeInstalled = new ArrayList<>();
            filesToBeInstalled.add(fixedDexFile);

            //准备其他参数
            File optimizedDirectory = new File(context.getFilesDir(), "fixed_dex");
            ArrayList<IOException> suppressedException = new ArrayList<>();

            //调用makeDexElements，得到 待修复Dex文件对应的Element 数组（新）
            Object[] extraElements = (Object[]) makeDexElements.invoke(
                    dexPathList,
                    filesToBeInstalled,
                    optimizedDirectory,
                    suppressedException,
                    context.getClassLoader()
            );
            Log.i("extraElements", String.valueOf(extraElements.length));
            Field dexFiledElements = ReflectUtil.findField(dexPathList, "dexElements");
            Log.i("dexFiledElements", String.valueOf(null == dexFiledElements));
            Object[] originElements = (Object[]) dexFiledElements.get(dexPathList);
            Log.i("originElements", String.valueOf(originElements.length));
            //创建一个新的Elements数组
            Object[] combineElements = (Object[]) Array.newInstance(originElements.getClass().getComponentType(), originElements.length + extraElements.length);
            Log.i("combineElements", String.valueOf(combineElements.length));
            // 在新的数组中先放入补丁包的extraElements，再放combineElements
            System.arraycopy(extraElements, 0, combineElements, 0, extraElements.length);
            System.arraycopy(originElements, 0, combineElements, extraElements.length, originElements.length);
            //将新的combineElements,重新赋值给dexPathList;
            dexFiledElements.set(dexPathList, combineElements);
        } catch (Exception e) {
            Log.i("RuntimeException", e.getMessage());
            throw new RuntimeException("hahahh");
        }
    }
}
