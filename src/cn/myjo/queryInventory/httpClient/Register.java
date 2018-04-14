package cn.myjo.queryInventory.httpClient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


public class Register {
	HttpClient httpClient = null;
	String url = null;
	static String verifyCodeName="verifyCode";
	String verifyCode="tessdata\\"+verifyCodeName+".jpg";
	public Register() {
		url = "http://www.tianmasport.com/ms/login.shtml";
		HttpClient httpClient = HttpClientBuilder.create().build();
	}

	/**
	 * 获得一个验证码
	 * 
	 * @return 验证码内的文本信息
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public boolean getVerifyCode() {
		try{
			String url = "http://www.tianmasport.com/ms/ImageServlet?time=new%20Date().getTime()";
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			HttpResponse response;
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			FileOutputStream output=null;
			if (entity != null) {
				output= new FileOutputStream(verifyCode);
				InputStream instream = entity.getContent();
				byte buf[] = new byte[1024];
				int j = 0;
				while ((j = instream.read(buf)) != -1) {
					output.write(buf, 0, j);
				}				
				
				while ((j = instream.read(buf)) != -1) {
					output.write(buf, 0, j);
				}				
			}else{
				return false;
			}
			return true;
		}catch(Exception e){
			return false;
		}
	}
	/**
	 * 进行登录操作
	 * @param url post请求进入的URL
	 * @param verifyCode
	 *            传入的验证码
	 * @throws UnsupportedEncodingException 
	 */
		
		 public static boolean goRegister(String url,String verifyCode) throws UnsupportedEncodingException{
			 CloseableHttpClient httpClient = HttpClients.createDefault();
			 // 根据地址发送post请求 
			 HttpPost request = new HttpPost(url);
			 //设置添加post数据 List<NameValuePair> 
			 ArrayList<NameValuePair> postData = new ArrayList<NameValuePair>(); 
			 postData.add(new BasicNameValuePair("nickName","青岛麦巨商贸")); 
			 postData.add(new BasicNameValuePair("pwd","852456mj")); 
			 postData.add(new BasicNameValuePair("verifyCode",verifyCode)); 
			 postData.add(new BasicNameValuePair("remember","on"));
			 request.setEntity(new UrlEncodedFormEntity(postData));
			 CloseableHttpResponse response=null;
			 try {
				response = httpClient.execute(request);
				HttpEntity entity = response.getEntity();
				System.out.println(EntityUtils.toString(entity));
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			  // 通过请求对象获取响应对象 HttpResponse response = httpClient.execute(request);
			  //判断网络连接状态码是否正常(0--200都数正常) 
	}
		 
	/*public static void register(){
		Register register = new Register();
		boolean getVerifyCodeFlag = register.getVerifyCode();
		if(getVerifyCodeFlag){
			TesseractExample tess=new TesseractExample();
			String verificationCode=null;
			try {
				verificationCode = tess.verificationCode(verifyCodeName);
				if(goRegister("http://www.tianmasport.com/ms/beLogin.do",verificationCode)){
					
				}
			} catch (UnsupportedEncodingException e) {
				System.out.println("验证码图片解析失败。");
				register();
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		register();
	}*/
	
}
