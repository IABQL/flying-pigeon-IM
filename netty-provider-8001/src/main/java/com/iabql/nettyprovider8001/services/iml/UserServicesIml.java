package com.iabql.nettyprovider8001.services.iml;

import com.iabql.nettyprovider8001.enums.MsgSignFlagEnum;
import com.iabql.nettyprovider8001.idworker.Sid;
import com.iabql.nettyprovider8001.mapper.*;
import com.iabql.nettyprovider8001.netty.ChatMsgNetty;
import com.iabql.nettyprovider8001.pojo.ChatMsg;
import com.iabql.nettyprovider8001.services.UserServices;
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
