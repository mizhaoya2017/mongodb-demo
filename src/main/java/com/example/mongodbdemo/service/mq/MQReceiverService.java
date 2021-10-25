package com.example.mongodbdemo.service.mq;

import com.example.mongodbdemo.content.CommonContent;
import com.example.mongodbdemo.data.dto.RecordInfoDTO;
import com.example.mongodbdemo.service.RecordInfoService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * MQ 消息消费者
 *
 * @Author Wang XinXin  <wangxinxin@situdata.com>  2021/1/9 18:52
 **/

@RabbitListener(
        bindings = {@QueueBinding(
                value = @Queue(value = CommonContent.APMQ_RECORDING_QUEUE, durable = "true"),
                exchange = @Exchange(value = CommonContent.APMQ_RECORDING_EXCHANGE, type = "topic", durable = "true", ignoreDeclarationExceptions = "true"),
                key = CommonContent.APMQ_RECORDING_QUEUE
        ),
                // 测试 队列
                @QueueBinding(
                        value = @Queue(value = CommonContent.APMQ_RECORDING_QUEUE_TEST, durable = "true"),
                        exchange = @Exchange(value = CommonContent.APMQ_RECORDING_EXCHANGE, type = "topic", durable = "true", ignoreDeclarationExceptions = "true"),
                        key = CommonContent.APMQ_RECORDING_QUEUE_TEST
                )
        }

)
@Slf4j
@Component
public class MQReceiverService {

    @Autowired
    private RecordInfoService recordInfoService;


    @RabbitHandler
    public void onRecordInfoMessage(@Payload RecordInfoDTO recordInfoDTO, Channel channel, @Headers Map<String, Object> headers) throws IOException {

        long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            log.info("recording -> recordInfo信息， MQ异步处理开始, taskId={}, index={}", recordInfoDTO.getTaskId(), recordInfoDTO.getIndex());
            long startTime = System.currentTimeMillis();
            recordInfoService.recordOptionReadyCAS(recordInfoDTO);
            //手工ack。false只确认当前一个消息收到，true确认所有consumer获得的消息
            channel.basicAck(deliveryTag, false);
            log.info("recording -> recordInfo信息， MQ异步处理完成,taskId={}, index={},耗时时间={} ", recordInfoDTO.getTaskId(), recordInfoDTO.getIndex(), (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            log.error("MQ消费者消费消息时，消费逻辑发生异常.当前消息将重新回退到队列", e);
            //ack返回false，并重新回到队列; 只确认当前一个消息
            channel.basicNack(deliveryTag, false, true);
        }
    }

}
