package com.iabql.userprovider8000.utils;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendSms {
    public static boolean send(String phone, String code) {
        try {
            Config config = new Config()
                    // 您的AccessKey ID
                    .setAccessKeyId("LTAI5tLJDTEmVekc3Rq7UXzJ")
                    // 您的AccessKey Secret
                    .setAccessKeySecret("boeDf4p3IaD4SLoEpM4oxRS6Yar9ZE");
            // 访问的域名
            config.endpoint = "dysmsapi.aliyuncs.com";
            Client client = new Client(config);

            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setSignName("阿里云短信测试")//短信签名
                    .setTemplateCode("SMS_154950909")//短信模板
                    .setPhoneNumbers(phone)
                    .setTemplateParam("{code:"+code+"}");
            RuntimeOptions runtime = new RuntimeOptions();
            // 复制代码运行请自行打印 API 的返回值
            client.sendSmsWithOptions(sendSmsRequest, runtime);
        }catch (Exception e){
            log.error("短信发送失败");
            return false;
        }
        return true;
    }

}
