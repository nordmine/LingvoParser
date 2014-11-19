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

	public static String executeRequest(String xml, String url) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(xml, "UTF-8"));

		HttpResponse response;
		String responseString = null;
		try {
			response = client.execute(post);
			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("Response Code: " + statusCode);
			responseString = EntityUtils.toString(response.getEntity());
			logger.info("Response: " + responseString);
		} catch (IOException e) {
			logger.error(e);
		}
		return responseString;
	}
}
