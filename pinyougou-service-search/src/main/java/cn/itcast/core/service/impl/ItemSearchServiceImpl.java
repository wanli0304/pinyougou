package cn.itcast.core.service.impl;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.service.ItemSearchService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 搜索 传入 关键字
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map <String, Object> search (Map <String, String> searchMap) {

        //关键词出来
        searchMap.put ("keywords", searchMap.get ("keywords").replaceAll (" ", ""));
        //调用封装方法 实现 查询关键词高亮 分页 结果集 总条数 总页数
        Map <String, Object> map = searchEnhance (searchMap);
        //商品分类 (品牌 规格)
        List <String> categoryList = searchCategoryByKeyWords (searchMap);
        map.put ("categoryList", categoryList);
        //将品牌 和 规格存入缓存中
        if (null != categoryList && categoryList.size () > 0) {
            //模板ID
            Object typeId = redisTemplate.boundHashOps ("itemCat").get (categoryList.get (0));
            //品牌列表
            List <Map> brandList = (List <Map>) redisTemplate.boundHashOps ("brandList").get (typeId);
            //规格列表
            List <Map> specList = (List <Map>) redisTemplate.boundHashOps ("specList").get (typeId);
            map.put ("specList", specList);
            map.put ("brandList", brandList);
        }
        return map;
    }

    /**
     * 商品分类查询
     *
     * @param searchMap
     * @return
     */
    private List <String> searchCategoryByKeyWords (Map <String, String> searchMap) {
        //关键词
        Criteria criteria = new Criteria ("item_keywords").is (searchMap.get ("keywords"));
        Query query = new SimpleQuery (criteria);

        //分组域
        GroupOptions groupOptions = new GroupOptions ();
        groupOptions.addGroupByField ("item_category");
        query.setGroupOptions (groupOptions);
        List <String> categoryList = new ArrayList <> ();

        //执行查询 分组
        GroupPage <Item> page = solrTemplate.queryForGroupPage (query, Item.class);
        //先获取与对象，再获取groupEntries ,再获取content
        List <GroupEntry <Item>> content = page.getGroupResult ("item_category").getGroupEntries ().getContent ();
//        Page <GroupEntry <Item>> groupEntries = category.getGroupEntries ();
//        List <GroupEntry <Item>> content = groupEntries.getContent ();
        for (GroupEntry <Item> itemGroupEntry : content) {
            categoryList.add (itemGroupEntry.getGroupValue ());
        }

        return categoryList;
    }

    /**
     * 关键字高亮
     * 结果集
     * 总条数
     * 总页数
     *
     * @param searchMap
     * @return
     */
    //定义搜索对象的结构  category:商品分类
    private Map <String, Object> searchEnhance (Map <String, String> searchMap) {


        //关键词
        Criteria criteria = new Criteria ("item_keywords").is (searchMap.get ("keywords"));
        HighlightQuery HighlightQuery = new SimpleHighlightQuery (criteria);
        //条件过滤
        //商品分类
        if (null != searchMap.get ("category") && !"".equals (searchMap.get ("category").trim ())) {
            FilterQuery filterQuery = new SimpleFilterQuery (new Criteria ("item_category").is (searchMap.get ("category").trim ()));
            HighlightQuery.addFilterQuery (filterQuery);
        }

        //品牌
        if (null != searchMap.get ("brand") && !"".equals (searchMap.get ("brand").trim ())) {
            FilterQuery filterQuery = new SimpleFilterQuery (new Criteria ("item_brand").is (searchMap.get ("brand").trim ()));
            HighlightQuery.addFilterQuery (filterQuery);
        }
        //规格
        if (null != searchMap.get ("spec") && !"".equals (searchMap.get ("spec"))) {
            Map <String, String> specMap = JSON.parseObject (searchMap.get ("spec"), Map.class);
            Set <Map.Entry <String, String>> entries = specMap.entrySet ();
            for (Map.Entry <String, String> entry : entries) {
                FilterQuery filterQuery = new SimpleFilterQuery (new Criteria ("item_spec_" + entry.getKey ()).is (entry.getValue ()));
                HighlightQuery.addFilterQuery (filterQuery);
            }
        }
        //价格
        if (null != searchMap.get ("price") && !"".equals (searchMap.get ("price").trim ())) {
            String[] price = searchMap.get ("price").trim ().split ("-");
            FilterQuery filterQuery = null;
            if (searchMap.get ("price").trim ().contains ("*")) {
                filterQuery = new SimpleFilterQuery (new Criteria ("item_price").greaterThanEqual (price[0]));
            } else {
                filterQuery = new SimpleFilterQuery (new Criteria ("item_price").between (price[0], price[1], true, false));
            }
            HighlightQuery.addFilterQuery (filterQuery);
        }

        //排序 3 个条件 价格高低 新品
        //    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
        //获取排序规则
        String sortValue = searchMap.get ("sort");
        //获取排序字段
        String sortField = searchMap.get ("sortField");
        if (null != sortField && !"".equals (sortField)) {
            if ("DESC".equals (sortValue)) {
                HighlightQuery.addSort (new Sort (Sort.Direction.DESC, "item_" + sortField));
            } else {
                HighlightQuery.addSort (new Sort (Sort.Direction.ASC, "item_" + sortField));
            }
        }

        //分页
        String pageNo = searchMap.get ("pageNo");
        String pageSize = searchMap.get ("pageSize");
        //设置偏移量（即solr分页）
        HighlightQuery.setOffset ((Integer.parseInt (pageNo) - 1) * Integer.parseInt (pageSize));
        //每页数
        HighlightQuery.setRows (Integer.parseInt (pageSize));

        //开启高亮
        HighlightOptions highlightOptions = new HighlightOptions ();
        //需要高亮的域名
        highlightOptions.addField ("item_title");
        //前缀
        highlightOptions.setSimplePrefix ("<span style='color:red'>");
        //后缀
        highlightOptions.setSimplePostfix ("</span>");
        HighlightQuery.setHighlightOptions (highlightOptions);
        //执行查询
        HighlightPage <Item> page = solrTemplate.queryForHighlightPage (HighlightQuery, Item.class);
        //将高亮词从docs中取出
        List <HighlightEntry <Item>> highlighted = page.getHighlighted ();
        //遍历对象
        for (HighlightEntry <Item> highlight : highlighted) {

            Item item = highlight.getEntity ();
            List <HighlightEntry.Highlight> highlights = highlight.getHighlights ();
            if (null != highlights && highlights.size () > 0) {
                //获取高亮的名称
                item.setTitle (highlights.get (0).getSnipplets ().get (0));
            }
        }

        //将结果集放入map中
        Map <String, Object> map = new HashMap <> ();
        //结果集
        map.put ("rows", page.getContent ());
        //总条数
        map.put ("total", page.getTotalElements ());
        //总页数
        map.put ("totalPages", page.getTotalPages ());
        return map;
    }
}
