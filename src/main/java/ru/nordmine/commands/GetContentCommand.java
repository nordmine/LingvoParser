package ru.nordmine.commands;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import ru.nordmine.helpers.RequestHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class GetContentCommand implements Command {

	private static final Logger logger = Logger.getLogger(GetContentCommand.class);

	@Override
	public void execute(URL siteUrl, String wordsDir, Map<String, Long> frequencyMap) {
		Random random = new Random();
		for (Map.Entry<String, Long> entry : frequencyMap.entrySet()) {
			File f = new File(wordsDir + File.separator + entry.getKey());
			if (f.exists()) {
				logger.info("File for word " + entry.getKey() + " already exists");
			} else {
				try {
					String url = "http://slovari.yandex.ru/" + URLEncoder.encode(entry.getKey(), "utf-8") + "/" + URLEncoder.encode("перевод", "utf-8");
					HttpResponse response = RequestHelper.executeRequest("", url);
					if (response.getStatusLine().getStatusCode() == 200) {
						String responseBody = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
						logger.info("Length for " + entry.getKey() + ": " + responseBody.length());
						FileUtils.writeStringToFile(f, responseBody, Charsets.UTF_8);
					} else if (response.getStatusLine().getStatusCode() == 404) {
						logger.error("Word " + entry.getKey() + " not found");
					} else if (response.getStatusLine().getStatusCode() == 302) {
						logger.warn("Redirect for " + entry.getKey() + ": " + response.getFirstHeader("Location").getValue());
					}
					TimeUnit.MILLISECONDS.sleep(500 + random.nextInt(500));
				} catch (IOException | InterruptedException e) {
					logger.error(e);
				}
			}
		}
	}
}
