package pojogroup;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;

import java.io.Serializable;
import java.util.List;

/*
* 包装对象
* 商品对象
* 商品详情对象
* 库存对象结果集
*/
public class GoodsVo implements Serializable {
    //商品对象
    private Goods goods;
    //商品对象详细对象(详细描述)
    private GoodsDesc goodsDesc;
    //库存对象
    private List<Item> itemList;

    public Goods getGoods () {
        return goods;
    }

    public void setGoods (Goods goods) {
        this.goods = goods;
    }

    public GoodsDesc getGoodsDesc () {
        return goodsDesc;
    }

    public void setGoodsDesc (GoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List <Item> getItemList () {
        return itemList;
    }

    public void setItemList (List <Item> itemList) {
        this.itemList = itemList;
    }
}
