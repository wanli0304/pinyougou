package cn.itcast.core.service.impl;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.pageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TypeTemplateServieImpl implements TypeTemplateService {

    @Autowired
    private TypeTemplateDao typeTemplateDao;

    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 未实现查询功能
     *
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    @Override
    public pageResult search (Integer page, Integer rows, TypeTemplate typeTemplate) {
        List <TypeTemplate> typeTemplates = typeTemplateDao.selectByExample (null);
        //将 模板对象结果集存入缓存
        for (TypeTemplate template : typeTemplates) {
            redisTemplate.boundHashOps ("brandList").put (template.getId (),JSON.parseArray (template.getBrandIds (),Map.class));
            redisTemplate.boundHashOps ("specList").put (template.getId (),findBySpecList (template.getId ()));
        }

        PageHelper.startPage (page, rows);
        PageHelper.orderBy ("id desc");

        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery ();
        TypeTemplateQuery.Criteria criteria = typeTemplateQuery.createCriteria ();
        if (null != typeTemplate.getName () && !"".equals (typeTemplate.getName ().trim ())) {
            criteria.andNameLike ("%" + typeTemplate.getName ().trim () + "%");
        }

        Page <TypeTemplate> p = (Page <TypeTemplate>) typeTemplateDao.selectByExample (typeTemplateQuery);
        return new pageResult (p.getTotal (), p.getResult ());
    }

    @Override
    public void add (TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective (typeTemplate);
    }

    @Override
    public void update (TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective (typeTemplate);
    }

    @Override
    public TypeTemplate findOne (Long id) {
        return typeTemplateDao.selectByPrimaryKey (id);
    }

    @Override
    public void delete (Long[] ids) {
        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery ();
        typeTemplateQuery.createCriteria ().andIdIn (Arrays.asList (ids));
        typeTemplateDao.deleteByExample (typeTemplateQuery);
    }

    @Override
    public List <Map> findBySpecList (Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey (id);
        //根据模板id 获取到 specIds
        String specIds = typeTemplate.getSpecIds ();

        List <Map> mapList = JSON.parseArray (specIds, Map.class);
        for (Map map : mapList) {
               /*
               * map
               *    id :
               *    text:网络
               *    options:list
               * */
            SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery ();
            specificationOptionQuery.createCriteria ().andSpecIdEqualTo ((long) (Integer) map.get ("id")); // 简单类型 intergen String -- long
            List <SpecificationOption> options = specificationOptionDao.selectByExample (specificationOptionQuery);
            map.put ("options", options);

        }

        return mapList;

    }
}
