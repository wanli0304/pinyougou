package cn.itcast.core.controller;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import entity.pageResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class sellerController {

    @Reference
    private SellerService sellerService;

    @RequestMapping("/add")
    public Result add (@RequestBody Seller seller){
        try {
            sellerService.add(seller);
            return new Result(true, "申请入驻成功,审核中!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "申请入驻失败!");
        }
    }
}

