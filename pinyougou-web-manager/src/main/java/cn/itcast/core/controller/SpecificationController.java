package cn.itcast.core.controller;

import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import entity.pageResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojogroup.SpecificationVo;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("specification")
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;

    @RequestMapping("/findAll")
    public List <Specification> specificationList () {

        return specificationService.findAll ();
    }

    /**
     * 分页 被弃用的
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/findByPage")
    public pageResult findPage (Integer pageNum, Integer pageSize) {
        return specificationService.findPage (pageNum, pageSize);
    }

    /**
     * 模糊查询 及分页
     *
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @RequestMapping("/search")
    public pageResult search (Integer page, Integer rows, @RequestBody Specification specification) {
        return specificationService.search (page, rows, specification);
    }

    @RequestMapping("/add")
    public Result add (@RequestBody SpecificationVo specificationVo) {
        try {
            specificationService.add (specificationVo);
            return new Result (true, "成功");
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "失败");
        }

    }

    @RequestMapping("/findOne")
    public SpecificationVo findOne (Long id) {
        return specificationService.findOne (id);
    }

    @RequestMapping("/update")
    public Result update (@RequestBody SpecificationVo specificationVo) {
        try {
            specificationService.update (specificationVo);
            return new Result (true, "成功");
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "失败");
        }
    }

    @RequestMapping("/delete")
    public Result delete (Long[] ids) {

        try {
            specificationService.delete (ids);
            return new Result (true, "成功");
        } catch (Exception e) {
            return new Result (false, "失败");
        }
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return specificationService.selectOptionList();
    }
}
