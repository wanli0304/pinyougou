package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import entity.pageResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    /**
     * 查询所有商品
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List <Brand> findAll () {
        return brandService.findAll ();
    }

    /**
     * 获取分页信息
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/findByPage")
    public pageResult findPage (Integer pageNum, Integer pageSize) {
        return brandService.findPage (pageNum, pageSize);
    }

    @RequestMapping("/add")
    public Result add (@RequestBody Brand brand) {
        System.out.println ("添加方法执行了");
        try {
            brandService.add (brand);
            return new Result (true, "保存成功");
        } catch (Exception e) {
            return new Result (false, "保存失败");
        }
    }

    @RequestMapping("/update")
    public Result update (@RequestBody Brand brand) {

        try {
            brandService.update (brand);
            return new Result (true, "修改成功");
        } catch (Exception e) {
            return new Result (false, "修改失败");
        }
    }

    @RequestMapping("/findOne")
    public Brand findOne (Long id) {
        return brandService.findOne (id);
    }

    @RequestMapping("/delete")
    public Result dele(Long [] ids){
        try {
            brandService.delete (ids);
            return new Result (true, "成功");
        } catch (Exception e) {
            return new Result (false, "失败");
        }
    }

    @RequestMapping("/search")
    public pageResult search(Integer pageNum,Integer pageSize,@RequestBody Brand brand ){
        return brandService.search(pageNum,pageSize,brand);
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return brandService.selectOptionList();
    }
}
