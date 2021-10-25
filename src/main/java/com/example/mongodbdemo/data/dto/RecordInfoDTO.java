package com.example.mongodbdemo.data.dto;

import com.example.mongodbdemo.data.bo.OperationVersionSourceBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description:
 * Created by whq on 2020/7/13
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordInfoDTO implements Serializable {

    private static final long serialVersionUID = 5231134212346077688L;

    /**
     * 订单
     */
    private OrderInfoDTO orderInfo;
    /**
     * 视频密文
     */
    private String recordInfo;
    /**
     * 可回溯编码
     */
    private String taskId;
    /**
     * 是否是最后一个task  1-是，0-不是
     */
    private Integer last;

    /**
     * 文件下载地址
     */
    private String fileDownloadUri;

    /**
     * 时间戳，由前端传过来的
     */
    private String timestamp;

    //  暂时先不校验  @NotNull(message = "index不能为空")
    private Integer index;
    //暂时先不校验  @NotNull(message = "mode")
    private Integer mode;
    /**
     * 1:AES加密
     * 2:明文
     * 3:压缩
     **/
    private Integer encodeMode;

    /**
     * 文件下载地址
     */
    private Integer optraceFile;

    public static List<OperationVersionSourceBO> convertOperationVersionSourceBO(RecordInfoDTO recordInfoDTO, List<String> pageUrlInfo) {

        OrderInfoDTO orderInfo = recordInfoDTO.getOrderInfo();
        List<OperationVersionSourceBO> collect = pageUrlInfo.stream().map(pageUrl -> {
            OperationVersionSourceBO versionSourceBO = new OperationVersionSourceBO();
            versionSourceBO.setOrderId(orderInfo.getOrderId());
            versionSourceBO.setPageUrl(pageUrl);
            versionSourceBO.setPlatform(orderInfo.getPlatform());
            versionSourceBO.setProductName(orderInfo.getProductName());
            versionSourceBO.setTaskId(recordInfoDTO.getTaskId());
            return versionSourceBO;
        }).collect(Collectors.toList());
        return collect;
    }


}
