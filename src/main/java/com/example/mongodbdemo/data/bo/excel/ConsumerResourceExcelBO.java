package com.example.mongodbdemo.data.bo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.example.mongodbdemo.conf.converter.EasyExcelDateConverter;
import lombok.Data;

import java.io.InputStream;
import java.util.Date;

/**
 * 客服信息Excel下载 (可回溯详情页Excel下载)
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/10/16 9:48
 **/
@Data
@ContentRowHeight(90) // 设置单元格的高为 90
public class ConsumerResourceExcelBO {


    /**
     * 内容
     */
    @ExcelProperty("图片内容")
    @ColumnWidth(30) // 设置单元格的宽为 30
    private InputStream imageContext;
    /**
     * 文本内容
     */
    @ExcelProperty("文本内容")
    private String textContext;

    /**
     * 角色
     */
    @ExcelProperty("角色")
    private String role;


    /**
     * 时间
     */
    @ExcelProperty(value = "时间", converter = EasyExcelDateConverter.class)
    private Date createTime;
}
