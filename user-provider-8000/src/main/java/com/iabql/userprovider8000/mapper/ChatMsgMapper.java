package com.iabql.userprovider8000.mapper;


import com.iabql.userprovider8000.pojo.ChatMsg;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatMsgMapper {
    int deleteByPrimaryKey(String id);

    int insert(ChatMsg record);

    int insertSelective(ChatMsg record);

    ChatMsg selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ChatMsg record);

    int updateByPrimaryKey(ChatMsg record);

    List<ChatMsg> getUnReadMsgListByAcceptUid(String acceptUserId);
}