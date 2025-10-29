// package com.vv.finance.investment.bg.mq;
//
// import cn.hutool.core.bean.BeanUtil;
// import cn.hutool.core.date.DatePattern;
// import cn.hutool.core.date.LocalDateTimeUtil;
// import cn.hutool.core.util.IdUtil;
// import com.alibaba.fastjson.JSON;
// import com.vv.finance.common.constants.GlobalConstants;
// import com.vv.finance.common.entity.quotation.StockNameMsgDTO;
// import com.vv.finance.investment.bg.api.stock.StockInfoApi;
// import com.vv.finance.investment.bg.stock.info.StockDefine;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
// import org.apache.rocketmq.spring.annotation.ConsumeMode;
// import org.apache.rocketmq.spring.annotation.MessageModel;
// import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
// import org.apache.rocketmq.spring.annotation.SelectorType;
// import org.apache.rocketmq.spring.core.RocketMQListener;
// import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
// import org.slf4j.MDC;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Component;
//
// import javax.annotation.Resource;
//
// /**
//  * @ClassName SnapshotTagListener
//  * @Deacription 消费股票名称
//  * @Author lh.sz
//  * @Date 2020年11月06日 16:57
//  **/
// @Component
// @Slf4j
// @RocketMQMessageListener(
//         consumerGroup = "${rocketmq.hk.stock.name.consumer.group:hk_stock_name_consumer_group}",
//         topic = "${rocketmq.hk.stock.name.topic.key:hk_stock_name_topic}",
//         selectorType = SelectorType.TAG,
//         consumeMode = ConsumeMode.ORDERLY,
//         messageModel = MessageModel.CLUSTERING
// )
// public class StockNameTopicListener implements RocketMQListener<String>, RocketMQPushConsumerLifecycleListener {
//
//     @Value("${rocketmq.instance-name}")
//     private String instanceName;
//
//     @Resource
//     private StockInfoApi stockInfoApi;
//
//     @Override
//     public void onMessage(String message) {
//         try {
//             MDC.put(GlobalConstants.TRACE_ID, IdUtil.simpleUUID());
//             log.debug("接收股票名称MQ消息：{}", message);
//             StockNameMsgDTO msgDTO = JSON.parseObject(message, StockNameMsgDTO.class);
//             StockDefine stockDefine = BeanUtil.copyProperties(msgDTO, StockDefine.class, "updateStockNameTime");
//             stockDefine.setUpdateStockNameTime(LocalDateTimeUtil.parse(msgDTO.getUpdateStockNameTime(), DatePattern.NORM_DATETIME_FORMATTER));
//             stockInfoApi.saveStockInfo(stockDefine);
//         } catch (Exception e) {
//             log.error("股票名称MQ消息处理错误", e);
//         }
//     }
//
//     /**
//      * 初始化consumer信息 instanceName
//      *
//      * @param consumer
//      */
//     @Override
//     public void prepareStart(DefaultMQPushConsumer consumer) {
//         consumer.setInstanceName(instanceName.concat("bg"));
//     }
//
// }
