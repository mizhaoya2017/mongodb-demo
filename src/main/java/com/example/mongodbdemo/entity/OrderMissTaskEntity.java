package com.example.mongodbdemo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @program: optrace
 * @description: 订单丢task的记录信息
 * @author: Logan
 * @create: 2020-11-18 20:56
 **/
@Document(collation = "order_miss_task")
@Data
public class OrderMissTaskEntity {
    @Id
    private String id;
    private Integer taskInfoId;
    private String orderId;
    private Integer orderInfoId;
}
