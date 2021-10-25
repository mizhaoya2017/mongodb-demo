package com.example.mongodbdemo.oss.service.impl;

import com.aliyun.oss.OSSClient;
import com.amazonaws.util.IOUtils;
import com.example.mongodbdemo.data.vo.RespCode;
import com.example.mongodbdemo.excepition.ResultException;
import com.example.mongodbdemo.oss.config.H5ImgProperties;
import com.example.mongodbdemo.oss.service.OSSService;
import com.sinosoft.image.client.ImgManagerClient;
import com.sinosoft.image.client.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @program: optrace
 * @description: 阿里云OSS服务
 * @author: Logan
 * @create: 2020-09-14 22:07
 **/
@Service("h5ImgService")
@Slf4j
public class H5ImgServiceImpl implements OSSService {
    @Autowired(required = false)
    private OSSClient ossClient;
    @Autowired(required = false)
    private H5ImgProperties h5ImgProperties;

    @Override
    public void putObject(String objectName, InputStream inputStream) {
        String businessNo = objectName.substring(objectName.lastIndexOf("/") + 1);

        File uploadFile = null;
        OutputStream os = null;
        try {
            if (businessNo.lastIndexOf(".") == -1){
                // 存文本
                uploadFile = File.createTempFile(objectName, ".txt");
            } else {
                // 存资源
                uploadFile = new File(objectName);
            }
            //uploadFile = new File(objectName);
            os = new FileOutputStream(uploadFile);
            int len = 0;
            byte[] buffer = new byte[4096];
            while ((len = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ImgBatchUploadRequestVo batchUploadVo = new ImgBatchUploadRequestVo();
        batchUploadVo.setComCode(h5ImgProperties.getComCode());// 机构号码
        batchUploadVo.setComName(h5ImgProperties.getComName());// 机构名称
        batchUploadVo.setOperator(h5ImgProperties.getOperatorName());// 操作员ID
        batchUploadVo.setOperatorName(h5ImgProperties.getOperatorName());// 操作员姓名
        batchUploadVo.setOperatorRole(h5ImgProperties.getOperatorRole());// 操作角色

        /** 业务信息 */
        ImgBussVo bussVo = new ImgBussVo();
        bussVo.setBussType(h5ImgProperties.getBusinessType());
        // bussVo.setClassCode(null);
        bussVo.setBussNo(businessNo);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String date = df.format(System.currentTimeMillis());
        bussVo.setBussDate(date);
        bussVo.setBussCom(h5ImgProperties.getBussCom());
        batchUploadVo.setBussVo(bussVo);
        /** 影像上传时的影像描述信息 */
        List<ImgMetaDataVo> uploadDataVos = new ArrayList<ImgMetaDataVo>();
        ImgMetaDataVo uploadDataVo = new ImgMetaDataVo();
        uploadDataVo.setImgType(h5ImgProperties.getImgType());
        uploadDataVo.setImgTypeName(h5ImgProperties.getImgTypeName());
        uploadDataVo.setUploadNode(h5ImgProperties.getUploadNode());
        uploadDataVo.setValidFlag(1);
        uploadDataVo.setUploadFile(uploadFile);
        uploadDataVos.add(uploadDataVo);
        batchUploadVo.setUploadDataVos(uploadDataVos);

        ImgManagerClient imgClient = new ImgManagerClient(h5ImgProperties.getServiceUrl());
        ImgBatchUploadResponseVo responseDataVo = imgClient.batchUpload(batchUploadVo);
        List<ImgMetaDataVo> metaDatas = responseDataVo.getSuccessDatas();
        ImgMetaDataVo imgMetaDataVo1 = responseDataVo.getSuccessDatas().get(0);

        String imgURL = StringUtils.EMPTY;
        if (metaDatas.size() == 1) {
            ImgMetaDataVo imgMetaDataVo = metaDatas.get(0);
        }
        for (ImgMetaDataVo dataVo : metaDatas) {
            System.out.println("imgId=" + dataVo.getImgId() + ",filesize=" + dataVo.getFileSize() + ",filepath=" + dataVo.getAppendPath() + dataVo.getFileOrgName());
        }
        //删除临时文件
        uploadFile.delete();
    }

    @Override
    public void putObject(String objectName, String content) {
        try (ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes())) {
            this.putObject(objectName, is);
        } catch (IOException e) {
            log.error("存储字符串失败", e);
            throw new ResultException(RespCode.ERROR_3000_);
        }
    }


    /**
     * 根据objectName返回文件流
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 09:51
     */
    @Override
    public String getObjectContentString(String objectName) {
        try {
            String bussNo = objectName.substring(objectName.lastIndexOf("/") + 1);
            ImageQueryDownRequestVo imageQueryDownRequestVo = new ImageQueryDownRequestVo();
            imageQueryDownRequestVo.setBussType(h5ImgProperties.getBusinessType());
            imageQueryDownRequestVo.setBussNo(bussNo);
            ImgManagerClient imgClient = new ImgManagerClient(h5ImgProperties.getServiceUrl());
            ImageQueryDownResponseVo response = imgClient.ImageQueryDown(imageQueryDownRequestVo);
            List<ImgNodeVo> imgNodes = response.getImgNodes();
            String imgURL = StringUtils.EMPTY;
            if (Objects.nonNull(imgNodes) && imgNodes.size() > 0) {
                imgURL = imgNodes.get(0).getImgURL();
            } else {
                return null;
            }
            InputStream inputStreamByUrl = getInputStreamByUrl(imgURL);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStreamByUrl.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String str = result.toString(StandardCharsets.UTF_8.name());
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取浏览地址（有有效期）
     *
     * @param objectName 例如 /video/video.mp4
     * @return
     * @author Logan
     * @date 2020-09-05 18:23
     */
    @Override
    public String getObjectSignatureUrl(String objectName) {
        try {
            String bussNo = objectName.substring(objectName.lastIndexOf("/") + 1);
            ImageQueryDownRequestVo imageQueryDownRequestVo = new ImageQueryDownRequestVo();
            imageQueryDownRequestVo.setBussType(h5ImgProperties.getBusinessType());
            imageQueryDownRequestVo.setBussNo(bussNo);
            ImgManagerClient imgClient = new ImgManagerClient(h5ImgProperties.getServiceUrl());
            ImageQueryDownResponseVo response = imgClient.ImageQueryDown(imageQueryDownRequestVo);
            List<ImgNodeVo> imgNodes = response.getImgNodes();
            String imgURL = StringUtils.EMPTY;
            if (Objects.nonNull(imgNodes) && imgNodes.size() > 0) {
                imgURL = imgNodes.get(0).getImgURL();
                return imgURL;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    /**
     * 根据地址获得数据的输入流
     *
     * @param strUrl 网络连接地址
     * @return url的输入流
     */
    public static InputStream getInputStreamByUrl(String strUrl) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(20 * 1000);
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.copy(conn.getInputStream(), output);
            return new ByteArrayInputStream(output.toByteArray());
        } catch (Exception e) {
            log.error(e + "");
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
                log.error(e + "");
            }
        }
        return null;
    }
}
