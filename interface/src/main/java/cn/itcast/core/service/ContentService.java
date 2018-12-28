package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.Content;
import entity.pageResult;

import java.util.List;

public interface ContentService {

	public List<Content> findAll();

    public pageResult findPage(Content content, Integer pageNum, Integer pageSize);
	
	public void add(Content content);
	
	public void edit(Content content);
	
	public Content findOne(Long id);
	
	public void delAll(Long[] ids);

	List<Content> findByCategoryId (Long categoryId);
}
