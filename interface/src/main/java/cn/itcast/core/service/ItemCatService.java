package cn.itcast.core.service;

import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;

public interface ItemCatService {
    List<ItemCat> findByParentId (Long parentId);

    ItemCat findOne (Long id);

    void add (ItemCat itemCat);

    void update (ItemCat itemCat);

    List<ItemCat> findAll ();
}
