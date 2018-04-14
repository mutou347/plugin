package cn.myjo.queryInventory.service;

public interface ItemService {
	/*
	 * 得到解析好的货物信息
	 * @param imageUrl 图片的地址
	 * @return
	 */
	String getArticleJson(String imageUrl);
}
