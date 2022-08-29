package com.iabql.nettyprovider8002.netty;


import java.io.Serializable;

public class DataContent implements Serializable {
    private Integer action;//动作类型
    private ChatMsgNetty chatMsgNetty;//用户的聊天内容
    private String extand;//扩展字段

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public ChatMsgNetty getChatMsg() {
        return chatMsgNetty;
    }

    public void setChatMsg(ChatMsgNetty chatMsgNetty) {
        this.chatMsgNetty = chatMsgNetty;
    }

    public String getExtand() {
        return extand;
    }

    public void setExtand(String extand) {
        this.extand = extand;
    }

    @Override
    public String toString() {
        return "DataContent{" +
                "action=" + action +
                ", chatMsgNetty=" + chatMsgNetty +
                ", extand='" + extand + '\'' +
                '}';
    }
}
