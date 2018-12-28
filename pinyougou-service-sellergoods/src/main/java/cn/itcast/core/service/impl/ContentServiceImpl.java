package cn.itcast.core.service.impl;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.pageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentDao contentDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List <Content> findAll () {
        List <Content> list = contentDao.selectByExample (null);
        return list;
    }

    @Override
    public pageResult findPage (Content content, Integer pageNum, Integer pageSize) {
        PageHelper.startPage (pageNum, pageSize);
        Page <Content> page = (Page <Content>) contentDao.selectByExample (null);
        return new pageResult (page.getTotal (), page.getResult ());
    }

    @Override
    public void add (Content content) {
        contentDao.insertSelective (content);
        redisTemplate.boundHashOps ("content").delete (content.getCategoryId ());
    }

    @Override
    public void edit (Content content) {
        //先根据id查询原来的广告
        Content c = contentDao.selectByPrimaryKey (content.getId ());
        //修改数据库
        contentDao.updateByPrimaryKeySelective (content);
        //判断原来广告的分类ID是否相同
        if (!content.getCategoryId ().equals (c.getCategoryId ())){
            //不同则清除原来的广告分类ID
            redisTemplate.boundHashOps ("content").delete (c.getCategoryId ());
        }
        //清除现在的广告分类的ID
        redisTemplate.boundHashOps ("content").delete (content.getCategoryId ());
    }

    @Override
    public Content findOne (Long id) {
        Content content = contentDao.selectByPrimaryKey (id);
        return content;
    }

    @Override
    public void delAll (Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                Content content = contentDao.selectByPrimaryKey (id);
                contentDao.deleteByPrimaryKey (id);
                redisTemplate.boundHashOps ("content").delete (content.getCategoryId ());
            }
        }
    }

    @Override
    public List <Content> findByCategoryId (Long categoryId) {
        //先查询redis缓存中是否存在
        List <Content> contentList = (List <Content>) redisTemplate.boundHashOps ("content").get (categoryId);
        if (null == contentList || contentList.size () == 0) {
            //查询数据库
            ContentQuery query = new ContentQuery ();
            query.createCriteria ().andCategoryIdEqualTo (categoryId).andStatusEqualTo ("1");
            query.setOrderByClause ("sort_order desc");
            contentList = contentDao.selectByExample (query);
            //保存到缓存中
            redisTemplate.boundHashOps ("content").put (categoryId, contentList);
            redisTemplate.boundHashOps ("content").expire (24, TimeUnit.HOURS);
        }
        return contentList;
    }

}
