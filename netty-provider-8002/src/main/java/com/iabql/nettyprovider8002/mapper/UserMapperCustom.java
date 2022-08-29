package com.iabql.nettyprovider8002.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapperCustom {

    void batchUpdateMsgSigned(List<String> msgIdList);

}
