package com.iabql.nettyprovider8001.netty;

import com.alibaba.fastjson.JSONObject;
import com.iabql.nettyprovider8001.SpringUtil;
import com.iabql.nettyprovider8001.enums.MsgActionEnum;
import com.iabql.nettyprovider8001.kafka.KafkaProducer;
import com.iabql.nettyprovider8001.redis.RedisUtils;
import com.iabql.nettyprovider8001.services.UserServices;
import com.iabql.nettyprovider8001.utils.JsonUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;


/**
 * 用于处理消息的handler
 * 由于它的传输数据的载体是frame，这个frame 在netty中，是用于为websocket专门处理文本对象的，frame是消息的载体，此类叫：TextWebSocketFrame
 */
@Slf4j
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private String ip_port = "localhost_8001";

    // 用于记录和管理所有客户端的channel
    public static ChannelGroup users = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 获取客户端所传输的消息
        String content = msg.text();
        content.replaceAll("\r","\\r").replaceAll("\n","\\n");
        // 1.获取客户端发来的消息
        DataContent dataContent = JSONObject.parseObject(content, DataContent.class);
        Integer action = dataContent.getAction();
        Channel channel =  ctx.channel();
        /*
         * 2.判断消息类型，根据不同的类型来处理不同的业务
         * 2.1 当websocket 第一次open的时候，初始化channel，把用的channel 和 userid 关联起来
         * 2.2 聊天类型的消息，把聊天记录保存到数据库，同时标记消息的签收状态[未签收]
         * 2.3 签收消息类型，针对具体的消息进行签收，修改数据库中对应消息的签收状态[已签收]
         * 2.4 心跳类型消息
         */
        if(action == MsgActionEnum.CONNECT.type){

            /** 2.1 当websocket 第一次open的时候，初始化channel，把用户的channel 和 userid 关联起来 **/
            String senderId = dataContent.getChatMsg().getSenderId();
            // 本地存储
            UserChanelRel.put(senderId,channel);
            // redis存储，key：userId，value：IP:port
            RedisUtils redisUtils = (RedisUtils) SpringUtil.getBean("redisUtils");
            redisUtils.save(senderId, ip_port);

        } else if(action == MsgActionEnum.CHAT.type){
            /** 2.2 聊天类型的消息，把聊天记录保存到数据库，同时标记消息的签收状态[未签收] **/
            ChatMsgNetty chatMsg = dataContent.getChatMsg();
            String msgContent = chatMsg.getMsg();//消息内容
            String senderId = chatMsg.getSenderId();//发送者
            String receiverId = chatMsg.getReceiverId();//接收者
            // 保存消息到数据库，并且标记为未签收
            UserServices userServices = (UserServices) SpringUtil.getBean("userServicesIml");
            String msgId = userServices.saveMsg(chatMsg);// 保存并返回消息在数据库中的id值
            chatMsg.setMsgId(msgId);

            // 将要推送给client的消息
            DataContent dataContentMsg = new DataContent();
            dataContentMsg.setChatMsg(chatMsg);
            dataContentMsg.setAction(MsgActionEnum.CHAT.type);

            /*发送消息给接收者*/

            // 从本地关系获取接收者的channel
            Channel receiverChannel = UserChanelRel.get(receiverId);
            if(receiverChannel ==null){
                // 如果本地为空，就去redis查询，（如果存在，说明client连接在其他server）
                RedisUtils redisUtils = (RedisUtils) SpringUtil.getBean("redisUtils");
                String topic_ip_port = redisUtils.get(receiverId);
                if(topic_ip_port == null){
                    // 用户不在线
                }else {
                    // 用户在线，将消息发送至对应netty服务器的消息队列
                    KafkaProducer kafkaProducer = (KafkaProducer) SpringUtil.getBean("kafkaProducer");
                    kafkaProducer.send(topic_ip_port,dataContentMsg);
                }
            }else{
                // 当receiverChannel 不为空的时候，从ChannelGroup 去查找对应的channel 是否存在
                // 因为在client断开时，只清理了ChannelGroup中的channel，没有移除Map中的
                Channel findChanel = users.find(receiverChannel.id());
                if(findChanel!=null){
                    // 用户在线，发送消息
                    receiverChannel.writeAndFlush(
                            new TextWebSocketFrame(
                                    JsonUtils.objectToJson(dataContentMsg)
                            )
                    );
                }else{
                    // 离线用户
                }
            }


        } else if(action == MsgActionEnum.SIGNED.type){
            /** 2.3 签收消息类型，针对具体的消息进行签收，修改数据库中对应消息的签收状态[已签收] **/
            UserServices userServices = (UserServices) SpringUtil.getBean("userServicesIml");
            // 前端将需要签收的消息id用逗号间隔组成字符串，放在扩展字段signed中
            String msgIdsStr = dataContent.getExtand();
            String[] msgsId = msgIdsStr.split(",");

            List<String> msgIdList = new ArrayList<>();
            for (String mid: msgsId) {
                if(StringUtils.isNotBlank(mid)){
                    msgIdList.add(mid);
                }
            }

            if(msgIdList!=null && !msgIdList.isEmpty() && msgIdList.size()>0){
                // 批量签收
                userServices.updateMsgSigned(msgIdList);
            }

        } else if(action == MsgActionEnum.KEEPALIVE.type){
            // 2.4 心跳类型的消息
            log.info("收到来自channel 为【"+channel+"】的心跳包");
        }

    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        users.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        UserChanelRel.remove(ctx.channel());
        users.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生了异常后关闭连接，同时从channelgroup移除
        ctx.channel().close();
        UserChanelRel.remove(ctx.channel());
        users.remove(ctx.channel());
    }
}
