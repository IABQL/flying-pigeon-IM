package com.iabql.nettyprovider8002.kafka;

import com.alibaba.fastjson.JSONObject;
import com.iabql.nettyprovider8002.enums.MsgActionEnum;
import com.iabql.nettyprovider8002.netty.ChatHandler;
import com.iabql.nettyprovider8002.netty.ChatMsgNetty;
import com.iabql.nettyprovider8002.netty.DataContent;
import com.iabql.nettyprovider8002.netty.UserChanelRel;
import com.iabql.nettyprovider8002.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @KafkaListener(topics = "localhost_8002")
    public void listenGroup(ConsumerRecord<String, String> record) {

        DataContent dataContent = JSONObject.parseObject(record.value(), DataContent.class);
        // 处理消息
        dealMessage(dataContent);

        // 手动提交offset
        //ack.acknowledge();
    }

    // 处理消息
    private void dealMessage(DataContent dataContent) {
        Integer action = dataContent.getAction();
        if(action == MsgActionEnum.CHAT.type) {
            /** 聊天类型的消息，把聊天记录保存到数据库，同时标记消息的签收状态[未签收] **/
            ChatMsgNetty chatMsg = dataContent.getChatMsg();
            String receiverId = chatMsg.getReceiverId();//接收者

            DataContent dataContentMsg = new DataContent();
            dataContentMsg.setChatMsg(chatMsg);

            /*发送消息给接收者*/

            // 获取接收者的channel
            Channel receiverChannel = UserChanelRel.get(receiverId);
            if(receiverChannel ==null){
                // 离线用户
            }else{
                // 当receiverChannel 不为空的时候，从ChannelGroup 去查找对应的channel 是否存在
                Channel findChanel = ChatHandler.users.find(receiverChannel.id());
                if(findChanel!=null){
                    // 用户在线，发送消息
                    receiverChannel.writeAndFlush(
                            new TextWebSocketFrame(JsonUtils.objectToJson(dataContentMsg))
                    );
                }else{
                    // 离线用户
                }
            }
        }else if (action == MsgActionEnum.PULL_FRIEND.type){
            /** 更新好友列表消息，主动推送给客服端，进行好友更新 **/
            ChatMsgNetty chatMsg = dataContent.getChatMsg();
            String receiverId = chatMsg.getReceiverId();//接收者
            // 获取接收者的channel
            Channel receiverChannel = UserChanelRel.get(receiverId);
            // 用户在线
            if (receiverChannel != null){
                Channel findChanel = ChatHandler.users.find(receiverChannel.id());
                if (findChanel != null){
                    //使用websocket 主动推送消息到请求发起者，更新他的通讯录列表为最新
                    DataContent data = new DataContent();
                    data.setAction(MsgActionEnum.PULL_FRIEND.type);
                    receiverChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(data)));
                }
            }
        }

    }
}
