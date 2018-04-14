package cn.myjo.queryInventory.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import cn.myjo.queryInventory.util.AuthService;
import cn.myjo.queryInventory.util.HttpUtil;

/**
 * 商品信息的service
 * @author 麦子
 *
 */
public class ItemServiceImpl implements ItemService {

	@Override
	public String getArticleJson(String imageUrl) {
		//得到货号json信息
		String articleNumberJson = getArticleNumber(imageUrl);
		//处理json取得货号
		JsonElement parse = new JsonParser().parse(articleNumberJson);
		JsonArray articleNumberArray= parse.getAsJsonObject().get("words_result").getAsJsonArray();
		String articleNumber=null;
		try {
			int worldLength=articleNumberJson.split("words").length-2;
			articleNumber= articleNumberArray.get(worldLength-2).getAsJsonObject().get("words").getAsString();
			articleNumber=articleNumber.substring(articleNumber.indexOf(":")+1);
		}catch(Exception e) {
			throw new RuntimeException("图片内没有可识别的内容，请检查图中是否有信息。");
		}
		String htmlText=null;
		Document doc;
		try {
		 htmlText = getHtmlText(articleNumber);
		doc = Jsoup.parse(htmlText);
		}catch(Exception e) {
			AccessMethodService am = new AccessMethodService();
			//登录超时重新执行模拟登陆
			am.sendRequestAndGetResponses();
			htmlText = getHtmlText(articleNumber);
			doc = Jsoup.parse(htmlText);
		}
		if (htmlText.contains("登录超时")) {
			AccessMethodService am = new AccessMethodService();
			//登录超时重新执行模拟登陆
			am.sendRequestAndGetResponses();
			htmlText = getHtmlText(articleNumber);
			doc = Jsoup.parse(htmlText);
		}
		
		System.out.println(htmlText);
		//爬取固定不变的表格
		Element fieldset=null;
		String jsonText=null;
		try {
			fieldset = doc.getElementsByTag("fieldset").get(0);
			jsonText=htmlText.substring(htmlText.lastIndexOf("parseJSON(")+11,htmlText.indexOf("var row_c")-3 );
			jsonText=jsonText.substring(0, jsonText.lastIndexOf("');"));
		}catch(IndexOutOfBoundsException e) {
			throw new RuntimeException("商品信息未查找到。请刷新当前页面 或 核实天马后台信息。货号："+articleNumber+".");
		}
		
		String tableEle = fieldset.getElementsByTag("table").get(0).toString();
		tableEle = tableEle.replaceFirst("<td .*</td>", " ").replaceFirst("<td .*</td>", " ");
		String jsonTextSub=null;
		String chiMa=null;
		
		
		if(htmlText.contains("size_info = '")) {
			jsonTextSub= htmlText.substring(htmlText.indexOf("size_info = '")+13);
			chiMa= jsonTextSub.substring(0,jsonTextSub.indexOf("'."));
			//System.out.println(chiMa);
		}
		
		//解析成json数据
		JsonParser jsonParse=new JsonParser();
		//JsonElement tableJsonEle = jsonParse.parse(tableJson);
		Map<String,String> map=new HashMap<>();
		map.put("table",tableEle);
		map.put("size_info",chiMa);
		String tableJson = new Gson().toJson(map);
		JsonElement tableJsonEle = jsonParse.parse(tableJson);

		JsonElement jsonTextParse = jsonParse.parse(jsonText);
		JsonArray jsonArray = new JsonArray();
		jsonArray.add(jsonTextParse);
		jsonArray.add(tableJsonEle);
		//System.out.println("json:::::::"+jsonArray.toString());
		//System.out.println(tableEle.toString());
		return jsonArray.toString();
	}
	/**
	 * 调用百度接口进行货号图片的识别
	 * @param imageUrl 图片的url地址
	 * @return	返回一个json字符串
	 */
	private String getArticleNumber(String imageUrl) {
		 // 请求url
       String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
       try {
           // 本地文件路径
           String filePath = imageUrl;
           String param = "url=" + filePath;
           // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
           String accessToken = new AuthService().getAuth();
           String result = HttpUtil.post(url, accessToken, param);
           return result;
       } catch (Exception e) {
           e.printStackTrace();
       }
       return null;
	}
	/**
	 *  获取输入货号后的页面信息
	 * @param articleno 货号
	 * @return 货号信息
	 * @throws Exception
	 */
	private String getHtmlText(String articleno){
		FileReader fr=null;
		BufferedReader in=null; 
		CloseableHttpClient httpClient=null;
		try {
			String path=this.getClass().getResource("/").getPath();
			path=path.substring(1,path.indexOf("WEB-INF"))+"cookie.txt";
			// 读取cookie
			File myFile = new File(path);
			fr = new FileReader(myFile);
			in = new BufferedReader(fr);
			String[] cookie=null;
			try {
				String str=in.readLine();
				cookie = str.split("=");
			}catch(Exception e) {
				AccessMethodService am = new AccessMethodService();
				//登录超时重新执行模拟登陆
				am.sendRequestAndGetResponses();
				String str=in.readLine();
				cookie = str.split("=");
			}
			CookieStore cookieStore = new BasicCookieStore();
			
			BasicClientCookie cookies = new BasicClientCookie(cookie[0], cookie[1]);
			cookies.setVersion(0);
			cookies.setDomain("www.tianmasport.com");
			cookies.setPath("/ms");
			cookieStore.addCookie(cookies);
			
			httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
			
			// 爬取快速下单页面内容
			List<NameValuePair> searchForm = new ArrayList<NameValuePair>();
			searchForm.add(new BasicNameValuePair("articleno", articleno));
			UrlEncodedFormEntity articlenoEntity = new UrlEncodedFormEntity(searchForm);
	
			HttpPost quick = new HttpPost("http://www.tianmasport.com/ms/order/searchByArticleno.do");
			quick.setEntity(articlenoEntity);
			CloseableHttpResponse quickResponse = httpClient.execute(quick);
			String quickInfo = EntityUtils.toString(quickResponse.getEntity());
			
			return quickInfo;
		}catch(Exception e) {
			throw new RuntimeException(e);
		}finally {
			try {
				httpClient.close();
				in.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
