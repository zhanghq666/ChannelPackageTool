package com.zhq;

import android.content.res.AXmlResourceParser;
import com.sun.deploy.xml.XMLParser;
import org.xmlpull.v1.XmlPullParserException;
import sun.tools.jar.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import org.xmlpull.v1.XmlPullParser;

public class Main {

    public static void main(String[] args) {

        for (String params:
             args) {
            System.out.println(params);

        }

        String zipPath = "D:\\channel-package-center\\_tools\\self-tools\\sample_legu.apk";
        String unzipDir = "D:\\channel-package-center\\_tools\\self-tools\\sample_legu\\";
        String newZipPath = "D:\\channel-package-center\\_tools\\self-tools\\sample_legu_new.apk";
        try {
//            Utils.unzip(zipPath, unzipDir);
//            System.out.println("解压成功");
//            Utils.zip(unzipDir, newZipPath);
//            System.out.println("压缩成功");

            String manifestPath = unzipDir + "AndroidManifest.xml";
//            File file = new File(manifestPath);
            Utils.modifyMetaData(manifestPath, "CHANNEL_CODE", "10022");

//            System.out.println(parser.nextText());
//
//            System.out.println(parser.getNamespace());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
