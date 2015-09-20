package ru.nordmine.helpers;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

public class ContentHelper {

	private static final Logger logger = Logger.getLogger(ContentHelper.class);

	public int downloadArticleFromYandex(String wordsDir, String word) {
		int statusCode = 0;
		File f = new File(wordsDir + File.separator + word);
		if (f.exists()) {
			logger.info("File for word " + word + " already exists");
		} else {
			try {
				String url = "http://slovari.yandex.ru/" + URLEncoder.encode(word, "utf-8") + "/" + URLEncoder.encode("перевод", "utf-8");
				logger.info(url);
				HttpResponse response = RequestHelper.executeGetRequest(url);
				statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					String responseBody = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
					logger.info("Length for " + word + ": " + responseBody.length());
					FileUtils.writeStringToFile(f, responseBody, Charsets.UTF_8);
				} else if (statusCode == 404) {
					logger.error("Word " + word + " not found");
				} else if (statusCode == 302) {
					logger.warn("Redirect for " + word + ": " + response.getFirstHeader("Location").getValue());
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return statusCode;
	}

	public boolean downloadVoiceFromGoogle(String wordsDir, String word) {
		boolean wasRequest = false;
		File dir = new File(wordsDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File f = new File(wordsDir + File.separator + word + ".mp3");
		if (f.exists()) {
			logger.info(word + ".mp3 already exists");
		} else {
			try {
				String url = "https://ssl.gstatic.com/dictionary/static/sounds/de/0/" + URLEncoder.encode(word, "utf-8") + ".mp3";
				logger.info(url);
				RequestHelper.downloadFile(url, f);
				wasRequest = true;
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return wasRequest;
	}
}
