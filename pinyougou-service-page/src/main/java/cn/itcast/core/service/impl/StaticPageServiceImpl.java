package cn.itcast.core.service.impl;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.service.StaticPageService;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletContext;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 静态化处理实现类
 */
@Service
public class StaticPageServiceImpl implements StaticPageService, ServletContextAware {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private GoodsDescDao goodsDescDao;

    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemCatDao itemCatDao;

    @Override
    public void index (Long id) {
        //输出路径
        String allPath = getPath ("/" + id + ".html");

        //创建Freemarket
        Configuration conf = freeMarkerConfigurer.getConfiguration ();

        //数据 (存入map)
        Map <String, Object> map = new HashMap <> ();

        //商品详情表
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey (id);
        map.put ("goodsDesc", goodsDesc);

        //库春详情表
        ItemQuery itemQuery = new ItemQuery ();
        itemQuery.createCriteria ().andGoodsIdEqualTo (id).andStatusEqualTo ("1");
        List <Item> itemList = itemDao.selectByExample (itemQuery);
        map.put ("itemList", itemList);

        //商品对象
        Goods goods = goodsDao.selectByPrimaryKey (id);
        map.put ("goods", goods);
        //三级分类
        map.put ("itemCat1", itemCatDao.selectByPrimaryKey (goods.getCategory1Id ()).getName ());
        map.put ("itemCat2", itemCatDao.selectByPrimaryKey (goods.getCategory2Id ()).getName ());
        map.put ("itemCat3", itemCatDao.selectByPrimaryKey (goods.getCategory3Id ()).getName ());
        //输出成页面
        Writer out = null;

        try {
            Template template = conf.getTemplate ("item.ftl");
            //输出(设置编码)
            out = new OutputStreamWriter (new FileOutputStream (allPath), "UTF-8");
            //处理
            template.process (map, out);
            System.out.println ("生成静态页面");
        } catch (Exception e) {
            e.printStackTrace ();
        } finally {

            try {
                if (null != out) {
                    out.close ();
                }
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }

    }

    //输出路径获取
    private String getPath (String path) {
        return servletContext.getRealPath (path);
    }

    private ServletContext servletContext;

    @Override
    public void setServletContext (ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
