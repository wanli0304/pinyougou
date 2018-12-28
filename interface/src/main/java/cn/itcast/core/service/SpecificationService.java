package cn.itcast.core.service;

import cn.itcast.core.pojo.specification.Specification;
import entity.pageResult;
import pojogroup.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {

    List<Specification> findAll ();

    pageResult findPage (Integer pageNum, Integer pageSize);

    pageResult search (Integer page, Integer rows, Specification specification);

    void add (SpecificationVo specificationVo);

    void update (SpecificationVo specificationVo);

    SpecificationVo findOne (Long id);

    void delete (Long[] ids);

    List<Map> selectOptionList ();
}
