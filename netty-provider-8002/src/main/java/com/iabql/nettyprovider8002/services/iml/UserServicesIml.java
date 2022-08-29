package com.iabql.nettyprovider8002.services.iml;

import com.iabql.nettyprovider8002.enums.MsgSignFlagEnum;
import com.iabql.nettyprovider8002.idworker.Sid;
import com.iabql.nettyprovider8002.mapper.ChatMsgMapper;
import com.iabql.nettyprovider8002.mapper.UserMapperCustom;
import com.iabql.nettyprovider8002.netty.ChatMsgNetty;
import com.iabql.nettyprovider8002.pojo.ChatMsg;
import com.iabql.nettyprovider8002.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserServicesIml implements UserServices {

    @Autowired
    private Sid sid;

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Autowired
    private UserMapperCustom userMapperCustom;


    @Override
    public String saveMsg(ChatMsgNetty chatMsg) {
        ChatMsg msgDB = new ChatMsg();
        String msgId = sid.nextShort();//生成消息唯一id
        msgDB.setId(msgId);
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(chatMsg.getMsg());

        chatMsgMapper.insert(msgDB);

        return msgId;
    }

    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        userMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

}
