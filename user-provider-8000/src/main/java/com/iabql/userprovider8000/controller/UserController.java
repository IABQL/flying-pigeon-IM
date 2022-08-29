package com.iabql.userprovider8000.controller;

import com.iabql.userprovider8000.bo.CodeBO;
import com.iabql.userprovider8000.bo.UserBO;
import com.iabql.userprovider8000.bo.UserRegister;
import com.iabql.userprovider8000.enums.OperatorFriendRequestTypeEnum;
import com.iabql.userprovider8000.enums.SearchFriendsStatusEnum;
import com.iabql.userprovider8000.pojo.ChatMsg;
import com.iabql.userprovider8000.pojo.FriendsRequest;
import com.iabql.userprovider8000.pojo.User;
import com.iabql.userprovider8000.services.UserServices;
import com.iabql.userprovider8000.utils.*;
import com.iabql.userprovider8000.vo.FriendsRequestVO;
import com.iabql.userprovider8000.vo.MyFriendsVO;
import com.iabql.userprovider8000.vo.UserVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController implements IMConstant {

    @Autowired
    private UserServices userServices;

    @Resource
    private RedisTemplate<String,String> redisTemplate;


    /**
     *  用户登入
     *  @Author WuXingCong
     */
    @RequestMapping("/login")
    public IWdzlJSONResult login(User user) {

        User userResult = userServices.queryUserNameIsExit(user.getUsername());

        if(userResult != null){// 登入
            if (!userResult.getPassword().equals(MD5Utils.getPwd(user.getPassword()))) {
                return IWdzlJSONResult.errorMap("密码不正确");
            }
        }else {
            return IWdzlJSONResult.errorMap("账号不存在");
        }
        UserVo userVo = new UserVo();// 视图vo
        BeanUtils.copyProperties(userResult,userVo);// 将数据库查询到的数据赋值到userVo，除了密码等敏感信息
        return IWdzlJSONResult.ok(userVo);
    }

    /**
     * 注册
     * @Author WuXingCong
     */
    @RequestMapping("/register")
    public IWdzlJSONResult register(UserRegister userRegister) {
        /*
         * 1.判断用户是否已存在
         * 2.判断验证码是否正确,redis中存储的code键为：register_code用户账号
         * 3.注册
         */

        User exit = userServices.queryUserNameIsExit(userRegister.getUsername());
        if (exit != null){
            return IWdzlJSONResult.errorMap("该用户已存在");
        }
        String code = redisTemplate.opsForValue().get("register_code" + userRegister.getUsername());

        if(code == null || !code.equals(userRegister.getCode())){
            // 验证失败
            return IWdzlJSONResult.errorMap("验证码错误");
        }

        User user = new User();
        user.setUsername(userRegister.getUsername());
        user.setPassword(userRegister.getPassword());
        userServices.insert(user);

        return IWdzlJSONResult.ok("注册成功");
    }

    /**
     *  发送验证码
     *  @Author WuXingCong
     */
    @RequestMapping("/sendCode")
    public IWdzlJSONResult sendCode(CodeBO codeBO) {
        if(codeBO.getUsername() == null){
            return IWdzlJSONResult.ok("发送失败");
        }
        //生成随机验证码
        String code = IMUtils.generateUUID().substring(2, 8);
        //存入redis
        redisTemplate.opsForValue().set(codeBO.getType() + codeBO.getUsername()
                ,code,CODE_REGISTER_EXPIRED_SECONDS, TimeUnit.SECONDS);

        // 发送验证码
        boolean send = SendSms.send(codeBO.getUsername(), code);

        if (!send)  return IWdzlJSONResult.ok("发送失败");
        return IWdzlJSONResult.ok("发送成功");
    }

    /**
     * 设置昵称
     * @Author WuXingCong
     */
    @RequestMapping("/setNickname")
    public IWdzlJSONResult setNickName(User user) {
        User userResult = userServices.updateUserInfo(user);
        return IWdzlJSONResult.ok(userResult);
    }

    /**
     * 上传头像
     * @Author WuXingCong
     */
    @RequestMapping("/uploadFaceBase64")
    public IWdzlJSONResult uploadFaceBase64(@RequestBody UserBO userBO) throws Exception {
        System.out.println(userBO);

        // 获取前端传过来的base64的字符串，然后转为文件对象在进行上传
        String base64Data = userBO.getFaceData();
        String userFacePath = "/usr/local/face/"+userBO.getUserId()+"userFaceBase64.png";
        // 调用FileUtils 类中的方法将base64 字符串转为文件对象
        FileUtils.base64ToFile(userFacePath, base64Data);
        MultipartFile multipartFile = FileUtils.fileToMultipart(userFacePath);
        // 获取fastDFS上传图片的路径
        // String url = fastDFSClient.uploadBase64(multipartFile);

        // 更新用户头像
        User user = new User();
        user.setId(userBO.getUserId());
        // user.setFaceImageBig(url);
        User result = userServices.updateUserInfo(user);

        return  IWdzlJSONResult.ok(result);
    }

    /**
     * 搜索好友的请求方法
     * @Author WuXingCong
     */
    @RequestMapping("/searchFriend")
    public IWdzlJSONResult searchFriend(String myUserId,String friendUserName){
        /*
         * 前置条件：
         * 1.搜索的用户如果不存在，则返回【无此用户】
         * 2.搜索的账号如果是你自己，则返回【不能添加自己】
         * 3.搜索的朋友已经是你好友，返回【该用户已经是你的好友】
         */

        Integer status = userServices.preconditionSearchFriends(myUserId,friendUserName);
        if(status== SearchFriendsStatusEnum.SUCCESS.status){
            User user = userServices.queryUserNameIsExit(friendUserName);
            UserVo userVo = new UserVo();
            BeanUtils.copyProperties(user,userVo);
            return IWdzlJSONResult.ok(userVo);
        }else{
            String msg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IWdzlJSONResult.errorMsg(msg);
        }
    }

    /**
     * 发送添加好友请求
     * @Author WuXingCong
     */
    @RequestMapping("/addFriendRequest")
    public IWdzlJSONResult addFriendRequest(String myUserId,String friendUserName){
        if(StringUtils.isBlank(myUserId)|| StringUtils.isBlank(friendUserName)){
            return IWdzlJSONResult.errorMsg("好友信息为空");
        }

        /*
         * 前置条件：
         * 1.搜索的用户如果不存在，则返回【无此用户】
         * 2.搜索的账号如果是你自己，则返回【不能添加自己】
         * 3.搜索的朋友已经是你好友，返回【该用户已经是你的好友】
         */
        Integer status = userServices.preconditionSearchFriends(myUserId,friendUserName);
        if(status==SearchFriendsStatusEnum.SUCCESS.status){
            userServices.sendFriendRequest(myUserId,friendUserName);
        }else{
            String msg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IWdzlJSONResult.errorMsg(msg);
        }
        return IWdzlJSONResult.ok();
    }

    /**
     * 好友请求列表查询
     * @Author WuXingCong
     */
    @RequestMapping("/queryFriendRequest")
    public IWdzlJSONResult queryFriendRequest(String userId){
        List<FriendsRequestVO> friendRequestList = userServices.queryFriendRequestList(userId);
        return IWdzlJSONResult.ok(friendRequestList);
    }

    /**
     *  好友请求处理映射my_friends
     * @Author WuXingCong
     */
    @RequestMapping("/operFriendRequest")
    public IWdzlJSONResult operFriendRequest(String acceptUserId,String sendUserId,Integer operType){
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setAcceptUserId(acceptUserId);
        friendsRequest.setSendUserId(sendUserId);
        // 0：忽略， 1：通过
        if(operType== OperatorFriendRequestTypeEnum.IGNORE.type){
            //满足此条件将需要对好友请求表中的数据进行删除操作
            userServices.deleteFriendRequest(friendsRequest);
        }else if(operType==OperatorFriendRequestTypeEnum.PASS.type){
            //满足此条件表示需要向好友表中添加一条记录，同时删除好友请求表中对应的记录
            userServices.passFriendRequest(sendUserId,acceptUserId);
        }
        //查询好友表中的列表数据
        List<MyFriendsVO> myFriends = userServices.queryMyFriends(acceptUserId);
        return IWdzlJSONResult.ok(myFriends);
    }

    /**
     * 好友列表查询
     * @Author WuXingCong
     */
    @RequestMapping("/myFriends")
    public IWdzlJSONResult myFriends(String userId){
        if (StringUtils.isBlank(userId)){
            return IWdzlJSONResult.errorMsg("用户id为空");
        }
        //数据库查询好友列表
        List<MyFriendsVO> myFriends = userServices.queryMyFriends(userId);
        return IWdzlJSONResult.ok(myFriends);
    }

    /**
     * 用户手机端获取未签收的消息列表
     * @Author WuXingCong
     */
    @RequestMapping("/getUnReadMsgList")
    public IWdzlJSONResult getUnReadMsgList(String acceptUserId){
        if(StringUtils.isBlank(acceptUserId)){
            return IWdzlJSONResult.errorMsg("接收者ID不能为空");
        }
        //根据接收ID查找为签收的消息列表
        List<ChatMsg> unReadMsgList = userServices.getUnReadMsgList(acceptUserId);
        return IWdzlJSONResult.ok(unReadMsgList);
    }

}
