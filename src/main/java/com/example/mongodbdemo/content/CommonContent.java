package com.example.mongodbdemo.content;

import com.example.mongodbdemo.enums.StorageType;
import com.example.mongodbdemo.enums.StorageVersionType;
import com.example.mongodbdemo.oss.service.OSSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/13 11:35
 **/
@Configuration
@Slf4j
public class CommonContent {
    public static final String SYSTEM = "系统";

    /**
     * MQ常量信息
     */
    public static final String APMQ_RECORDING_EXCHANGE = "OPTRACE-RECORDING-EXCHANGE-beta";
    public static final String APMQ_RECORDING_QUEUE = "optrace-recording-queue-beta";
    public static final String APMQ_RECORDING_EXCHANGE_TEST = "OPTRACE-RECORDING-EXCHANGE-1-TEST";
    public static final String APMQ_RECORDING_QUEUE_TEST = "optrace-recording-queue-1-test";

    /**
     * 版本管理常量
     */
    public static final String OPERATION_VERSION_AUTOMATIC_NAME = "V1";

    /**
     * 可回溯列表Excel下载文件名
     */
    public static final String RECORD_ORDER_EXCEL_NAME = "optrace-record-order";
    public static final String RECORD_ORDER_EXCEL_SHEET = "sheet";
    /**
     * 详情页客服信息excel下载常量
     */
    public static final String RECORD_CONSUMER_RESOURCE_EXCEL_NAME = "optrace-consumer-info";
    public static final String RECORD_CONSUMER_RESOURCE_EXCEL_SHEET = "sheet";
    /**
     * 订单缺失列表页下载
     */
    public static final String ABNORMAL_ORDER_LIST_EXCEL_NAME = "abnormal-order-list";
    public static final String FAILED_ORDER_LIST_EXCEL_NAME = "failed-order-list";
    /**
     * 详情页图片zip包文件名
     */
    public static final String RECORD_ORDER_IMAGE_ZIP_NAME = "optrace-images";
    /**
     * base64
     */
    public static final String BASE64 = ";base64,";
    public static final String IMAGE_SUFFIX_PNG = "png";
    public static final String IMAGE_BASE64_MOST_PREFIX = "data:image/";

    /**
     * 文件下载字体
     */
    public static List<String> CONTENT_TYPE_FONT_TTF = new ArrayList<>();
    public static List<String> CONTENT_TYPE_FONT_WOFF = new ArrayList<>();
    public static List<String> URL_SUFFIX = new ArrayList<>();

    static {
        CONTENT_TYPE_FONT_TTF.add("application/x-font-ttf");
        CONTENT_TYPE_FONT_TTF.add("application/font-sfnt");
    }

    static {
        CONTENT_TYPE_FONT_WOFF.add("application/x-font-woff");
        CONTENT_TYPE_FONT_WOFF.add("application/font-woff");
    }

    static {
        URL_SUFFIX.add("png");
        URL_SUFFIX.add("jpeg");
        URL_SUFFIX.add("jpg");
        URL_SUFFIX.add("gif");
        URL_SUFFIX.add("ico");
        URL_SUFFIX.add("jfif");
        URL_SUFFIX.add("jpe");
        URL_SUFFIX.add("net");
        URL_SUFFIX.add("rp");
        URL_SUFFIX.add("tif");
        URL_SUFFIX.add("tiff");
        URL_SUFFIX.add("wbmp");
        URL_SUFFIX.add("avg");
        // 字体
        URL_SUFFIX.add("ttf");
        URL_SUFFIX.add("woff");
        // css
        URL_SUFFIX.add("css");
        // js
        URL_SUFFIX.add("js");
    }

    public static final String DECRYPT_KEY = "0ca03a92751282f6ee05f8e6e42020b5";
    public static final String IMAGE = "image";
    public static final String DOCUMENT_PDF = "pdf";
    public static final String VIDEO = "video";
    public static final String CONTENT_TYPE_JS = "application/x-javascript";
    public static final String CONTENT_TYPE_CSS = "text/css";
    public static final String IMAGE_FILE_NAME_SUFFIX = ".png";
    public static final String FONT_TTF_FILE_NAME_SUFFIX = "ttf";
    public static final String FONT_WOFF_FILE_NAME_SUFFIX = "woff";
    public static final String JS_FILE_NAME_SUFFIX = "js";
    public static final String CSS_FILE_NAME_SUFFIX = "css";

    /**
     * 资源下载状态
     */
    public static final Integer RESOURCE_DOWNLOAD_STATUS_REPLACED_SUCCESS = 1;
    public static final Integer RESOURCE_DOWNLOAD_STATUS_REPLACED_FAILURE = 2;
    public static final Integer RESOURCE_DOWNLOAD_STATUS_DEFAULT = 0;
    public static final Integer RESOURCE_DOWNLOAD_STATUS_RUNNING = 5;


    /**
     * redis
     */
    public final static String RECORD_INFO_SOURCE_PREFIX = "7dc79013785946e4a60d665a8dc2f476";
    public static Integer CACHE_SELECT_RANGE_RATE;
    public static Integer CACHE_SELECT_RANGE_RATE_START = 0;

    public static String FETCH_URL = "";

    /**
     * 平台类型；platform字段的可选值
     */
    public static final String PLATFORM_APP = "app";
    public static final String PLATFORM_APPLET = "小程序";
    /**
     * rrweb里面解析type=4的节点
     */
    public static final String RRWEB_PAGE_URL_REG_PATTERN = "(\\{\"type\":4,\"data\":\\{\"href\":\")((http|https)://[\\w./]+\\?)";
    /**
     * rrweb里面解析type=4的节点 请求地址所在分组
     */
    public static final Integer RRWEB_PAGE_URL_REG_GROUP = 2;


    /**
     * 域名地址
     */
    public static String DO_MAIN;
    public static String PAGE_UPLOAD = "resource";
    public static String RECORD_INFO_PATH = "recordInfo-download";
    public static String APPLET_VERSION_IMAGE_PATH = "applet-version-image";
    /**
     * recording接口recordInfo视频资源地址
     */
    public static String RECORD_INFO_UPLOAD_PATH = PAGE_UPLOAD + "/" + RECORD_INFO_PATH;
    /**
     * 小程序版本管理图片上传地址
     */
    public static String APPLET_VERSION_IMAGE_UPLOAD_PATH = PAGE_UPLOAD + "/" + APPLET_VERSION_IMAGE_PATH;
    /**
     * 定时任务间隔时间
     */
    public static String FIXED_RATE_TIME_SED;

    /**
     * OSS 过期时间为六天
     */
    public static Integer OSS_EXPIRED_MINUTES = 60 * 24 * 6;

    /**
     * 获取oss资源，重定向到oss，将objectName放到Reques的Parameter中
     */
    public static String RESOURCE_NAME_REQ_PARAM = "resouceName";
    /**
     * OSS存储路径前缀
     */
    public static String RESOURCE_OSS_PREFIX;


    @Value("${storage.version-type:baseVersionType}")
    private String storageVersionType;

    @Bean
    public StorageVersionType storageVersionType() {
        return StorageVersionType.getStorageVersionType(storageVersionType);
    }


    @Value("${access.domain}")
    public void setDoMain(String doMain) {
        DO_MAIN = doMain;
    }

    @Value("${storage.type:MySql}")
    private String storageType;

    @Bean
    public StorageType storageType() {
        return StorageType.getStorageType(storageType);
    }

    @Autowired(required = false)
    private OSSService aliyunOssService;
    @Autowired(required = false)
    private OSSService qingStorOssService;
    @Autowired(required = false)
    private OSSService zanhuaOssService;
    @Autowired(required = false)
    private OSSService nasSharedService;
    @Autowired(required = false)
    private OSSService h5ImgService;

    @Bean
    public OSSService myOssService(StorageType storageType) {
        switch (storageType) {
            case MYSQL_STORAGE:
                return null;
            case OSS_ALI_STORAGE:
                return aliyunOssService;
            case OSS_QINGSTOR_STORAGE:
                return qingStorOssService;
            case OSS_ZANHUA_STORAGE:
                return zanhuaOssService;
            case NAS_STORAGE:
                return nasSharedService;
            case H5IMG_STORAGE:
                return h5ImgService;
            default:
                return null;
        }

    }

    @Value("${access.resource-oss-prefix: resource}")
    public void setResourceOssPrefix(String resourceOssPrefix) {
        RESOURCE_OSS_PREFIX = resourceOssPrefix;
    }

    @Value("${access.fixedRateTimeSed: 10000}")
    public void setFixedRateTimeSed(String fixedRateTimeSed) {
        FIXED_RATE_TIME_SED = fixedRateTimeSed;
    }

    @Value("${access.range-rate}")
    public void setRangeRate(Integer rangeRate) {
        CACHE_SELECT_RANGE_RATE = rangeRate;
    }

    @Value("${fetch.url}:test-url")
    public void setFetchUrl(String fetchUrl) {
        FETCH_URL = fetchUrl;
    }

    @Bean
    public InetAddress hostAddr() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
            return addr;
        } catch (UnknownHostException e) {
            log.error("获取服务器地址失败", e);
            return null;
        }
    }
}
