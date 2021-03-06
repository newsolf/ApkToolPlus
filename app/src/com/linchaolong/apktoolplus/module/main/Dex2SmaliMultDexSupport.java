package com.linchaolong.apktoolplus.module.main;

import com.linchaolong.apktoolplus.core.ApkToolPlus;
import com.linchaolong.apktoolplus.core.AppManager;
import com.linchaolong.apktoolplus.utils.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * dex2jar multi-dex support
 *
 * Created by linch on 2016/3/28.
 */
public abstract class Dex2SmaliMultDexSupport extends MultDexSupport{

    public Dex2SmaliMultDexSupport(File apk) {

        // 如果不是apk
        if(!FileHelper.isSuffix(apk,"apk")){
            onStart();
            File dexFile = apk;
            File smaliDir = new File(dexFile.getParentFile(), dexFile.getName() + "_smali");
            boolean isSuccess;
            if(!smaliDir.exists()){
                // 如果文件已经存在，跳过转换环节
                isSuccess = ApkToolPlus.dex2smali(dexFile, smaliDir);
            }else{
                isSuccess = true;
            }
            if (isSuccess) {
                ArrayList<File> smaliDirList = new ArrayList<>(1);
                smaliDirList.add(smaliDir);
                onEnd(smaliDirList);
            } else {
                onFailure(dexFile);
            }
            return;
        }

        // start
        onStart();

        // 解压dex文件
        File tempDir = new File(AppManager.getTempDir(),"dex2smali"); // 缓存目录
        List<File> dexFileList = unzipDexList(apk, tempDir);
        if(dexFileList.isEmpty()){
            onEnd(null);
            return;
        }

        // dex2smali
        List<File> smaliDirList = new ArrayList<>(dexFileList.size());
        boolean isSuccess;
        String apkName = FileHelper.getNoSuffixName(apk);
        File outDir = new File(apk.getParentFile(), apkName+"_smali");
        outDir.mkdirs();
        for(File dexFile : dexFileList){
            File smaliDir = new File(outDir, FileHelper.getNoSuffixName(dexFile) + "_smali");
            if(!smaliDir.exists()){
                isSuccess = ApkToolPlus.dex2smali(dexFile, smaliDir);
            }else{
                // 如果文件已经存在，跳过转换环节
                isSuccess = true;
            }
            if(isSuccess){
                smaliDirList.add(smaliDir);
            }else{
                onFailure(dexFile);
            }
        }

        // clear dex file temp
        clearDexTemp(dexFileList);

        // end
        onEnd(smaliDirList);
    }


    public abstract void onStart();
    public abstract void onEnd(List<File> smaliDirList);
    public abstract void onError(Exception e);
    public abstract void onFailure(File dexFile);

}
