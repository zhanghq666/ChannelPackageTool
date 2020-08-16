package com.zhq;

import android.content.res.AXmlResourceParser;
import android.util.TypedValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.Enumeration;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Utils {
    public static void zip(String inputDir, String outputPath, boolean wrapDir) throws Exception {
        //创建zip输出流
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputPath));
        //创建缓冲输出流
        BufferedOutputStream bos = new BufferedOutputStream(out);
        File input = new File(inputDir);
        if (input.isFile() || wrapDir) {
            compress(out, bos, input, null);
        } else {
            for (File file :
                    input.listFiles()) {
                compress(out, bos, file, file.getName());
            }
        }
        bos.close();
        out.close();
    }

    /**
     * @param name 压缩文件名，可以写为null保持默认
     */
    //递归压缩
    private static void compress(ZipOutputStream out, BufferedOutputStream bos, File input, String name) throws IOException {
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
                    compress(out, bos, flist[i], name + "/" + flist[i].getName());
                }
            }
        } else//如果不是目录（文件夹），即为文件，则先写入目录进入点，之后将文件写入zip文件中
        {
            out.putNextEntry(new ZipEntry(name));
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

    public static void unzip(String inputPath, String outputDir) throws Exception {
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
            // 如果是文件夹，就创建个文件夹
            if (entry.isDirectory()) {
                String dirPath = outputDir + "/" + entry.getName();
                new File(dirPath).mkdirs();
            } else {
                // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                File targetFile = new File(outputDir + "/" + entry.getName());
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


    public static boolean modifyMetaData(String xmlPath, String valueMask, String valueReal) {
        boolean success = false;

        File originXml = new File(xmlPath);
        String originBackupPath = originXml.getParentFile().getParent() + File.separator + originXml.getName() + ".origin";
        File originFileBackup = new File(originBackupPath);
        if (originFileBackup.exists()) {
            success = originFileBackup.delete();
        } else {
            success = true;
        }
        if (success && moveFile(xmlPath, originBackupPath)) {
            success = originXml.delete();
        }

        if (!success) {
            System.out.println("xml备份出错, 终止执行！");
            return false;
        }
        System.out.println("xml备份完毕");

        int maskOffset = findOffset(originBackupPath, valueMask);
        System.out.println("found mask offset " + maskOffset);
        if (maskOffset <= 0) {
            return false;
        }

        System.out.println("开始修改xml");

        System.out.println("for " + valueReal);
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(originFileBackup);
            fos = new FileOutputStream(xmlPath);

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

        System.out.println("modifyMetaData done!");
        return true;
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
            System.out.println("file total length:" + fis.available());
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

    public static void signApk(String apkPath) {
        if (!new File(apkPath).exists()) {
            System.out.println("待签名文件不存在");
            return;
        }

        Runtime runtime = Runtime.getRuntime();
        String signedName = getFileRawName(apkPath) + "_signed" + getFileExtName(apkPath);
        BufferedInputStream bis = null;
        try {
            String cmd = String.format("jarsigner -verbose -signedjar %s " +
                    " %s -keystore %s -keypass %s -storepass %s %s",
                    signedName, apkPath,"jk.keystore", "jk123456","jk123456", "jkclinic");
            System.out.println(cmd);
            Process process = runtime.exec(cmd);

            bis = new BufferedInputStream(process.getInputStream());
            while (bis.read() != -1) {
                System.out.println(bis.read());
            }
            process.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStreamSilence(bis);
        }
    }

    public static void alignApk(String apkPath) {

    }

    public static String getFileRawName(String path) {
        String rawName = path;
        if (path != null) {
            int index = path.lastIndexOf(".");
            if (index > 0) {
                rawName = path.substring(0, index);
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
}