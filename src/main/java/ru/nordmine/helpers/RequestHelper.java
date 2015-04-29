package ru.nordmine.helpers;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.io.*;

public class RequestHelper {

	private static final Logger logger = Logger.getLogger(RequestHelper.class);
	// todo порефакторить методы запросов
	public static HttpResponse executeRequest(String xml, String url) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(xml, "UTF-8"));

		HttpResponse response = null;
		try {
			response = client.execute(post);
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("Response Code: " + statusCode);
		} catch (IOException e) {
			logger.error(e);
		}
		return response;
	}

	public static HttpResponse executeGetRequest(String url) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = null;
		try {
			response = client.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("Response Code: " + statusCode);
		} catch (IOException e) {
			logger.error(e);
		}
		return response;
	}

	public static void downloadFile(String url, File f) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = client.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("Response Code: " + statusCode);
			HttpEntity entity = response.getEntity();
			if (statusCode == 200 && entity != null) {
				BufferedInputStream bis = new BufferedInputStream(entity.getContent());
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
				int inByte;
				while ((inByte = bis.read()) != -1) {
					bos.write(inByte);
				}
				bis.close();
				bos.close();
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
