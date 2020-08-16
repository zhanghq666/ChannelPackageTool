package com.zhq;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        for (String params:
             args) {
            System.out.println(params);

        }

        String zipPath = "D:\\channel-package-center\\_tools\\self-tools\\sample.apk";
        String unzipDir = "D:\\channel-package-center\\_tools\\self-tools\\sample\\";
        String newZipPath = "D:\\channel-package-center\\_tools\\self-tools\\sample_new.apk";
        try {
//            Utils.unzip(zipPath, unzipDir);
//            System.out.println("解压成功");
//
//            // 删除META-INF中的签名信息，AS默认打出的包是V2签名，而后面重签名的时候将采用V1签名
//            String METAPath = unzipDir + "META-INF\\";
//            Utils.deleteFile(new File(METAPath + "MANIFEST.MF"));
//            Utils.deleteFile(new File(METAPath + "CERT.MF"));
//            Utils.deleteFile(new File(METAPath + "CERT.RSA"));
//
//            String manifestPath = unzipDir + "AndroidManifest.xml";
//            Utils.modifyMetaData(manifestPath, "CHANNEL_CODE", "10022");
//
//            Utils.zip(unzipDir, newZipPath, false);
//            System.out.println("压缩成功");

            Utils.signApk(newZipPath);
            System.out.println("签名成功");

//            Utils.alignApk(newZipPath);
//            System.out.println("对齐成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
