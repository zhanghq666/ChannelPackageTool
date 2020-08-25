package com.zhq;

import android.content.res.AXmlResourceParser;
import android.util.TypedValue;
import org.xmlpull.v1.XmlPullParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 解压、压缩这里注意两个地方：
 * 1、路径分隔符不用File.separate而是写死成 /
 * 2、解压时记录哪些文件是未经压缩的，压缩回去时对这些文件同样处理成未压缩的格式STORED
 */
public class Utils {
    /**
     *
     * @param inputDir
     * @param outputPath
     * @param wrapDir 是否多包含一层跟目录
     * @throws Exception
     */
    public static void zip(String inputDir, String outputPath, boolean wrapDir) throws Exception {
        //创建zip输出流
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputPath));
        //创建缓冲输出流
        BufferedOutputStream bos = new BufferedOutputStream(out);
        File input = new File(inputDir);
        if (input.isFile() || wrapDir) {
            compress(out, bos, input, null, null);
        } else {
            for (File file :
                    input.listFiles()) {
                compress(out, bos, file, file.getName(), null);
            }
        }
        bos.close();
        out.close();
    }
    /**
     *
     * @param inputDir
     * @param outputPath
     * @param wrapDir 是否多包含一层跟目录
     * @param storedFileList 采用Stored压缩方法的文件集合
     * @throws Exception
     */
    public static void zip(String inputDir, String outputPath, boolean wrapDir, ArrayList<String> storedFileList) throws Exception {
        //创建zip输出流
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputPath));
        //创建缓冲输出流
        BufferedOutputStream bos = new BufferedOutputStream(out);
        File input = new File(inputDir);
        if (input.isFile() || wrapDir) {
            compress(out, bos, input, null, storedFileList);
        } else {
            for (File file :
                    input.listFiles()) {
                compress(out, bos, file, file.getName(), storedFileList);
            }
        }
        bos.close();
        out.close();
    }

    private static void compress(ZipOutputStream out, BufferedOutputStream bos, File input, String name, ArrayList<String> storedFileList) throws IOException {
        if (name == null) {
            name = input.getName();
        }
        //如果路径为目录（文件夹）
        if (input.isDirectory()) {
            //取出文件夹中的文件（或子文件夹）
            File[] flist = input.listFiles();

            if (flist.length == 0)//如果文件夹为空，则只需在目的地zip文件中写入一个目录进入
            {
                out.putNextEntry(new ZipEntry(name + "/"));
            } else//如果文件夹不为空，则递归调用compress，文件夹中的每一个文件（或文件夹）进行压缩
            {
                for (int i = 0; i < flist.length; i++) {
                    compress(out, bos, flist[i], name + "/" + flist[i].getName(), storedFileList);
                }
            }
        } else//如果不是目录（文件夹），即为文件，则先写入目录进入点，之后将文件写入zip文件中
        {
            ZipEntry entry = new ZipEntry(name);
            if (storedFileList != null && storedFileList.contains(name)) {
//                System.out.println("compress got stored file:" + name);
                entry.setMethod(ZipEntry.STORED);
                entry.setSize(input.length());
                entry.setCrc(checksumInputStream(input.getPath()));
            } else {
                entry.setMethod(ZipEntry.DEFLATED);
            }
            out.putNextEntry(entry);
            FileInputStream fis = new FileInputStream(input);
            BufferedInputStream bis = new BufferedInputStream(fis);
            int len;
            //将源文件写入到zip文件中
            byte[] buf = new byte[1024];
            while ((len = bis.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            bos.flush();
            bis.close();
            fis.close();
        }
    }

    public static void unzip(String inputPath, String outputDir, ArrayList<String> storedFileList) throws Exception {
        File srcFile = new File(inputPath);//获取当前压缩文件
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new Exception(srcFile.getPath() + "所指文件不存在");
        }
        File outFile = new File(outputDir);
        if (outFile.exists()) {
            deleteFile(outFile);
        }

        ZipFile zipFile = new ZipFile(srcFile);//创建压缩文件对象
        //开始解压
        Enumeration<?> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.getMethod() == ZipEntry.STORED) {
//                System.out.println("unzip got stored file:" + entry.getName());
                storedFileList.add(entry.getName());
            }
            // 如果是文件夹，就创建个文件夹
            if (entry.isDirectory()) {
                String dirPath = outputDir + File.separator + entry.getName();
                new File(dirPath).mkdirs();
            } else {
                // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                File targetFile = new File(outputDir + File.separator + entry.getName());
                // 保证这个文件的父文件夹必须要存在
                if (!targetFile.getParentFile().exists()) {
                    targetFile.getParentFile().mkdirs();
                }
                targetFile.createNewFile();
                // 将压缩文件内容写入到这个文件中
                InputStream is = zipFile.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(targetFile);
                int len;
                byte[] buf = new byte[1024];
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                // 关流顺序，先打开的后关闭
                fos.close();
                is.close();
            }
        }
    }

    public static long checksumInputStream(String filepath) {
        CRC32 crc = new CRC32();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filepath);

            int cnt;
            while ((cnt = inputStream.read()) != -1) {
                crc.update(cnt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeStreamSilence(inputStream);
        }
        return crc.getValue();
    }

    public static String doShield(String apkPath, ToolConfig toolConfig) {
        if (!new File(apkPath).exists()) {
            System.out.println("待加固文件不存在");
            return null;
        }

        Runtime runtime = Runtime.getRuntime();

        String shieldedApkPath = getFileRawNameWithPath(apkPath) + "_legu" + getFileExtName(apkPath);
        BufferedInputStream bis = null;
        try {
            String path = Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            String jarPath = new File(path).getParent() + File.separator + "libs" + File.separator + "ms-shield.jar";
            String cmd = String.format("java -Dfile.encoding=utf-8 -jar %s -sid %s -skey %s -uploadPath %s -downloadPath %s",
                    jarPath, toolConfig.getTencentSid(), toolConfig.getTencentSkey(), apkPath, new File(apkPath).getParent());
            if (toolConfig.isShowDetailLog()) {
                System.out.println("ms-shield command:" + cmd);
            }
            Process process = runtime.exec(cmd);

            bis = new BufferedInputStream(process.getInputStream());
            byte[] bytes = new byte[1024];
            int length = -1;
            while ((length = bis.read(bytes)) != -1) {
                System.out.print(new String(bytes, 0, length, "utf-8"));
            }
            process.waitFor();
            if (process.exitValue() == 0) {
                System.out.println("ms-shield执行成功");

                return shieldedApkPath;
            } else {
                System.out.println("ms-shield执行失败");

                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            closeStreamSilence(bis);
        }
    }

    public static boolean modifyMetaData(String backupFilePath, String outputFilePath, int maskOffset, String valueReal) {

        System.out.println("开始修改xml");

        System.out.println("for " + valueReal);
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(backupFilePath);
            fos = new FileOutputStream(outputFilePath);

            byte[] bytes = new byte[1];
            int offset = 0;
            while (fis.read(bytes) != -1) {
                if (maskOffset == offset) {
                    int valueRealInt = Integer.parseInt(valueReal);
                    byte[] valueB = intToByteArray(valueRealInt);
                    fos.write(valueB);
                    fis.skip(valueB.length - 1);
                } else {
                    fos.write(bytes);
                }
                offset++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("修改完毕");
        return true;
    }

    public static boolean backupManifest(String xmlPath, File originXml, String originBackupPath) {
        boolean success = false;
        if (new File(originBackupPath).exists()) {
            new File(originBackupPath).delete();
        }
        if (moveFile(xmlPath, originBackupPath)) {
            success = originXml.delete();
        }

        if (!success) {
            System.out.println("xml备份出错, 终止执行！");
        } else {
            System.out.println("xml备份完毕");
        }
        return success;
    }

    private static boolean moveFile(String source, String dest) {
        boolean success = false;
        if (!new File(source).exists()) {
            return false;
        }
        File destFile = new File(dest);
        if (destFile.exists()) {
            destFile.delete();
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(source);
            fos = new FileOutputStream(dest);

            byte[] bytes = new byte[1024];
            int length = 0;
            while ((length = fis.read(bytes)) != -1) {
                fos.write(bytes, 0, length);
            }
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        } finally {
            closeStreamSilence(fis);
            closeStreamSilence(fos);
        }
        return success;
    }

    /**
     * 找到目的属性值得偏移字节量
     *
     * @param manifestPath
     * @param valueMask
     * @return
     */
    public static int findOffset(String manifestPath, String valueMask) {
        int offsetTotal = -1;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(manifestPath);
//            System.out.println("file total length:" + fis.available());
        } catch (Exception e) {
            e.printStackTrace();
            return offsetTotal;
        }


        boolean foundTarget = false;
        int eventType = -1;
        AXmlResourceParser parser = new AXmlResourceParser();
        try {
            parser.open(fis);
            while (true) {
                eventType = parser.next();
                if (eventType == XmlPullParser.END_DOCUMENT) {
                    break;
                }
                if (eventType == XmlPullParser.START_TAG && "meta-data".equals(parser.getName()) && parser.getAttributeCount() > 0) {
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        if (valueMask.equals(parser.getAttributeValue(i))) {
                            foundTarget = true;
                            System.out.println(parser.getAttributeValue(i) + ":");

                            int valueIndex = i + 1;
                            int valueType = parser.getAttributeValueType(valueIndex);
                            if (valueType >= TypedValue.TYPE_FIRST_INT &&
                                    valueType <= TypedValue.TYPE_LAST_INT) {
                                // ATTRIBUTE_LENGTH为5
                                // 计算目标AttributeValue在整个文档中的偏移量
                                // 偏移量 = 当前已读取的字节数 - ((属性倒序位置 * 每个属性所占Int数) - 属性值所在位置) * Int类型字节数
                                offsetTotal = parser.getCurrentOffset() - ((parser.getAttributeCount() - valueIndex) * 5 - 4) * 4;

                                System.out.println(parser.getAttributeIntValue(valueIndex, -1));
                            } else {
                                System.out.println("不支持的值类型！！！");
                            }
                            break;
                        }
                    }

                    if (foundTarget) {
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (foundTarget || eventType == XmlPullParser.END_DOCUMENT) {
                parser.close();
            }
        }

        return offsetTotal;
    }

    /**
     * @param i
     * @return
     */
    public static byte[] intToByteArray(int i) {
        return intToByteArray(i, false);
    }

    public static byte[] intToByteArray(int i, boolean isBigEndian) {
        byte[] result = new byte[4];
        if (isBigEndian) {
            result[0] = (byte) ((i >> 24) & 0xFF);
            result[1] = (byte) ((i >> 16) & 0xFF);
            result[2] = (byte) ((i >> 8) & 0xFF);
            result[3] = (byte) (i & 0xFF);
        } else {
            result[0] = (byte) (i & 0xFF);
            result[1] = (byte) ((i >> 8) & 0xFF);
            result[2] = (byte) ((i >> 16) & 0xFF);
            result[3] = (byte) ((i >> 24) & 0xFF);
        }
        return result;

    }

    public static boolean deleteFile(File fileRoot) {
        boolean success = false;
        if (fileRoot.isDirectory()) {
            if (fileRoot.listFiles() != null) {
                for (File file : fileRoot.listFiles()) {
                    deleteFile(file);
                }
            }
            success = fileRoot.delete();
        } else {
            success = fileRoot.delete();
        }
        return success;
    }

    public static ToolConfig parseToolConfig(String configPath) {
        ToolConfig config = new ToolConfig();
        FileReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new FileReader(configPath);
            bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] cline = line.split("=");
                if (cline.length == 2) {
                    if (cline[0] != null) {
                        cline[0] = cline[0].trim();
                    }
                    if (cline[1] != null) {
                        cline[1] = cline[1].trim();
                    }
                    String key = cline[0];
                    switch (key) {
                        case "TencentSid":
                            config.setTencentSid(cline[1]);
                            break;
                        case "TencentSkey":
                            config.setTencentSkey(cline[1]);
                            break;
                        case "KeystoreFilePath":
                            config.setKeystoreFilePath(cline[1]);
                            break;
                        case "KeyPassword":
                            config.setKeyPassword(cline[1]);
                            break;
                        case "StorePassword":
                            config.setStorePassword(cline[1]);
                            break;
                        case "KeyAlias":
                            config.setKeyAlias(cline[1]);
                            break;
                        case "ChannelConfigPath":
                            config.setChannelConfigPath(cline[1]);
                            break;
                        case "ChannelMask":
                            config.setChannelMask(cline[1]);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeReaderSilence(reader);
            Utils.closeReaderSilence(bufferedReader);
        }

        return config;
    }

    public static List<ChannelConfig> parseChannelConfig(String configPath) {
        ArrayList<ChannelConfig> channelConfigs = new ArrayList<>();
        FileReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new FileReader(configPath);
            bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] cline = line.split(",");
                if (cline.length >= 2) {
                    if (cline[0] != null) {
                        cline[0] = cline[0].trim();
                    }
                    if (cline[1] != null) {
                        cline[1] = cline[1].trim();
                    }

                    ChannelConfig config = new ChannelConfig();
                    config.setNameIdentify(cline[0]);
                    config.setValue(cline[1]);
                    channelConfigs.add(config);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeReaderSilence(reader);
            Utils.closeReaderSilence(bufferedReader);
        }

        return channelConfigs;
    }

    public static String signApk(String apkPath, ToolConfig config) {
        if (!new File(apkPath).exists()) {
            System.out.println("待签名文件不存在");
            return null;
        }

        Runtime runtime = Runtime.getRuntime();
        String signedApkPath = getFileRawNameWithPath(apkPath) + "_signed" + getFileExtName(apkPath);
        BufferedInputStream bis = null;
        try {
            String keyStorePath = config.getKeystoreFilePath();
            String cmd = String.format("jarsigner -verbose -signedjar %s " +
                            " %s -keystore %s -keypass %s -storepass %s %s",
                    signedApkPath, apkPath, keyStorePath, config.getKeyPassword(), config.getStorePassword(), config.getKeyAlias());
            if (config.isShowDetailLog()) {
                System.out.println("jarsigner command:" + cmd);
            }
            Process process = runtime.exec(cmd);

            bis = new BufferedInputStream(process.getInputStream());
            byte[] bytes = new byte[1024];
            int length = -1;
            while ((length = bis.read(bytes)) != -1) {
                if (config.isShowDetailLog()) {
                    System.out.print(new String(bytes, 0, length, "GBK"));
                }
            }
            process.waitFor();
            if (process.exitValue() == 0) {
                System.out.println("jarsigner执行成功");

                return signedApkPath;
            } else {
                System.out.println("jarsigner执行失败");

                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStreamSilence(bis);
        }
        return null;
    }

    public static boolean alignApk(String apkPath, boolean showDetail) {
        boolean success = false;
        String alignToolPath = findZipAlignTool();
        if (alignToolPath != null && alignToolPath.length() > 0) {
            if (!new File(apkPath).exists()) {
                System.out.println("待对齐文件不存在");
                return false;
            }

            Runtime runtime = Runtime.getRuntime();
            String alignApkPath = getFileRawNameWithPath(apkPath) + "_align" + getFileExtName(apkPath);
            File alignFile = new File(alignApkPath);
            if (alignFile.exists()) {
                alignFile.delete();
            }
            BufferedInputStream bis = null;
            try {
                String cmd = String.format("%s 4 %s %s", alignToolPath, apkPath, alignApkPath);
                if (showDetail) {
                    System.out.println("zipalign command:" + cmd);
                }
                Process process = runtime.exec(cmd);

                bis = new BufferedInputStream(process.getInputStream());
                byte[] bytes = new byte[1024];
                int length = -1;
                while ((length = bis.read(bytes)) != -1) {
                    if (showDetail) {
                        System.out.print(new String(bytes, 0, length, "GBK"));
                    }
                }
                process.waitFor();

                success = process.exitValue() == 0;
                if (alignFile.exists()) {
                    System.out.println("zipalign执行成功");
                } else {
                    System.out.println("zipalign执行失败");
                }

            } catch (Exception e) {
                e.printStackTrace();
                success = false;

            } finally {
                closeStreamSilence(bis);
            }
        } else {
            System.out.println("未发现zipalign.exe");
            success = false;
        }

        return success;
    }

    private static String findZipAlignTool() {
        String path = null;
        String sdkPath = System.getenv("ANDROID_HOME");
        File platformDir = new File(sdkPath + File.separator + "build-tools");
        if (platformDir.exists() && platformDir.isDirectory()) {
            File[] files = platformDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        File tool = new File(file.getPath() + File.separator + "zipalign.exe");
                        if (tool.isFile() && tool.exists()) {
                            path = tool.getPath();
                            break;
                        }
                    }
                }
            }

        }

        return path;
    }

    public static String getFileRawNameWithPath(String path) {
        String rawName = path;
        if (path != null) {
            int index = path.lastIndexOf(".");
            if (index > 0) {
                rawName = path.substring(0, index);
            }
        }

        return rawName;
    }

    public static String getFileRawName(String path) {
        String rawName = null;

        File file = new File(path);
        if (file.exists()) {
            int index = file.getName().lastIndexOf(".");
            if (index > 0) {
                rawName = file.getName().substring(0, index);
            }
        }
        return rawName;
    }

    public static String getFileExtName(String path) {
        String extName = null;
        if (path != null) {
            int index = path.lastIndexOf(".");
            if (index > 0) {
                extName = path.substring(index);
            }
        }

        return extName;
    }

    public static void closeStreamSilence(InputStream s) {
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeStreamSilence(OutputStream s) {
        if (s != null) {
            try {
                s.flush();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeReaderSilence(Reader r) {
        if (r != null) {
            try {
                r.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeWriterSilence(Writer w) {
        if (w != null) {
            try {
                w.flush();
                w.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}