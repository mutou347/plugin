package cn.myjo.queryInventory.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.myjo.queryInventory.service.AccessMethodService;
import cn.myjo.queryInventory.service.ItemService;
import cn.myjo.queryInventory.service.ItemServiceImpl;
/**
 * 查找货号的servlet
 * @author 麦子
 *
 */
public class InvertoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Access-Control-Allow-Origin", "*"); 
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE"); 
		response.setHeader("Access-Control-Max-Age", "3600"); 
		response.setCharacterEncoding("utf-8");
		String imageUrl=request.getParameter("imageUrl");
		try {
			ItemService itemService=new ItemServiceImpl();
				//得到货号信息
			String text = itemService.getArticleJson(imageUrl);    //获得页面信息
			PrintWriter pw=response.getWriter();
			pw.println(text);		//将页面输出到前端页面
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
