package com.iabql.nettyprovider8001.mapper;


import com.iabql.nettyprovider8001.pojo.ChatMsg;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ChatMsgMapper {

    int insert(ChatMsg record);
}