package com.example.mongodbdemo.service.mq;

import com.example.mongodbdemo.content.CommonContent;
import com.example.mongodbdemo.data.dto.RecordInfoDTO;
import com.example.mongodbdemo.utils.StUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MQ生产者
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2021/1/9 18:36
 **/
@Slf4j
@Component
public class MQSenderService {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    final RabbitTemplate.ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {

        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            log.info("correlationData: {}", correlationData);
            System.out.println("ack: " + ack);
            log.info("消息 ack={}", ack);
            if (!ack) {
                byte[] body = correlationData.getReturnedMessage().getBody();
                String returnData = new String(body);
                log.error("消息处理发生异常！！！异常数据={}", returnData);
            }
        }

    };

    final RabbitTemplate.ReturnCallback returnCallback = new RabbitTemplate.ReturnCallback() {

        @Override
        public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
            log.info("return exchange: {}, routingKey: {}, replyCode: {}, replyText: {}", exchange, routingKey, replyCode, replyText);
        }
    };

    /**
     * 发送消息方法调用: 构建Message消息
     *
     * @param recordInfoDTO
     * @throws Exception
     */
    public void sendRecordInfo(RecordInfoDTO recordInfoDTO) {
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnCallback(returnCallback);
        //id + 时间戳 全局唯一
        CorrelationData correlationData = new CorrelationData(StUtils.getUUID() + System.currentTimeMillis());
        rabbitTemplate.convertAndSend(CommonContent.APMQ_RECORDING_EXCHANGE, CommonContent.APMQ_RECORDING_QUEUE, recordInfoDTO, correlationData);
    }
}
