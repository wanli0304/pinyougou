package cn.itcast.core.controller;

import cn.itcast.core.pojo.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("cart")
public class CartController {

    @Reference
    private CartService cartService;

    /**
     * 添加到购物车
     *
     * @param itemId
     * @param num
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("addGoodsToCartList")
    /*
     * CrossOrgin 实现跨域
     */
    @CrossOrigin(origins = {"http://localhost:9003"})
    public Result addGoodsToCartList (Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response) {
        try {
            List <Cart> cartList = null;

            boolean k = false;
            //获取Cookie数据
            Cookie[] cookies = request.getCookies ();
            if (null != cookies && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    //获取Cookie中的购物车
                    if ("CART".equals (cookie.getName ())) {
                        cartList = JSON.parseArray (cookie.getValue (), Cart.class);
                        k = true;
                        //查到要获取的数据 跳出 提高性能
                        break;
                    }
                }
            }
            //没有购物车对象 创建购物车对象
            if (null == cartList) {
                cartList = new ArrayList <> ();
            }
            //追加当前款
            Cart newcart = new Cart ();
            //通过库存Id查询库存对象
            Item item = cartService.findItemById (itemId);
            //商家ID
            newcart.setSellerId (item.getSellerId ());
            //库存ID
            OrderItem newOrderItem = new OrderItem ();
            newOrderItem.setItemId (itemId);
            //数量
            newOrderItem.setNum (num);

            //商品结果集
            List <OrderItem> newOrderItemList = new ArrayList <> ();
            newOrderItemList.add (newOrderItem);
            newcart.setOrderItemList (newOrderItemList);
            //将当前购物车保存到Cookie中

            //判断新购物车的商家是谁，在当前购物车结果中是否已经存在(可以使用if 和 foreach遍历实现)
            // 使用indexOf方法比较购物车
            int newIndexOf = cartList.indexOf (newcart); // -1 存在, >=0存在
            if (newIndexOf != -1){
                //判断购物车中是否有相同商品 ，有追加，没有 添加
                Cart oldCart = cartList.get (newIndexOf);
                List <OrderItem> oldCartOrderItemList = oldCart.getOrderItemList ();
                int indexOf = oldCartOrderItemList.indexOf (newIndexOf);
                if (indexOf!=-1){
                    OrderItem oldOrderItem = oldCartOrderItemList.get (indexOf);
                    oldOrderItem.setNum (oldOrderItem.getNum ()+newOrderItem.getNum ());
                }else {
                    //商家中不存在这个商品 添加这个商品
                    oldCartOrderItemList.add (newOrderItem);
                }
            }else {
                //不存在这个商家，则添加这个商家
                cartList.add (newcart);
            }
            //判断是否登陆
            String name = SecurityContextHolder.getContext ().getAuthentication ().getName ();
            //没有登陆时有一个匿名(这个匿名)
            if (!"anonymousUser".equals (name)){
                //已登录 将当前购物车合并到原来的购物车中
                cartService.merge(cartList,name);
                //清空Cookie
                if(k){
                    Cookie cookie = new Cookie ("CART",null);
                    cookie.setMaxAge (0);
                    cookie.setPath ("/");
                    response.addCookie (cookie);
                }

            }else {
                //未登录，将请求保存到Cookie中
                Cookie cookie = new Cookie ("CART",JSON.toJSONString (cartList));
                cookie.setMaxAge (60*60*24);
                cookie.setPath ("/");
                response.addCookie (cookie);
            }

            return new Result (true, "加入购物车成功");
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "加入购物车失败");
        }

    }

    /**
     * 购物车结果集
     *
     * @return
     */
    @RequestMapping("findCartList")
    public List <Cart> findCartList (HttpServletRequest request,HttpServletResponse response) {
        //跳转购物车页面
        List <Cart> cartList = null;
        boolean k = false;
        //获取Cookie数据
        Cookie[] cookies = request.getCookies ();
        if (null != cookies && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                //获取Cookie中的购物车
                if ("CART".equals (cookie.getName ())) {
                    cartList = JSON.parseArray (cookie.getValue (), Cart.class);
                    k = true;
                    //查到要获取的数据 跳出 提高性能
                    break;
                }
            }
        }
        //判断是否登陆
        String name = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        if (!"anonymousUser".equals (name)){
            //已登录 将购物车合并到原购物车，清空cookie
            if (null!=cartList){
                cartService.merge (cartList,name);
                Cookie cookie = new Cookie ("CART",null);
                cookie.setMaxAge (0);
                cookie.setPath ("/");
                response.addCookie (cookie);

            }
            cartList = cartService.findCartListFromRedis(name);
        }
        if (null!=cartList){
            cartList = cartService.findCartList(cartList);
        }
        return cartList;
    }
}
