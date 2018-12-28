package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import entity.pageResult;
import org.apache.ibatis.annotations.Results;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojogroup.GoodsVo;


@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    @RequestMapping("add")
    public Result add(@RequestBody GoodsVo vo){
        try {
            //获取商家id
            String sellerId = SecurityContextHolder.getContext ().getAuthentication ().getName ();
            vo.getGoods ().setSellerId (sellerId);
            goodsService.add(vo);
            return new Result (true,"成功");
        }catch (Exception e){
            return new Result (false,"失败");
        }
    }

    @RequestMapping("search")
    public pageResult search(Integer page, Integer rows , @RequestBody Goods goods){
        //获取当前商家ID
        String sellerId = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        goods.setSellerId (sellerId);
        return goodsService .search(page,rows,goods);
    }

    @RequestMapping("findOne")
    public GoodsVo findOne(Long id){
        return goodsService.findOne(id);
    }

    @RequestMapping("update")
    public Result update(@RequestBody GoodsVo vo){
        try {
            goodsService.update(vo);
            return new Result (true,"成功");
        }catch (Exception e){
            return new Result (false,"失败");

        }

    }

    @RequestMapping("updateStatus")
    public Result updateStatus(Long[] ids,String status){

        try {
            goodsService.updateStatus(ids,status);
            return new Result (true,"审核成功");
        }catch (Exception e){
            e.printStackTrace ();
            return new Result (false,"审核失败");
        }

    }

}
