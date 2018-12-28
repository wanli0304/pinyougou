package cn.itcast.core.service.impl;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.GoodsService;
import cn.itcast.core.service.StaticPageService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.pageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import pojogroup.GoodsVo;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.*;

@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private GoodsDescDao goodsDescDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemCatDao itemCatDao;

    @Autowired
    private SellerDao sellerDao;

    @Autowired
    private BrandDao brandDao;

    @Override
    public void add (GoodsVo vo) {
        //页面出穿过来的(9个)
        //审核状态
        vo.getGoods ().setAuditStatus ("0");

        //商品表
        goodsDao.insertSelective (vo.getGoods ());
        //商品详情表
        //将上面商品表ID 设置给自己的ID
        vo.getGoodsDesc ().setGoodsId (vo.getGoods ().getId ());
        goodsDescDao.insertSelective (vo.getGoodsDesc ());
        //判断是否启用了规格
        //选中规格 IsEnableSpec 属性 会为 1
        if ("1".equals (vo.getGoods ().getIsEnableSpec ())) {
            //启用规格表
            //向 库存表中 存取数据
            List <Item> itemList = vo.getItemList ();
            //遍历库存
            for (Item item : itemList) {
                //获取标题  商品名称 + 规格1 + 规格2
                String title = vo.getGoods ().getGoodsName ();
                String spec = item.getSpec ();
                Map <String, String> specMap = JSON.parseObject (spec, Map.class);
                //遍历Map
                Set <Map.Entry <String, String>> set = specMap.entrySet ();
                for (Map.Entry <String, String> entry : set) {
                    title += " " + entry.getValue ();
                }
                item.setTitle (title);
                //存图片
                String itemImages = vo.getGoodsDesc ().getItemImages ();
                List <Map> images = JSON.parseArray (itemImages, Map.class);
                //判断图片是否存在
                if (null != images && images.size () > 0) {
                    item.setImage ((String) images.get (0).get ("url"));
                }
                //存商品分类 3级ID
                item.setCategoryid (vo.getGoods ().getCategory3Id ());
                //商品分类 3级名称
                ItemCat itemCat = itemCatDao.selectByPrimaryKey (vo.getGoods ().getCategory3Id ());
                item.setCategory (itemCat.getName ());
                //添加时间
                item.setCreateTime (new Date ());
                //修改时间
                item.setUpdateTime (new Date ());
                //商品表的ID，本表的外键
                item.setGoodsId (vo.getGoods ().getId ());
                //商家ID
                item.setSellerId (vo.getGoods ().getSellerId ());
                //商家公司名称
                Seller seller = sellerDao.selectByPrimaryKey (vo.getGoods ().getSellerId ());
                item.setSeller (seller.getName ());
                //品牌名称
                Brand brand = brandDao.selectByPrimaryKey (vo.getGoods ().getBrandId ());
                item.setBrand (brand.getName ());

                //保存
                itemDao.insertSelective (item);
            }
        }
    }

    @Override
    public pageResult search (Integer page, Integer rows, Goods goods) {
        PageHelper.startPage (page, rows);
        PageHelper.orderBy ("id desc");

        GoodsQuery goodsQuery = new GoodsQuery ();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria ();

        //判断
        if (null != goods.getAuditStatus () && !"".equals (goods.getAuditStatus ())) {
            criteria.andAuditStatusEqualTo (goods.getAuditStatus ());
        }

        if (null != goods.getGoodsName () && !"".equals (goods.getGoodsName ().trim ())) {
            criteria.andGoodsNameLike ("%" + goods.getGoodsName ().trim () + "%");
        }

        //判断如果是商家 则查询当前 商家 的商品 否则是 运营商 查全部
        if (null != goods.getSellerId ()) {
            //只查当前用户商品
            criteria.andSellerIdEqualTo (goods.getSellerId ());
        }
        //只查询 不删除
        criteria.andIsDeleteIsNull ();
        Page <Goods> p = (Page <Goods>) goodsDao.selectByExample (goodsQuery);
        return new pageResult (p.getTotal (), p.getResult ());
    }

    @Override
    public GoodsVo findOne (Long id) {
        GoodsVo vo = new GoodsVo ();
        //商品表
        vo.setGoods (goodsDao.selectByPrimaryKey (id));
        //商品详情
        vo.setGoodsDesc (goodsDescDao.selectByPrimaryKey (id));
        //库存表
        ItemQuery itemQuery = new ItemQuery ();
        itemQuery.createCriteria ().andGoodsIdEqualTo (id);
        vo.setItemList (itemDao.selectByExample (itemQuery));
        return vo;
    }

    @Override
    public void update (GoodsVo vo) {
        //商品表
        goodsDao.updateByPrimaryKeySelective (vo.getGoods ());
        //商品详情表
        goodsDescDao.updateByPrimaryKeySelective (vo.getGoodsDesc ());

        //库存 先删后插
        ItemQuery itemQuery = new ItemQuery ();
        itemQuery.createCriteria ().andGoodsIdEqualTo (vo.getGoods ().getId ());
        itemDao.deleteByExample (itemQuery);
        //添加
        //判断是否启用了规格
        //选中规格 IsEnableSpec 属性 会为 1
        if ("1".equals (vo.getGoods ().getIsEnableSpec ())) {
            //启用规格表
            //向 库存表中 存取数据
            List <Item> itemList = vo.getItemList ();
            //遍历库存
            for (Item item : itemList) {
                //获取标题  商品名称 + 规格1 + 规格2
                String title = vo.getGoods ().getGoodsName ();
                String spec = item.getSpec ();
                Map <String, String> specMap = JSON.parseObject (spec, Map.class);
                //遍历Map
                Set <Map.Entry <String, String>> set = specMap.entrySet ();
                for (Map.Entry <String, String> entry : set) {
                    title += " " + entry.getValue ();
                }
                item.setTitle (title);
                //存图片
                String itemImages = vo.getGoodsDesc ().getItemImages ();
                List <Map> images = JSON.parseArray (itemImages, Map.class);
                //判断图片是否存在
                if (null != images && images.size () > 0) {
                    item.setImage ((String) images.get (0).get ("url"));
                }
                //存商品分类 3级ID
                item.setCategoryid (vo.getGoods ().getCategory3Id ());
                //商品分类 3级名称
                ItemCat itemCat = itemCatDao.selectByPrimaryKey (vo.getGoods ().getCategory3Id ());
                item.setCategory (itemCat.getName ());
                //添加时间
                item.setCreateTime (new Date ());
                //修改时间
                item.setUpdateTime (new Date ());
                //商品表的ID，本表的外键
                item.setGoodsId (vo.getGoods ().getId ());
                //商家ID
                item.setSellerId (vo.getGoods ().getSellerId ());
                //商家公司名称
                Seller seller = sellerDao.selectByPrimaryKey (vo.getGoods ().getSellerId ());
                item.setSeller (seller.getName ());
                //品牌名称
                Brand brand = brandDao.selectByPrimaryKey (vo.getGoods ().getBrandId ());
                item.setBrand (brand.getName ());

                //保存
                itemDao.insertSelective (item);
            }
        }
    }


    @Autowired
    private JmsTemplate jmsTemplate;

    //发布者
    @Autowired
    private Destination topicPageAndSolrDestination;

    //使用activeMQ实现索引导入 和 生成静态页面
    @Override
    public void updateStatus (Long[] ids, String status) {
        Goods goods = new Goods ();
        goods.setAuditStatus (status);
        for (Long id : ids) {
            goods.setId (id);
            goodsDao.updateByPrimaryKeySelective (goods);
            //发布订阅者，发布审核通过的商品ID
            if ("1".equals (status)) {
                jmsTemplate.send (topicPageAndSolrDestination, new MessageCreator () {
                    @Override
                    public Message createMessage (Session session) throws JMSException {
                        return session.createTextMessage (String.valueOf (id));
                    }
                });

            }
        }
    }
    //点对点
    @Autowired
    private Destination queueSolrDeleteDestination;

    @Override
    public void delete (Long[] ids) {
        Goods goods = new Goods ();
        goods.setIsDelete ("1");
        for (Long id : ids) {
            goods.setId (id);
            goodsDao.updateByPrimaryKeySelective (goods);
            //发消息
            jmsTemplate.send (queueSolrDeleteDestination, new MessageCreator () {
                @Override
                public Message createMessage (Session session) throws JMSException {
                    return session.createTextMessage (String.valueOf (id));
                }
            });
        }
    }

}
