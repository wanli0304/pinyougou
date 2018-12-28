package cn.itcast.core.service.impl;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Service;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private ItemCatDao itemCatDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<ItemCat> findByParentId (Long parentId) {
        List <ItemCat> itemCats = findAll ();
        //将所有商品分类添加到缓存中
        for (ItemCat itemCat : itemCats) {
            redisTemplate.boundHashOps ("itemCat").put (itemCat.getName (),itemCat.getTypeId ());
        }

        ItemCatQuery itemCatQuery = new ItemCatQuery ();
        itemCatQuery.createCriteria ().andParentIdEqualTo (parentId);
        return itemCatDao.selectByExample (itemCatQuery);
    }

    @Override
    public ItemCat findOne (Long id) {
        ItemCat itemCat = itemCatDao.selectByPrimaryKey (id);
        return itemCat;
    }

    @Override
    public void add (ItemCat itemCat) {
        itemCatDao.insertSelective (itemCat);
    }

    @Override
    public void update (ItemCat itemCat) {
        itemCatDao.updateByPrimaryKey (itemCat);
    }

    @Override
    public List <ItemCat> findAll () {
        return itemCatDao.selectByExample (null);
    }
}
