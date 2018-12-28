package cn.itcast.core.controller;

import cn.itcast.core.service.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("pay")

public class PayController {

    @Reference
    private PayService payService;

    //获取生成的二维码value地址
    @RequestMapping("createNative")
    public Map <String, String> createNative () {
        //获取当前登陆的用户名
        String userId = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        return payService.createNative (userId);
    }

    //根据订单号 查看支付状态
    @RequestMapping("queryPayStatus")
    public Result queryPayStatus (String out_trade_no) {
        try {
            int x = 100;
            while (true) {
                Map <String, String> map = payService.queryPayStatus (out_trade_no);
                //判断交易状态
                if ("SUCCESS".equals (map.get ("trade_state"))) {
                    //SUCCESS—支付成功
                    System.out.println (map.get ("trade_state"));
                    System.out.println ();
                    return new Result (true, "支付成功");
                }
                if ("NOTPAY".equals (map.get ("trade_state")) ||
                        "CLOSED".equals (map.get ("trade_state"))
                        || "REVOKED".equals (map.get ("trade_state"))
                        || "USERPAYING".equals (map.get ("trade_state"))
                        || "PAYERROR".equals (map.get ("trade_state"))) {

                    Thread.sleep (10000);
                    x++;
                    if (x > 100) {
                        //五分钟
                        //调用微信那边关闭订单Api (同学完成了)
                        return new Result (false, "二维码超时");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "支付失败");
        }
    }

}


