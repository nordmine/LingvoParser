package ru.nordmine.helpers;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

public class RequestHelper {

	private static final Logger logger = Logger.getLogger(RequestHelper.class);
	private static final String SITE_BASE_ADDRESS = "http://nordmine.test/";

	public static int executeRequest(String xml, String urn) {
		int updateCounter = 0;
		String url = SITE_BASE_ADDRESS + urn;
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(xml, "UTF-8"));

		HttpResponse response;
		try {
			response = client.execute(post);
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("Response Code: " + statusCode);
			String responseString = EntityUtils.toString(response.getEntity());
			logger.info("Response: " + responseString);
		} catch (IOException e) {
			logger.error(e);
		}
		return updateCounter;
	}
}
