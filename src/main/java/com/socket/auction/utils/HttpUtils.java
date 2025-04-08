package com.socket.auction.utils;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HttpUtils { 	
	private Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	
    public HashMap<String, Object> sendPostQuery(String url, String query) {
    	HttpClient httpClient = HttpClientBuilder.create().build();
    	HashMap<String, Object> result = new HashMap<String, Object>();

    	try {
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
			
			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			paramList.add(new BasicNameValuePair("query", query));
			
			post.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));

			HttpResponse response = httpClient.execute(post);
		    HttpEntity respEntity = response.getEntity();
		    if(response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
		    	result.put("resultCd", response.getStatusLine().getStatusCode());
		    	result.put("data", EntityUtils.toString(respEntity));
		    }else {
		    	result.put("resultCd", response.getStatusLine().getStatusCode());
		    	result.put("data", EntityUtils.toString(respEntity));
		    }
		    
		} catch (Exception e) {
			logger.info("error : " +e.getMessage());
		}
		return result;
	}
	
    public HashMap<String, Object> sendPostJson(String url, String postData) {
    	HttpClient httpClient = HttpClientBuilder.create().build();
    	HashMap<String, Object> result = new HashMap<String, Object>();

    	try {
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-Type", "application/json");
			
			StringEntity stringEntity = new StringEntity(postData, "UTF-8");
			
			post.setEntity(stringEntity);
			HttpResponse response = httpClient.execute(post);
			
		    HttpEntity respEntity = response.getEntity();
		    
		    if(response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
		    	result.put("resultCd", response.getStatusLine().getStatusCode());
		    	result.put("data", EntityUtils.toString(respEntity));
		    }else {
		    	result.put("resultCd", response.getStatusLine().getStatusCode());
		    	result.put("data", EntityUtils.toString(respEntity));
		    }
		} catch (Exception e) {
			logger.info("error : " +e.getMessage());
		}
		return result;
	}    
	
    public HashMap<String, Object> sendPostJson(String url, String jsnAuth, String postData) {
    	HttpClient httpClient = HttpClientBuilder.create().build();
    	HashMap<String, Object> result = new HashMap<String, Object>();

    	try {
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-Type", "application/json");
			post.setHeader("JSN-AUTH", jsnAuth);
			
			StringEntity stringEntity = new StringEntity(postData, "UTF-8");
			
			post.setEntity(stringEntity);
			HttpResponse response = httpClient.execute(post);
			
		    HttpEntity respEntity = response.getEntity();
		    
		    if(response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
		    	result.put("resultCd", response.getStatusLine().getStatusCode());
		    	result.put("data", EntityUtils.toString(respEntity));
		    }else {
		    	result.put("resultCd", response.getStatusLine().getStatusCode());
		    	result.put("data", EntityUtils.toString(respEntity));
		    }
		} catch (Exception e) {
			logger.error("error : " +e.getMessage());
		}
		return result;
	}    
	
    public HashMap<String, Object> sendPostQuery(String url, String jsnAuth, JSONObject queryObj) {
    	HttpClient httpClient = HttpClientBuilder.create().build();
    	HashMap<String, Object> result = new HashMap<String, Object>();

    	try {
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
			post.setHeader("JSN-AUTH", jsnAuth);
			
			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			paramList.add(new BasicNameValuePair("pocket_sno", String.valueOf(queryObj.getInt("pocket_sno"))));
			paramList.add(new BasicNameValuePair("isue_dvsn_cd", String.valueOf(queryObj.getString("isue_dvsn_cd"))));
			paramList.add(new BasicNameValuePair("isue_amnt", String.valueOf(queryObj.getInt("isue_amnt"))));
			paramList.add(new BasicNameValuePair("invt_hist_sno", String.valueOf(queryObj.getInt("invt_hist_sno"))));

			post.setEntity(new UrlEncodedFormEntity(paramList, "UTF-8"));

			logger.info("pocket mssn url : "+ url);
			logger.info("pocket mssn parameter : "+ paramList.toString());
			logger.info("pocket mssn jsn-auth : "+ jsnAuth);

			HttpResponse response = httpClient.execute(post);
		    HttpEntity respEntity = response.getEntity();
		    if(response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
		    	result.put("resultCd", response.getStatusLine().getStatusCode());
		    	result.put("data", EntityUtils.toString(respEntity));
		    }else {
		    	result.put("resultCd", response.getStatusLine().getStatusCode());
		    	result.put("data", EntityUtils.toString(respEntity));
		    }
		    
		} catch (Exception e) {
			logger.info("error : " +e.getMessage());
		}
		return result;
	}
}
