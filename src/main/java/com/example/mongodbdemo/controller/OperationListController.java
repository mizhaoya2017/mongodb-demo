package com.example.mongodbdemo.controller;

import com.example.mongodbdemo.data.vo.OrderRecordListVO;
import com.example.mongodbdemo.data.vo.Result;
import com.example.mongodbdemo.service.RecordInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2020/7/23 17:00
 **/
@RestController
@RequestMapping("/record")
public class OperationListController {

    @Autowired
    private RecordInfoService recordInfoService;

    /**
     * 程序可回溯列表(包含orderId或者其他可回溯列表)(1:0)
     *
     * @param orderRecordListVO
     * @return
     */
    @PostMapping("/getOrderRecordList")
    public Result orderRecordList(@RequestBody OrderRecordListVO orderRecordListVO) {
        Result<Object> result = recordInfoService.getOrderRecordList(orderRecordListVO);
        return result;
    }
}
