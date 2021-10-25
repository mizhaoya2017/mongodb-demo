package com.example.mongodbdemo.oss.service.impl;

import com.example.mongodbdemo.content.CommonContent;
import com.example.mongodbdemo.data.vo.RespCode;
import com.example.mongodbdemo.excepition.ResultException;
import com.example.mongodbdemo.oss.service.OSSService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/22 11:51
 **/
@Slf4j
@Service("nasSharedService")
public class NasSharedServiceImpl implements OSSService {

    /**
     * 保存一个对象，返回对象所在地址
     *
     * @param objectName 例如 /video/video.mp4
     * @param input
     * @return
     * @author Logan
     * @date 2020-09-05 09:49
     */
    @Override
    public void putObject(String objectName, InputStream input) {
        if (StringUtils.isBlank(objectName) || input == null) {
            log.warn("NasSharedServiceImpl putObject()中 objectName 或 InputStream为空... 文件保存失败...");
            return;
        }
        int lastPathIndex = objectName.lastIndexOf("/");
        String filePath = objectName.substring(0, lastPathIndex);
        File mkdirPathFile = new File(filePath);
        if (!mkdirPathFile.exists()) {
            mkdirPathFile.mkdirs();
        }
        // 替换为绝对路径
        String absolutePath = mkdirPathFile.getAbsolutePath();
        String absoluteObjectName = objectName.replace(filePath, absolutePath);
        File file = new File(absoluteObjectName);
        FileOutputStream fileOutputStream = null;
        try {
            // 文件写
            //一次最多读取1k
            byte[] buffer = new byte[1024];
            //实际读取的长度
            int readLenghth;
            fileOutputStream = new FileOutputStream(file);
            //先读出来，保存在buffer数组中
            while ((readLenghth = input.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, readLenghth);
            }
        } catch (IOException e) {
            log.error("Nas OSS IOException  objectName = {}", objectName);
            throw new ResultException(RespCode.ERROR_3004_);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        log.info("Nas 共享空间文件保存成功... filePath: {}", file.getAbsolutePath());
    }

    @Override
    public void putObject(String objectName, String content) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes())) {
            this.putObject(objectName, byteArrayInputStream);
        } catch (Exception exception) {
            log.error("Nas 存储字符串失败！！！ ", exception);
            throw new ResultException(RespCode.ERROR_3000_);
        }

    }

    /**
     * 根据bucket,key返回对象数据流
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 09:51
     */
    @Override
    public String getObjectContentString(String objectName) {
        try {
            String encoding = "UTF-8";
            File file = new File(objectName);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                StringBuilder sb = new StringBuilder();
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    sb.append(lineTxt);
                }
                return sb.toString();
            } else {
                log.warn("系统找不到指定的文件， objectName={}", objectName);
            }
        } catch (Exception e) {
            log.error("读取文件内容出错... objectName={}", objectName);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取浏览地址（nginx做转发）
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 18:23
     */
    @Override
    public String getObjectSignatureUrl(String objectName) {
        return String.format("%s/%s", CommonContent.DO_MAIN, objectName);
    }

    /**
     * 删除一个对象
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 18:35
     */
    @Override
    public void deleteObject(String objectName) {
        if (StringUtils.isBlank(objectName)) {
            log.info("Nas共享存储系统  删除文件对象时 objectName为空！！！ objectName = {}", objectName);
            return;
        }
        try {
            File file = new File(objectName);
            if (file.exists()) {
                file.delete();
                log.info("Nas共享存储系统 文件对象删除成功！！！ objectName = {}", objectName);
            } else {
                log.info("Nas共享存储系统 删除文件对象时， objectName不存在， 删除失败！！！");
            }
        } catch (Exception exception) {
            log.error("Nas共享存储系统 删除对象异常！！!", exception);
            throw new ResultException(RespCode.ERROR_3002_);
        }

    }

    @Override
    public String getBucketName() {
        return null;
    }

    /**
     * 判断是否超时
     *
     * @param code
     * @return
     * @author Logan
     * @date 2020-09-24 11:19
     */
    @Override
    public boolean timeoutAndWait(String code) {
        return false;
    }


}
