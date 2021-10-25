package com.example.mongodbdemo.data.vo;

import com.example.mongodbdemo.data.dto.CustomerServiceInfoDTO;
import lombok.Data;

import java.util.List;

/**
 * @Author xieyitong  <xieyitong@situdata.com>
 * @Date 2020/9/7 18:02
 **/
@Data
public class CustomerServiceInfoVO {
    private List<CustomerServiceInfoDTO> customerServiceInfoList;

}
