package com.example.mongodbdemo.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/18 16:18
 **/
@Slf4j
public class URLUtils {


    public static void download2Html(String urlPath, String savePath) {
        URL url = null;

        try {
            url = new URL(urlPath);   //想要读取的url地址
            File fp = new File(savePath);
            OutputStream os = new FileOutputStream(fp);          //建立文件输出流


            URLConnection conn = url.openConnection();          //打开url连接
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String urlString = "";
            String current;
            while ((current = in.readLine()) != null) {
                urlString += current;
            }
            os.write(urlString.getBytes());
            os.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String download2Str(String urlPath) {
        URL url = null;
        String urlString = "";
        try {
            url = new URL(urlPath);   //想要读取的url地址
            URLConnection conn = url.openConnection();          //打开url连接
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String current;
            while ((current = in.readLine()) != null) {
                urlString += current;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return urlString;
    }

    public static InputStream url2InputStream(String url) {
        try {
            URL realUrl = new URL(url);
            URLConnection urlConnection = realUrl.openConnection();
            return urlConnection.getInputStream();
        } catch (Exception e) {
            log.error("url转inputStream时异常！！！");
        }

        return null;
    }

    /**
     * 将字符串文本保存到本地
     *
     * @param inputStream
     * @param savePath
     * @param fileName
     */
    public static String str2Local(InputStream inputStream, String savePath, String fileName) {
        //一次最多读取1k
        byte[] buffer = new byte[1024];
        //实际读取的长度
        int readLenghth;
        //根据文件保存地址，创建文件输出流
        File filePath = new File(savePath);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        File realFile = null;
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            fileName = RegExUtils.matchSpecialChar(fileName);
            realFile = new File(filePath.getAbsolutePath() + "/" + fileName);
            fileOutputStream = new FileOutputStream(realFile);
            //创建的一个写出的缓冲流
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            //文件逐步写入本地
            //先读出来，保存在buffer数组中

            while ((readLenghth = inputStream.read(buffer, 0, 1024)) != -1) {
                //再从buffer中取出来保存到本地
                bufferedOutputStream.write(buffer, 0, readLenghth);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭缓冲流
                bufferedOutputStream.close();
                fileOutputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return savePath + "/" + fileName;
    }
}
