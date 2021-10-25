package com.example.mongodbdemo.controller;

import com.example.mongodbdemo.data.dto.RecordInfoDTO;
import com.example.mongodbdemo.data.vo.CreateRecordInfoVO;
import com.example.mongodbdemo.data.vo.Result;
import com.example.mongodbdemo.service.RecordInfoService;
import com.example.mongodbdemo.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * description:
 * Created by whq on 2020/7/13
 */
@Validated
@RestController
@RequestMapping("/record")
@Slf4j
public class OperationTraceController {

    @Autowired
    private RecordInfoService recordOptionService;


    /**
     * 传输录制内容
     *
     * @param recordInfoDTO
     * @return
     */
    @PostMapping("/recording")
    public Result<CreateRecordInfoVO> recording(HttpServletRequest request, @RequestBody RecordInfoDTO recordInfoDTO) {
        // 日志输出
        String orderId = recordInfoDTO.getOrderInfo() != null ? recordInfoDTO.getOrderInfo().getOrderId() : "当前请求无orderId";
        log.info("control-recording接口接收到视频片段, taskId={}, orderId = {}, index ={}, last={} ", recordInfoDTO.getTaskId(), orderId, recordInfoDTO.getIndex(), recordInfoDTO.getLast());

        log.info("request client id :{}", HttpRequestUtils.getIpAddr(request));
        if (StringUtils.isEmpty(recordInfoDTO.getTaskId())) {
            log.error("传入taskId空了！");
        }
        return recordOptionService.recordOptionAsyncMQHandler(recordInfoDTO);
    }

}
