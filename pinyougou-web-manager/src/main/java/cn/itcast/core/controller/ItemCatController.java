package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {
    @Reference
    private ItemCatService itemCatService;

    @RequestMapping("/findByParentId")
    public List <ItemCat> findByParentId (Long parentId) {
        return itemCatService.findByParentId (parentId);
    }

    @RequestMapping("/findOne")
    public ItemCat findOne (Long id) {
        return itemCatService.findOne (id);
    }

    @RequestMapping("/add")
    public Result add (@RequestBody ItemCat itemCat) {
        try {
            itemCatService.add (itemCat);
            return new Result (true, "成功");
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "失败");
        }
    }

    @RequestMapping("/update")
    public Result update (@RequestBody ItemCat itemCat) {
        try {
            itemCatService.update (itemCat);
            return new Result (true, "成功");
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "失败");
        }
    }

    @RequestMapping("findAll")
    public List <ItemCat> findAll () {
        return itemCatService.findAll ();
    }
}
