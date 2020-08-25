package com.zhq;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // 是否全要
        boolean doAll = false;
        // 是否做加固
        boolean doShield = false;
        // 是否生成渠道包
        boolean doChannel = false;
        // 是否打印详细信息
        boolean showDetailLog = false;
        String configFilePath = null;
        String targetApkPath = null;
        for (int i = 0; i < args.length; i++) {
            String param = args[i];
            if ("-a".equals(param)) {
                doAll = true;
            } else if ("-c".equals(param)) {
                doChannel = true;
            } else if ("-s".equals(param)) {
                doShield = true;
            } else if ("-v".equals(param)) {
                showDetailLog = true;
            } else if ("-i".equals(param)) {
                if (i + 1 < args.length) {
                    String paramValue = args[i + 1];
                    if (!paramValue.startsWith("-")) {
                        configFilePath = paramValue;
                    }
                }
            } else if ("-h".equals(param)) {
                showHelp();
                return;
            } else {
                if (i == args.length - 1) {
                    targetApkPath = param;
                }
            }
        }

        ToolConfig config = null;
        if (configFilePath == null || configFilePath.length() <= 0 || !new File(configFilePath).exists()) {
            System.out.println("未发现配置文件");
            return;
        } else {
            config = Utils.parseToolConfig(configFilePath);
        }
        config.setShowDetailLog(showDetailLog);


        String zipPath = targetApkPath;
        if (zipPath == null || !new File(zipPath).exists()) {
            System.out.println("目标apk文件不存在,退出执行");
            return;
        }

        String shieldApkPath = null;
        if (doShield || doAll) {
            shieldApkPath = Utils.doShield(zipPath, config);
        }

        if (shieldApkPath != null && new File(shieldApkPath).exists()) {
            System.out.println("加固成功");
            zipPath = shieldApkPath;
        }

        if (!doChannel && !doAll) {
            System.out.println("执行完毕");
            return;
        }

        System.out.println("开始生成渠道包...");

        List<ChannelConfig> channelConfigList = Utils.parseChannelConfig(config.getChannelConfigPath());
        if (channelConfigList == null || channelConfigList.isEmpty()) {
            System.out.println("渠道配置解析失败，退出执行");
            return;
        }

        String unzipDir = Utils.getFileRawNameWithPath(zipPath);
        String name = Utils.getFileRawName(zipPath);
        String ext = Utils.getFileExtName(zipPath);
        try {
            ArrayList<String> storedFileList = new ArrayList<>();

            Utils.unzip(zipPath, unzipDir, storedFileList);
            System.out.println("解压成功");

            // 删除META-INF中的签名信息，AS默认打出的包是V2签名，而后面重签名的时候将采用V1签名
            String METAPath = unzipDir + File.separator + "META-INF" + File.separator;
            for (File f :
                    new File(METAPath).listFiles()) {
                if (f.getName().endsWith(".MF") || f.getName().endsWith("RSA")) {
                    f.delete();
                }
            }
//            Utils.deleteFile(new File(METAPath + "MANIFEST.MF"));
//            Utils.deleteFile(new File(METAPath + "CERT.MF"));
//            Utils.deleteFile(new File(METAPath + "CERT.RSA"));

            String manifestPath = unzipDir + File.separator + "AndroidManifest.xml";
            File originXml = new File(manifestPath);
            String originBackupPath = originXml.getParentFile().getParent() + File.separator + originXml.getName() + ".origin";
            if (!Utils.backupManifest(manifestPath, originXml, originBackupPath)) {
                System.out.println("AndroidManifest备份失败，退出执行");
                return;
            }

            int maskOffset = Utils.findOffset(originBackupPath, config.getChannelMask());
            System.out.println("find mask offset " + maskOffset);
            if (maskOffset <= 0) {
                System.out.println("查找关键字失败，退出执行");
                return;
            }

            for (ChannelConfig channelConfig : channelConfigList) {

                Utils.modifyMetaData(originBackupPath, manifestPath, maskOffset, channelConfig.getValue());

                String channelRawFilePath = new File(zipPath).getParent() + File.separator + "ChannelRaw" + File.separator;
                File channelRawDir = new File(channelRawFilePath);
                if (!channelRawDir.exists()) {
                    channelRawDir.mkdirs();
                }

                String newZipPath = channelRawFilePath + name + "_" + channelConfig.getValue()+ "_" + channelConfig.getNameIdentify() + ext;
                Utils.zip(unzipDir, newZipPath, false, storedFileList);
                System.out.println("压缩成功");

                String signedPath = Utils.signApk(newZipPath, config);
                if (signedPath != null && signedPath.length() > 0) {
                    System.out.println("签名成功");
                    new File(newZipPath).delete();

                    if (Utils.alignApk(signedPath, showDetailLog)) {
                        System.out.println("对齐成功");
                        new File(signedPath).delete();
                    } else {
                        System.out.println("对齐失败，退出本轮执行");
                    }
                } else {
                    System.out.println("签名失败，退出本轮执行");
                }

                System.out.println("----------------------");
                System.out.println("----------------------");
            }

            Utils.deleteFile(new File(unzipDir));
            Utils.deleteFile(new File(originBackupPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showHelp() {
        System.out.println("用法：java -jar channel_tool.jar [选项] 目标apk路径\n");
        System.out.println("[-s]                启用乐固加固\n");
        System.out.println("[-c]                启用渠道打包\n");
        System.out.println("[-a]                同时启用乐固加固和渠道打包\n");
        System.out.println("[-i <配置文件路径>]   配置文件路径\n");
    }
}
