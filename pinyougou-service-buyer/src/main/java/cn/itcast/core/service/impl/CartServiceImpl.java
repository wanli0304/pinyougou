package cn.itcast.core.service.impl;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.CartService;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlRenameTableStatement;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemDao itemDao;

    @Override
    public Item findItemById (Long itemId) {

        return itemDao.selectByPrimaryKey (itemId);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void merge (List <Cart> newCartList, String name) {
        //获取缓存中购物车结果集
        List <Cart> oldCartList = (List <Cart>) redisTemplate.boundHashOps ("CART").get (name);
        //合并两个购物车
        oldCartList = merge1 (newCartList, oldCartList);
        //将购物车存入缓存中
        redisTemplate.boundHashOps ("CART").put (name, oldCartList);
    }

    @Override
    public List <Cart> findCartListFromRedis (String name) {
        return (List <Cart>) redisTemplate.boundHashOps ("CART").get (name);
    }

    @Override
    public List <Cart> findCartList (List <Cart> cartList) {
        for (Cart cart : cartList) {
            //库存
            Item item = null;
            //商家名称
            List <OrderItem> orderItemList = cart.getOrderItemList ();
            for (OrderItem orderItem : orderItemList) {
                //查询库存
                item = findItemById (orderItem.getItemId ());
                //图片
                orderItem.setPicPath (item.getImage ());
                //标题
                orderItem.setTitle (item.getTitle ());
                //单价
                orderItem.setPrice (item.getPrice ());
                //小计
                orderItem.setTotalFee (new BigDecimal (orderItem.getPrice ().doubleValue () * orderItem.getNum ()));
            }
            //商家名称
            cart.setSellerName (item.getSeller ());
        }

        return cartList;
    }

    //合并购物车
    private List <Cart> merge1 (List <Cart> newCartList, List <Cart> oldCartList) {
        //判断新购物车是否有值
        if (null != newCartList && newCartList.size () > 0) {
            //判断旧购物车是否有值
            if (null != oldCartList && oldCartList.size () > 0) {
                //新购物车结果集
                for (Cart newCart : newCartList) {
                    //判断新购物车商家，老购物车当中是否有这个商家
                    int newIndexOf = oldCartList.indexOf (newCart);
                    if (newIndexOf != -1) {
                        //从老购物车结果集中找出那个跟新购物车是同一个商家的老购物车
                        Cart oldCart = oldCartList.get (newIndexOf);
                        //判断新购物车中，新商品，在老购物车中商品结果集是否存在
                        List <OrderItem> oldOrderItemList = oldCart.getOrderItemList ();
                        //新购物车中有的商品集合
                        List <OrderItem> newOrderItemList = newCart.getOrderItemList ();
                        for (OrderItem newOrderItem : newOrderItemList) {
                            int indexOf = oldOrderItemList.indexOf (newOrderItem);
                            if (indexOf != -1) {
                                //存在 追加数量
                                OrderItem oldOrderItem = oldOrderItemList.get (indexOf);
                                oldOrderItem.setNum (oldOrderItem.getNum () + newOrderItem.getNum ());

                            } else {
                                oldOrderItemList.add (newOrderItem);
                            }
                        }
                    } else {
                        oldCartList.add (newCart);
                    }
                }
            } else {
                return newCartList;
            }
        }
        return oldCartList;
    }
}

