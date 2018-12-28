package cn.itcast.core.service.impl;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.pageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import pojogroup.SpecificationVo;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private SpecificationDao specificationDao;

    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    @Override
    public List <Specification> findAll () {
        return specificationDao.selectByExample (null);
    }

    @Override
    public pageResult findPage (Integer page, Integer rows) {
        PageHelper.startPage (page, rows);

        Page <Specification> p = (Page <Specification>) specificationDao.selectByExample (null);

        return new pageResult (p.getTotal (), p.getResult ());
    }

    @Override
    public void add (SpecificationVo specificationVo) {
        //规格表 返回 ID
        specificationDao.insertSelective (specificationVo.getSpecification ());
        //规格表选项结果集
        List <SpecificationOption> optionList = specificationVo.getSpecificationOptionList ();
        for (SpecificationOption option : optionList) {
            //外键
            option.setSpecId (specificationVo.getSpecification ().getId ());
            //保存
            specificationOptionDao.insertSelective (option);
        }
    }

    @Override
    public void update (SpecificationVo specificationVo) {
        //规格修改
        specificationDao.updateByPrimaryKeySelective (specificationVo.getSpecification ());
        //先删除
        SpecificationOptionQuery query = new SpecificationOptionQuery ();
        query.createCriteria ().andSpecIdEqualTo (specificationVo.getSpecification ().getId ());
        specificationOptionDao.deleteByExample (query);
        //再增加
        List <SpecificationOption> optionList = specificationVo.getSpecificationOptionList ();
        for (SpecificationOption option : optionList) {
            //外键
            option.setSpecId (specificationVo.getSpecification ().getId ());
            //保存
            specificationOptionDao.insertSelective (option);
        }
    }

    @Override
    public SpecificationVo findOne (Long id) {
        SpecificationVo vo = new SpecificationVo ();
        //规格对象
        Specification specification = specificationDao.selectByPrimaryKey (id);
        vo.setSpecification (specification);
        //规格对象结果集
        SpecificationOptionQuery query = new SpecificationOptionQuery ();
        query.createCriteria ().andSpecIdEqualTo (id);
        //可以进行排序
        query.setOrderByClause ("orders desc");

        //获得关联键上的值
        List <SpecificationOption> list = specificationOptionDao.selectByExample (query);
        vo.setSpecificationOptionList (list);
        //返回SpecificationVo 对象
        return vo;
    }

    @Override
    public void delete (Long[] ids) {
        for (Long id : ids) {
            //删除规格
            specificationDao.deleteByPrimaryKey (id);

            SpecificationOptionQuery query = new SpecificationOptionQuery ();
            query.createCriteria ().andSpecIdEqualTo (id);
            specificationOptionDao.deleteByExample (query);
        }
    }

    @Override
    public List <Map> selectOptionList () {
        return specificationDao.selectOptionList();
    }

    @Override
    public pageResult search (Integer page, Integer rows, Specification specification) {
        PageHelper.startPage (page, rows);
        SpecificationQuery specificationQuery = new SpecificationQuery ();
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria ();

        if (null != specification.getSpecName () && !"".equals (specification.getSpecName ().trim ())) {
            criteria.andSpecNameLike ("%" + specification.getSpecName ().trim () + "%");
        }

        Page <Specification> p = (Page <Specification>) specificationDao.selectByExample (specificationQuery);
        return new pageResult (p.getTotal (), p.getResult ());
    }

}
