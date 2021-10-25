package com.example.mongodbdemo.data.vo;

import com.example.mongodbdemo.entity.OperationVersionBasicInfoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/8/14 15:01
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationVersionBasicInfoListVO {


    /**
     * 版本id 唯一标识
     */
    private Integer versionId;
    /**
     * 版本号
     */
    private String version;
    /**
     * 版本更新记录内容
     */
    private String updateRecord;
    /**
     * 责任人记录
     */
    private String responsibleRecord;
    /**
     * 创建人姓名
     */
    private String username;
    /**
     * 备注
     */
    private String remark;
    /**
     * 记录开始时间
     */
    private Timestamp recordTime;


    public static OperationVersionBasicInfoListVO convert(OperationVersionBasicInfoEntity versionBasicInfoEntity) {
        OperationVersionBasicInfoListVO basicInfoListVO = new OperationVersionBasicInfoListVO();
        BeanUtils.copyProperties(versionBasicInfoEntity, basicInfoListVO);
        basicInfoListVO.setVersionId(versionBasicInfoEntity.getId());
        return basicInfoListVO;

    }


}
