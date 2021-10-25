package com.example.mongodbdemo.service;

import com.example.mongodbdemo.data.dto.GetTaskDetailDTO;
import com.example.mongodbdemo.data.dto.OrderInfoDTO;
import com.example.mongodbdemo.data.vo.CustomerServiceInfoVO;
import com.example.mongodbdemo.data.vo.Result;
import com.example.mongodbdemo.entity.CustomerServiceInfoEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author xieyitong  <xieyitong@situdata.com>
 * @Date 2020/9/7 16:30
 **/
public interface ReturnInfoService {

    Result saveCustomerServiceInfo(CustomerServiceInfoVO customerServiceInfo);

    Result getCustomerServiceInfo(GetTaskDetailDTO getTaskDetailDTO);

    Result saveOptraceFile(OrderInfoDTO orderInfoDTO, MultipartFile file);

    /**
     * 根据taskId查询客服信息
     *
     * @param taskId
     * @return
     */
    List<CustomerServiceInfoEntity> findAllByTaskId(String taskId);

    /**
     * 获取资源
     *
     * @param insteadContext
     * @param contextType
     * @return
     */
    String getContextInfo(String insteadContext, String contextType);

    /**
     * 返回资源url
     *
     * @param insteadContext
     * @param contextType
     * @return
     */
    String getContextInfoSignatureUrl(String insteadContext, String contextType);
}
