package com.example.mongodbdemo.service;

import com.example.mongodbdemo.data.bo.OperationVersionSourceBO;
import com.example.mongodbdemo.data.bo.VersionHistoryOrderInfoBO;
import com.example.mongodbdemo.data.dto.*;
import com.example.mongodbdemo.data.vo.Result;
import com.example.mongodbdemo.entity.OperationVersionBasicInfoEntity;
import com.example.mongodbdemo.entity.OperationVersionResourceEntity;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/12 19:21
 **/
public interface OperationVersionBasicInfoService {

    /**
     * 添加版本
     *
     * @param versionBasicInfoDTO
     * @return
     */
    Result save(OperationVersionBasicInfoDTO versionBasicInfoDTO);

    void saveRecordInfoResource(OperationVersionResourceEntity versionResourceEntity);

    void saveRecordInfoResourceList(List<OperationVersionResourceEntity> versionResourceEntityList);

    /**
     * 版本列表
     *
     * @param basicInfoPageDTO
     * @return
     */
    Result listVersionHandler(OperationVersionBasicInfoPageDTO basicInfoPageDTO);

    /**
     * H5版本订单列表
     *
     * @param basicInfoPageDTO
     * @return
     */
    Result orderProductList(OperationPageCrawPageDTO basicInfoPageDTO);


    void parsePageInfo(OperationVersionSourceBO sourceBO);

    /**
     * 根据平台查询当前最大版本号
     *
     * @param platform H5/app/小程序
     * @return
     */
    OperationVersionBasicInfoEntity findMaxVersion(String platform);

    /**
     * 根据orderId和taskId获取视频信息
     *
     * @param orderSingleProductInfoDTO
     * @return
     */
    Result orderSingleProductInfo(OrderSingleProductInfoDTO orderSingleProductInfoDTO);

    /**
     * 查询当前版本下是否有该url存在
     *
     * @param pageUrl
     * @param version
     * @return
     */
    Integer isExistByUrl(String pageUrl, String version);

    /**
     * 根据主键id更新产品名称
     *
     * @param id
     * @param productName
     */
    void updateProductName(Integer id, String productName);

    /**
     * 保存资源抓取记录
     *
     * @param versionSourceEntity
     * @return
     */
    Integer operationVersionResourceSave(OperationVersionResourceEntity versionSourceEntity);

    /**
     * 根据主键id更新recordinfoId
     *
     * @param recordInfoId recordInfo表主键id
     * @param resourceIds  recourse表主键
     */
    void operationVersionResourceUpdateRecordInfoIdById(Integer recordInfoId, List<Integer> resourceIds);

    /**
     * 根据产品编码表主键Id 查询版本号
     *
     * @param productId
     * @return
     */
    List<OperationVersionBasicInfoEntity> findAllByProductId(Integer productId);

    /**
     * 查询小程序的所有版本
     *
     * @return
     */
    List<OperationVersionBasicInfoEntity> findAllAppletByMoreCreateTime(Integer productId, Timestamp createTime);


    /**
     * 根据解密后的recordInfo判断是否需要下载资源
     *
     * @param decrptRecordInfo 解密后的recordInfo
     * @param versionType      1-web 2-小程序 3-app
     * @return
     * @author Logan
     * @date 2020-09-14 16:48
     */
    boolean needDownloadResource(String decrptRecordInfo, Integer versionType);


    /**
     * 根据资源的url判断，是否需要下载
     *
     * @param url
     * @param versionType 1-web 2-小程序 3-app
     * @return
     * @author Logan
     * @date 2020-09-14 18:11
     */
    boolean needDownloadResouceByUrl(String url, Integer versionType);

    /**
     * 根据前端传过来的页面地址，判断是否需要下载资源
     *
     * @param pageUrls
     * @param versionType
     * @return
     * @author Logan
     * @date 2020-09-14 18:58
     */
    boolean needDownloadResouceByOriginalPages(List<String> pageUrls, Integer versionType);

    /**
     * 根据版本号查询
     *
     * @param versionList 版本号
     * @return
     */
    List<OperationVersionResourceEntity> findAllByVersion(List<String> versionList);

    /**
     * 根据主键查询
     *
     * @param versionKeyId
     * @return
     */
    OperationVersionBasicInfoEntity findAllById(Integer versionKeyId);

    /**
     * 基于平台的自动建立版本（太平财）
     *
     * @param createVersionDTO
     */
    Result saveAutomaticVersionInfo(AutomaticCreateVersionDTO createVersionDTO);

    /**
     * 当新增版本时，更新历史的订单数据版本和recordInfo视频资源
     *
     * @param versionHistoryOrderInfoBO
     */
    void versionHistoryOrderInfoProcess(VersionHistoryOrderInfoBO versionHistoryOrderInfoBO);

    /**
     * 根据版本号和accessUrl查询数据是否存在
     *
     * @param version
     * @param accessUrl
     * @return
     */
    OperationVersionResourceEntity isExistByVersionAndAccessUrl(String version, String accessUrl);
}
