package cn.myjo.queryInventory.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.aip.ocr.AipOcr;
import com.crawler.domain.Sample;

/**
 * 发送请求并获得响应的方法
 * @author 麦子
 *
 */
public class AccessMethodService {

	private String[] finalCookie;
	private String nickName;
	private String pwd;
	private String remember;

	// 发送请求并获取响应（cookie+验证码图片）
	public Map<String, String> sendRequestAndGetResponse() throws Exception {

		AccessMethodService am = new AccessMethodService();
		// 生成HttpClient对象
		CloseableHttpClient httpclient = HttpClients.createDefault();
		// 创建一个GET对象
		String codeUrl = "http://www.tianmasport.com/ms/ImageServlet?time=new%20Date().getTime()";
		HttpGet get = new HttpGet(codeUrl);
		// 执行请求
		CloseableHttpResponse response = httpclient.execute(get);
		// 取得响应结果
		Header[] headers = response.getAllHeaders();
		Map<String, String> map = new HashMap<String, String>();
		for (Header h : headers) {
			map.put(h.getName(), h.getValue());
		}
		String cookie = map.get("Set-Cookie");

		// 生成图片
		int width = 65;
		int height = 30;
		String path=this.getClass().getResource("/").getPath();
		path=path.substring(1,path.indexOf("WEB-INF"))+"valcodeImg/valcode.jpg";
		System.out.println("图片路径:"+path);
		File file = new File(path);
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		ImageIO.write(image, "jpg", file);
		OutputStream os = null;
		// 写入图片
		os = new FileOutputStream(file);
		response.getEntity().writeTo(os);
		ImageIO.write(image, "jpg", os);
		os.close();
		String vcode = am.getVcode();
		Map<String, String> cookieAndVcode = new HashMap<String, String>();
		cookieAndVcode.put("cookie", cookie);
		cookieAndVcode.put("vcode", vcode);

		return cookieAndVcode;
	}

	// 解析验证码图片
	public String getVcode() throws UnsupportedEncodingException, JSONException {
		Sample s = new Sample();
		AipOcr client = new AipOcr(s.getAppId(), s.getApiKey(), s.getSecretKey());

		String path=this.getClass().getResource("/").getPath();
		path=path.substring(1,path.indexOf("WEB-INF"))+"valcodeImg/valcode.jpg";
		
		System.out.println(path);
		String image =path;
		JSONObject res = client.basicAccurateGeneral(image, null);
		JSONArray json = res.getJSONArray("words_result");
		String vcode = (String) json.getJSONObject(0).get("words");
		System.out.println("v:"+vcode);
		return vcode;
	}

	// 发送带参请求获取权限
	public void sendRequestAndGetResponses() {
		CloseableHttpClient httpclient=null;
		FileWriter fw=null;
		try {
			AccessMethodService am = new AccessMethodService();
			am.getData();
			Map<String, String> cookieAndVcode = am.sendRequestAndGetResponse();
			String cookie = cookieAndVcode.get("cookie");
			String vcode = cookieAndVcode.get("vcode").trim();
			
			String[] str = cookie.split(";");
			finalCookie = str[0].split("=");
			CookieStore cookieStore = new BasicCookieStore();
			BasicClientCookie cookies = new BasicClientCookie(finalCookie[0], finalCookie[1]);
			cookies.setVersion(0);
			cookies.setDomain("www.tianmasport.com");
			cookies.setPath("/ms");
			cookieStore.addCookie(cookies);
			httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
	
			List<NameValuePair> form = new ArrayList<NameValuePair>();
			form.add(new BasicNameValuePair("nickName", nickName));
			form.add(new BasicNameValuePair("pwd", pwd));
			form.add(new BasicNameValuePair("verifyCode", vcode));
			form.add(new BasicNameValuePair("remember", remember));
	
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, "UTF-8");
			HttpPost login = new HttpPost("http://www.tianmasport.com/ms/beLogin.do");
			login.setEntity(entity);
			login.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36");
			login.setHeader("Referer", "http://www.tianmasport.com/ms/login.shtml");
			CloseableHttpResponse loginResponse = httpclient.execute(login);
			String loginInfo = EntityUtils.toString(loginResponse.getEntity());
			//System.out.println(loginInfo);
			// 进入主页，不然无权限
			HttpGet main = new HttpGet("http://www.tianmasport.com/ms/main.shtml");
			CloseableHttpResponse mainResponse = httpclient.execute(main);
			String htmlText = EntityUtils.toString(mainResponse.getEntity());
	
			// System.out.println(loginInfo.indexOf("验证码输入错误"));
			if (loginInfo.indexOf("false") >= 0) {
				Thread.sleep(3000);
				am.sendRequestAndGetResponses();
			} else {
				// 保存cookie到文件中
				String path=this.getClass().getResource("/").getPath();
				path=path.substring(1,path.indexOf("WEB-INF"))+"cookie.txt";
				
				
				fw = new FileWriter(path);
				fw.write(finalCookie[0] + "=" + finalCookie[1]);
				fw.flush();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
			if(httpclient!=null) {
				httpclient.close();
			}
			if(fw!=null) {
				fw.close();
			}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void getData() throws Exception {
		String path=this.getClass().getResource("/").getPath();
		path=path.substring(1,path.indexOf("classes"))+"FormData.properties";
		FileInputStream fs = new FileInputStream(path);
		Properties pro = new Properties();
		pro.load(new InputStreamReader(fs, "utf-8"));// 直接用pro.load(fs)会出现输出内容中文乱码
		fs.close();
		nickName = pro.getProperty("nickName");
		pwd = pro.getProperty("pwd");
		remember = pro.getProperty("remember");
	}
}
