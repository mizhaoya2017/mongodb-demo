package com.example.mongodbdemo.data.dto;

import com.example.mongodbdemo.entity.OperationVersionBasicInfoEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/12 19:23
 **/
@Data
public class OperationVersionBasicInfoDTO {

    /**
     * 版本
     */
    private String version;
    /**
     * 版本更新记录
     */
    private String updateRecord;
    /**
     * 责任人记录
     */
    private String responsibleRecord;
    /**
     * 记录（版本）开始时间
     */
    private Long recordTime;

    /**
     * 版本类型；1=H5,2=小程序，3=APP
     */
    private Integer versionType;

    /**
     * 产品主键Id（小程序版本管理需要）
     */
    private Integer productId;

    /**
     * 备注
     */
    private String remark;


    /**
     * 数据类型转换
     *
     * @param versionBasicInfoDTO
     * @return
     */
    public static OperationVersionBasicInfoEntity convert(OperationVersionBasicInfoDTO versionBasicInfoDTO) {
        OperationVersionBasicInfoEntity versionBasicInfoEntity = new OperationVersionBasicInfoEntity();
        BeanUtils.copyProperties(versionBasicInfoDTO, versionBasicInfoEntity);
        versionBasicInfoEntity.setRecordTime(new Timestamp(versionBasicInfoDTO.getRecordTime()));
        versionBasicInfoEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        return versionBasicInfoEntity;
    }

}
