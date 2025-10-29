//package com.vv.finance.investment.bg.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.vv.finance.common.entity.common.StockMoveTheme;
//import com.vv.finance.investment.bg.entity.move.StockMove;
//import com.vv.finance.investment.bg.stock.move.service.impl.MoveThemeServiceImpl;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
//import org.apache.rocketmq.spring.annotation.ConsumeMode;
//import org.apache.rocketmq.spring.annotation.MessageModel;
//import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.annotation.SelectorType;
//import org.apache.rocketmq.spring.core.RocketMQListener;
//import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.Date;
//
///**
// * @ClassName SnapshotTagListener
// * @Deacription 消费股票快照
// * @Author lh.sz
// * @Date 2020年11月06日 16:57
// **/
//@Component
//@Slf4j
//@RocketMQMessageListener(
//        consumerGroup = "${rocketmq.move.consumer.group}",
//        topic = "${rocketmq.move.topic.key}",
//        selectorType = SelectorType.TAG,
//        consumeMode = ConsumeMode.CONCURRENTLY,
//        messageModel = MessageModel.CLUSTERING
//)
//public class MoveTopicListener implements RocketMQListener<String>, RocketMQPushConsumerLifecycleListener {
//
//    @Value("${rocketmq.instance-name}")
//    private String instanceName;
//
//    @Resource
//    MoveThemeServiceImpl moveThemeService;
//
//    @Override
//    public void onMessage(String message) {
//        StockMoveTheme stockMoveTheme = JSON.parseObject(message, StockMoveTheme.class);
//        StockMove stockMove = new StockMove();
//        stockMove.setCode(stockMoveTheme.getCode());
//        stockMove.setName(stockMoveTheme.getName());
//        stockMove.setMoveType(stockMoveTheme.getMoveType());
//        stockMove.setMoveData(JSON.toJSONString(stockMoveTheme.getMoveData()));
//        stockMove.setStockTime(stockMoveTheme.getStockTime());
//        stockMove.setCreateTime(new Date());
//        stockMove.setUpdateTime(new Date());
//        moveThemeService.save(stockMove);
//        log.info("异动数据落库成功！股票代码{}", stockMoveTheme.getCode());
//    }
//
//    /**
//     * 初始化consumer信息 instanceName
//     *
//     * @param consumer
//     */
//    @Override
//    public void prepareStart(DefaultMQPushConsumer consumer) {
//        consumer.setInstanceName(instanceName.concat("move"));
//    }
//
//
//}
