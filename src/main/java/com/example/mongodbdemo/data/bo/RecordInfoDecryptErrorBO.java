package com.example.mongodbdemo.data.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * recording接口视频信息AES解密失败时将id信息封装该BO
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/9/12 11:33
 **/
@Data
@NoArgsConstructor
public class RecordInfoDecryptErrorBO {

    /**
     * recordInfo表主键id
     */
    private Integer rid;
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 可回溯编码
     */
    private String taskId;
}
