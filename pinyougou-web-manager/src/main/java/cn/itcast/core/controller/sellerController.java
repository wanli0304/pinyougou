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

    @RequestMapping("/search")
    public pageResult search (Integer page, Integer rows,@RequestBody Seller seller) {
        return sellerService.search (page, rows, seller);
    }

    @RequestMapping("/findOne")
    public Seller findOne (String id) {
        return sellerService.findOne (id);
    }

    @RequestMapping("/updateStatus")
    public Result updateStatus (String sellerId ,String status) {
        try {
            sellerService.updateStatus (sellerId,status);
            return new Result (true, "审核通过");
//            if (seller.getStatus ().toCharArray ().equals (1)) {
//                return new Result (true, "审核通过");
//            } else if (seller.getStatus ().toCharArray ().equals (2)) {
//                return new Result (true, "审核未通过");
//            } else {
//                return new Result (true, "关闭商家");
//            }
        } catch (Exception e) {
            return new Result (false, "修改评审失败");
        }
    }
}
