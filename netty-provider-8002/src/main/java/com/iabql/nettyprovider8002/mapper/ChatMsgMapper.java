package com.iabql.nettyprovider8002.mapper;


import com.iabql.nettyprovider8002.pojo.ChatMsg;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ChatMsgMapper {

    int insert(ChatMsg record);
}