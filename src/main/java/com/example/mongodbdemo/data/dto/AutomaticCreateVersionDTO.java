package com.example.mongodbdemo.data.dto;

import com.example.mongodbdemo.entity.OperationVersionBasicInfoEntity;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 自动创建版本实体
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/10/20 16:20
 **/
@Data
public class AutomaticCreateVersionDTO {

    /**
     * 版本更新内容
     */
    private String updateContext;

    /**
     * 版本时间
     */
    private String versionTime;
    /**
     * 责任人记录
     */
    private String responsibleRecord;
    /**
     * 备注
     */
    private String remark;

    public static OperationVersionBasicInfoEntity converterEntity(AutomaticCreateVersionDTO createVersionDTO, String version) {
        OperationVersionBasicInfoEntity versionBasicInfoEntity = new OperationVersionBasicInfoEntity();
        versionBasicInfoEntity.setUpdateRecord(createVersionDTO.getUpdateContext());
        versionBasicInfoEntity.setVersion(version);
        versionBasicInfoEntity.setRecordTime(new Timestamp(Long.parseLong(createVersionDTO.getVersionTime())));
        versionBasicInfoEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        versionBasicInfoEntity.setResponsibleRecord(createVersionDTO.getResponsibleRecord());
        versionBasicInfoEntity.setRemark(createVersionDTO.getRemark());
        versionBasicInfoEntity.setVersionType(1);
        return versionBasicInfoEntity;
    }


}
