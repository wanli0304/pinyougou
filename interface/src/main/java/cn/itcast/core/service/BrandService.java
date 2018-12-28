package cn.itcast.core.service;

import cn.itcast.core.pojo.good.Brand;
import entity.pageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {
    List<Brand> findAll();

    pageResult findPage (Integer pageNum, Integer pageSize);

    void add (Brand brand);

    void update (Brand brand);

    Brand findOne (Long id);

    void delete (Long[] ids);

    pageResult search (Integer pageNum, Integer pageSize, Brand brand);

    List<Map> selectOptionList ();
}
