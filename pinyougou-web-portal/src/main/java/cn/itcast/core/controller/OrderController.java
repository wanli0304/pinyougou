package cn.itcast.core.controller;

import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.service.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
public class OrderController {
    @Reference
    private OrderService orderService;

    @RequestMapping("add")
    public Result add (@RequestBody Order order) {
        try {
            //获取当前订单用户id
            String userId = SecurityContextHolder.getContext ().getAuthentication ().getName ();
            order.setUserId (userId);
            orderService.add(order);
            return new Result (true,"保存订单成功");
        }catch (Exception e){
            e.printStackTrace ();
            return new Result (false,"保存订单失败");
        }
    }
}
