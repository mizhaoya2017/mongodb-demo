package com.example.mongodbdemo.data.dto;

import lombok.Data;

/**
 * 可回溯详情页客服资源Excel下载
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/10/16 11:08
 **/
@Data
public class ConsumerResourceExcelDTO {
    /**
     * taskId
     */
    private String taskId;
    /**
     * 富文本图片集
     */
//    @NotNull(message = "richTextList不能为空")
//    private List<RichTextDTO> richTextList;

    @Data
    public static class RichTextDTO {
        /**
         * 客服资源表主键id
         */
        private Integer id;
        /**
         * 富文本的图片base64
         */
        private String base64;

    }
}
