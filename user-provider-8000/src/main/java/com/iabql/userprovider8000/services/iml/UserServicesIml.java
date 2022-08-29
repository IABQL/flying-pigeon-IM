package com.iabql.userprovider8000.services.iml;

import com.iabql.userprovider8000.enums.MsgActionEnum;
import com.iabql.userprovider8000.enums.MsgSignFlagEnum;
import com.iabql.userprovider8000.enums.SearchFriendsStatusEnum;
import com.iabql.userprovider8000.idworker.Sid;
import com.iabql.userprovider8000.kafka.KafkaProducer;
import com.iabql.userprovider8000.mapper.*;
import com.iabql.userprovider8000.netty.ChatMsgNetty;
import com.iabql.userprovider8000.netty.DataContent;
import com.iabql.userprovider8000.pojo.ChatMsg;
import com.iabql.userprovider8000.pojo.FriendsRequest;
import com.iabql.userprovider8000.pojo.MyFriends;
import com.iabql.userprovider8000.pojo.User;
import com.iabql.userprovider8000.services.UserServices;
import com.iabql.userprovider8000.utils.*;
import com.iabql.userprovider8000.vo.FriendsRequestVO;
import com.iabql.userprovider8000.vo.MyFriendsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class UserServicesIml implements UserServices {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private MyFriendsMapper myFriendsMapper;

    @Autowired
    private FriendsRequestMapper friendsRequestMapper;

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Autowired
    private UserMapperCustom userMapperCustom;

    @Autowired
    private KafkaProducer kafkaProducer;

    @Resource
    private RedisTemplate redisTemplate;

    @Value("${head-img-url}")
    private String headImgUrl;

    @Override
    public User getUserById(String id) {
        return userMapper.selectByPrimaryKey(id);
    }

    @Override
    public User queryUserNameIsExit(String username) {
        return userMapper.queryUserNameIsExit(username);
    }

    // 添加用户信息
    @Override
    public User insert(User user) {
        String id = sid.nextShort();// 为用户生成唯一主键ID
        user.setId(id);

        // 为每个注册用户生成一个唯一的二维码
        String filename = id+"qrcode.png";
        //String qrCodePath="/usr/local/qrcode/"+id+"qrcode.png";todo
        String qrCodePath="D:\\"+ filename;
        // 创建二维码对象信息，扫描后的结果如：“ bird_qrcode:账号 ”
        QRCodeUtils.createQRCode(qrCodePath,"bird_qrcode:"+user.getUsername());
        // 生成二维码图片
        MultipartFile qrcodeFile = FileUtils.fileToMultipart(qrCodePath);
        String qrCodeURL ="";
        try {
            // 上传二维码
            qrCodeURL = UpLoadUtils.upLoadImg(qrcodeFile,"qr_img/",filename);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            // 删除本地二维码图片,/usr/local/qrcode/todo
            File file = new File("D:\\"+ filename);
            if (file != null)
                file.delete();
        }

        // 随机选择一个作为默认头像
        Random random = new Random();
        int i = random.nextInt(4);
        String headImg = headImgUrl + i + ".png";

        user.setNickname(id.substring(0,7));
        user.setQrcode(qrCodeURL);
        user.setPassword(MD5Utils.getPwd(user.getPassword()));
        user.setFaceImage(headImg);
        user.setFaceImageBig(headImg);
        userMapper.insert(user);//保存
        return user;
    }

    // 修改用户信息
    @Override
    public User updateUserInfo(User user) {
        userMapper.updateByPrimaryKeySelective(user);
        User result = userMapper.selectByPrimaryKey(user.getId());
        return result;
    }

    // 搜索好友
    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUserName) {
        //查询用户
        User user = queryUserNameIsExit(friendUserName);
        //1.搜索的用户如果不存在，则返回【无此用户】
        if(user==null){
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        //2.搜索的账号如果是你自己，则返回【不能添加自己】
        if(myUserId.equals(user.getId())){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        //3.搜索的朋友已经是你好友，返回【该用户已经是你的好友】
        MyFriends myfriend = new MyFriends();
        myfriend.setMyUserId(myUserId);
        myfriend.setMyFriendUserId(user.getId());
        MyFriends myF = myFriendsMapper.selectOneByExample(myfriend);
        if(myF!=null){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SearchFriendsStatusEnum.SUCCESS.status;
    }

    //发送好友请求
    @Override
    public void sendFriendRequest(String myUserId, String friendUserName) {
        User user = queryUserNameIsExit(friendUserName);
        MyFriends myFriend = new MyFriends();
        myFriend.setMyUserId(myUserId);
        myFriend.setMyFriendUserId(user.getId());
        MyFriends myF = myFriendsMapper.selectOneByExample(myFriend);
        if(myF == null){
            FriendsRequest friendsRequest = new FriendsRequest();
            String requestId = sid.nextShort();
            friendsRequest.setId(requestId);
            friendsRequest.setSendUserId(myUserId);//发送者
            friendsRequest.setAcceptUserId(user.getId());// 接收者
            friendsRequest.setRequestDateTime(new Date());// 发送日期
            // 好友请求写入请求表
            friendsRequestMapper.insert(friendsRequest);
        }
    }

    @Override
    public List<FriendsRequestVO> queryFriendRequestList(String acceptUserId) {
        return userMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Override
    public void deleteFriendRequest(FriendsRequest friendsRequest) {
        friendsRequestMapper.deleteByFriendRequest(friendsRequest);
    }

    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        //进行双向好友数据保存
        saveFriends(sendUserId,acceptUserId);
        saveFriends(acceptUserId,sendUserId);

        //删除好友请求表中的数据
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setSendUserId(sendUserId);
        friendsRequest.setAcceptUserId(acceptUserId);
        deleteFriendRequest(friendsRequest);

        //将好友请求通过消息推送给client，发送给kafka，交给netty处理
        /*Channel sendChannel  = UserChanelRel.get(sendUserId);
        if(sendChannel!=null){
            //使用websocket 主动推送消息到请求发起者，更新他的通讯录列表为最新
            DataContent dataContent = new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);

            //消息的推送
            sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }*/
        DataContent dataContent = new DataContent();
        dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
        ChatMsgNetty chatMsg = new ChatMsgNetty();
        chatMsg.setReceiverId(sendUserId);
        dataContent.setChatMsg(chatMsg);

        Object o = redisTemplate.opsForValue().get(sendUserId);
        if(o != null){
            // client在线才进行发送
            kafkaProducer.send(o.toString(),dataContent);
        }

    }

    //通过好友请求并保存数据到my_friends 表中
    private void saveFriends(String sendUserId, String acceptUserId){
        MyFriends myFriends = new MyFriends();
        String recordId = sid.nextShort();

        myFriends.setId(recordId);
        myFriends.setMyUserId(sendUserId);
        myFriends.setMyFriendUserId(acceptUserId);

        myFriendsMapper.insert(myFriends);
    }

    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) {
        return userMapperCustom.queryMyFriends(userId);
    }

    @Override
    public List<ChatMsg> getUnReadMsgList(String acceptUserId) {
        List<ChatMsg> result = chatMsgMapper.getUnReadMsgListByAcceptUid(acceptUserId);
        return result;
    }
}
