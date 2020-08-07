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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Utils {
    public static void zip(String inputDir, String outputPath) throws Exception {
        //创建zip输出流
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputPath));
        //创建缓冲输出流
        BufferedOutputStream bos = new BufferedOutputStream(out);
        File input = new File(inputDir);
        compress(out, bos, input, null);
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
            FileInputStream fos = new FileInputStream(input);
            BufferedInputStream bis = new BufferedInputStream(fos);
            int len;
            //将源文件写入到zip文件中
            byte[] buf = new byte[1024];
            while ((len = bis.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }
            bis.close();
            fos.close();
        }
    }

    public static void unzip(String inputPath, String outputDir) throws Exception {
        File srcFile = new File(inputPath);//获取当前压缩文件
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new Exception(srcFile.getPath() + "所指文件不存在");
        }
        ZipFile zipFile = new ZipFile(srcFile);//创建压缩文件对象
        //开始解压
        Enumeration<?> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            // 如果是文件夹，就创建个文件夹
            if (entry.isDirectory()) {
                String dirPath = outputDir + "/" + entry.getName();
                srcFile.mkdirs();
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


    public static void modifyMetaData(String xmlPath, String valueMask, String valueReal) {
        int maskOffset = findOffset(xmlPath, valueMask);
//if (1==1)
//    return;
        String originPath = xmlPath + ".origin";
        File originFile = new File(originPath);
        if (!originFile.exists()) {
            new File(xmlPath).renameTo(originFile);
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(originPath);
            fos = new FileOutputStream(xmlPath);

            byte[] bytes = new byte[1];
            int offset = 0;
            while (fis.read(bytes) != -1) {
                if (maskOffset == offset) {
                    fos.write(valueReal.getBytes());
                } else {
                    fos.write(bytes);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    }

    /**
     * 找到目的属性值得偏移字节量
     * @param manifestPath
     * @param valueMask
     * @return
     */
    private static int findOffset(String manifestPath, String valueMask) {
        int offsetTotal = -1;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(manifestPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return offsetTotal;
        }

        AXmlResourceParser parser = new AXmlResourceParser();
        parser.open(fis);
        boolean foundTarget = false;
        int eventType = -1;
        while (true) {
            try {
                eventType = parser.next();
                if ("meta-data".equals(parser.getName()) && parser.getAttributeCount() > 0) {
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        if (valueMask.equals(parser.getAttributeValue(i))) {
                            // ATTRIBUTE_LENGTH为5
                            offsetTotal = parser.getCurrentOffset() - (parser.getAttributeCount() - i) * 5 + 4;
                            foundTarget = true;
                            System.out.println(parser.getAttributeValue(i) + ":");

                            int valueType = parser.getAttributeValueType(i + 1);
                            if (valueType >= TypedValue.TYPE_FIRST_INT &&
                                    valueType <= TypedValue.TYPE_LAST_INT) {
                                System.out.println(parser.getAttributeIntValue(i + 1, -1));
                            } else {
                                System.out.println("不支持的值类型！！！");
                            }
                            break;
                        }
                    }
                }

                if (parser.getEventType() == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (eventType != XmlPullParser.END_DOCUMENT) {
            parser.close();
        }

        return offsetTotal;
    }
}
