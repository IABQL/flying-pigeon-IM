package com.iabql.nettyprovider8002.services;

import com.iabql.nettyprovider8002.netty.ChatMsgNetty;

import java.util.List;

public interface UserServices {

    //保存用户聊天消息
    String saveMsg(ChatMsgNetty chatMsg);

    //签收消息
    void updateMsgSigned(List<String> msgIdList);

}
