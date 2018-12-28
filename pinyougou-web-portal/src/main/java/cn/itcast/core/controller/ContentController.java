package cn.itcast.core.controller;

import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 广告管理
 * @author lx
 *
 */
@RestController
@RequestMapping("/content")
public class ContentController {

	@Reference
	private ContentService contentService;

	//通过广告分类ID查询  广告结果集
	@RequestMapping("/findByCategoryId")
	public List <Content> findByCategoryId (Long categoryId) {
		return contentService.findByCategoryId (categoryId);
	}
}
	
