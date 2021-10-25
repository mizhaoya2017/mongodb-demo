package com.example.mongodbdemo.service.impl;

import com.example.mongodbdemo.content.CommonContent;
import com.example.mongodbdemo.service.HtmlHandlerResolver;
import com.example.mongodbdemo.utils.RegExUtils;
import com.example.mongodbdemo.utils.StUtils;
import com.example.mongodbdemo.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * 解析xml
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/22 13:31
 **/
@Slf4j
@Service
public class HtmlHandlerResolverImpl implements HtmlHandlerResolver {


    private static String FILE_PATH_PREFIX = "D:/downloads";
    private static String JS_PATH_SUFFIX = "/js";
    private static String CSS_PATH_SUFFIX = "/css";
    private static String IMAGES_PATH_SUFFIX = "/images";



    /**
     * @param url              要解析的url
     * @param filePathPrefix   页面保存的磁盘路径
     * @param mainHtmlFileName HTML页面文件名称
     * @return
     */
    @Override
    public String dowmload(String url, String filePathPrefix, String mainHtmlFileName) {
        return parse(url, filePathPrefix, RegExUtils.matchSpecialChar(mainHtmlFileName));
    }


    private String pathConvert(String realSavePath) {
        String relativelySavePath = StringUtils.sub(realSavePath, CommonContent.PAGE_UPLOAD);
        return relativelySavePath.startsWith("http") ? relativelySavePath : CommonContent.DO_MAIN + relativelySavePath;
    }

    /**
     * 解析入口
     *
     * @param url              要解析的url
     * @param filePathPrefix   页面保存的磁盘路径
     * @param mainHtmlFileName HTML页面文件名称
     * @return
     */
    private String parse(String url, String filePathPrefix, String mainHtmlFileName) {

        Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla").timeout(3 * 1000).get();
        } catch (IOException e) {
            log.error("网页url地址解析失败，网址：{}， 异常： {}", url, e.getMessage());
            return null;
        }
        // 网页链接/css
        Elements links = doc.select("[href]");
        for (Element link : links) {
            // 得到连接
            String linkHref = link.attr("abs:href");
            // 得到文本信息
            String linkText = link.text();
            String realSavePath = parseXmlElement(linkHref, filePathPrefix);
            // url: 域名 + \page-resource-upload\2020-08-24\c15c8a11ab0c876f821c6bdf26083333-cd2837d74f674d3e99d3721747ed20a7\fei7837226-article-details-79377906.html
            link.attr("href", pathConvert(realSavePath));

        }


        // js样式文件
        Elements cssSrcElements = doc.select("script[src]");
        for (Element link : cssSrcElements) {
            // 得到连接
            String srcHref = link.attr("abs:src");
            String realSavePath = parseXmlElement(srcHref, filePathPrefix);
            link.attr("src", pathConvert(realSavePath));
        }

        // 图片及js/css资源文件
        Elements srcElements = doc.select("img[src]");
        for (Element link : srcElements) {
            // 得到链接
            String srcHref = link.attr("abs:src");
            String realSavePath = parseXmlElement(srcHref, filePathPrefix);
            link.attr("src", pathConvert(realSavePath));
        }
        return downloadAsHtml(doc.toString(), filePathPrefix, mainHtmlFileName);
    }


    /**
     * xml数据保存为HTML页面
     *
     * @param text
     * @param filePath
     * @param fileName
     */
    public static String downloadAsHtml(String text, String filePath, String fileName) {
        // 创建目录
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        // 得到磁盘绝对路径，开始保存文件
        File realFile = new File(file.getAbsolutePath() + "/" + fileName);
        try {
            OutputStream os = new FileOutputStream(realFile);
            os.write(text.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 获取文件的绝对路径
        String absolutePath = realFile.getAbsolutePath();
        log.info("main HTML页面保存成功！！！路径：{}", absolutePath);
        return absolutePath;

    }


    /**
     * 解析元素嵌套链接
     *
     * @param linkValue
     * @return
     */
    private String parseXmlElement(String linkValue, String filePathPrefix) {
        if (linkValue.isEmpty()) {
            return "";
        }
        String realFilePath = urlSourceDownload(linkValue, filePathPrefix);
        log.info("原链接：{}, 本地连接：{} ", linkValue, realFilePath);
        return realFilePath;
    }

    /**
     * 网页源代码连接资源下载(嵌套的HTML页面正常返回，不做下载)
     *
     * @param fileUrl
     * @return 返回本地路径
     * @throws IOException
     */
    public static String urlSourceDownload(String fileUrl, String filePathPrefix) {
        Connection.Response response = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            Connection connection = Jsoup.connect(fileUrl);
            response = connection.method(Connection.Method.GET).ignoreContentType(true).timeout(5 * 1000).execute();
            bufferedInputStream = response.bodyStream();
        } catch (Exception e) {
            return fileUrl;
        }
        String localRealFilePath = "";
        String fileName = "";
        int indexOf = fileUrl.lastIndexOf("/");
        String urlFileName = fileUrl.substring(indexOf + 1);

        String contentType = response.contentType();

        // 内嵌网页链接
        if (contentType.startsWith("text/html")) {
            return fileUrl;
        }
        // images
        if (contentType.startsWith("image")) {
            fileName = urlFileName.contains(".") ? urlFileName : (StUtils.getUUID() + urlFileName + ".png");
            localRealFilePath = saveFile(bufferedInputStream, filePathPrefix + IMAGES_PATH_SUFFIX, fileName);
        }

        // js
        if (contentType.startsWith("application")
                || contentType.startsWith("text/javascript")) {
            fileName = urlFileName.contains(".") ? urlFileName : (StUtils.getUUID() + urlFileName);
            // 过滤文件名中的特殊字符
            int indexSpecialChar = RegExUtils.matchIndexSpecialChar(fileName);
            fileName = indexSpecialChar == -1 ? fileName : fileName.substring(0, indexSpecialChar);
            localRealFilePath = saveFile(bufferedInputStream, filePathPrefix + JS_PATH_SUFFIX, fileName);
        }

        // css
        if (contentType.startsWith("text/css")) {
            if (!urlFileName.contains(".")) {
                return fileUrl;
            }
            // 过滤特殊字符
            int indexSpecialChar = RegExUtils.matchIndexSpecialChar(urlFileName);
            fileName = indexSpecialChar == -1 ? urlFileName : urlFileName.substring(0, indexSpecialChar);
            localRealFilePath = saveFile(bufferedInputStream, filePathPrefix + CSS_PATH_SUFFIX, fileName);
        }
        return localRealFilePath;
    }


    /**
     * 保存文件到磁盘
     *
     * @param bufferedInputStream
     * @param savePath
     */
    public static String saveFile(BufferedInputStream bufferedInputStream, String savePath, String fileName) {
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
            while ((readLenghth = bufferedInputStream.read(buffer, 0, 1024)) != -1) {
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
                bufferedInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return realFile.getAbsolutePath();
    }

}
