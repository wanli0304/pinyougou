package cn.itcast.core.service.impl;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.pageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandDao brandDao;

    @Override
    public List<Brand> findAll () {
        return brandDao.selectByExample (null);
    }


    @Override
    public pageResult findPage (Integer pageNum, Integer pageSize) {
        PageHelper.startPage (pageNum,pageSize);
        // 使用 page 方法实现 方法掉调用
        Page <Brand> page = (Page <Brand>) brandDao.selectByExample (null);
        return new pageResult (page.getTotal (),page.getResult ());
    }

    @Override
    public void add (Brand brand) {
        brandDao.insertSelective (brand);
    }

    @Override
    public void update (Brand brand) {
        brandDao.updateByPrimaryKeySelective (brand);
    }

    @Override
    public Brand findOne (Long id) {
        return brandDao.selectByPrimaryKey (id);
    }

    @Override
    public void delete (Long[] ids) {
        BrandQuery brandQuery = new BrandQuery ();
        brandQuery.createCriteria ().andIdIn (Arrays.asList (ids));
        brandDao.deleteByExample (brandQuery);
    }

    @Override
    public pageResult search (Integer pageNum, Integer pageSize, Brand brand) {
        PageHelper.startPage (pageNum,pageSize);
        BrandQuery brandQuery = new BrandQuery ();
        BrandQuery.Criteria criteria = brandQuery.createCriteria ();

        //判断品牌名称 不为null 不为 "  "
        if (null != brand.getName () && !"".equals (brand.getName ().trim ())){
            criteria.andNameLike ("%"+brand.getName ().trim ()+"%");
        }
        //判断品牌首字母
        if (null != brand.getFirstChar () && !"".equals (brand.getFirstChar ().trim ())){
            criteria.andFirstCharLike ("%"+brand.getFirstChar ().toUpperCase ()+"%");
        }
        //查询
        Page <Brand> p = (Page <Brand>) brandDao.selectByExample (brandQuery);

        return new pageResult (p.getTotal (),p.getResult ());
    }

    @Override
    public List <Map> selectOptionList () {
       return brandDao.selectOptionList();
    }
}
