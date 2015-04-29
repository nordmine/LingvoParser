package ru.nordmine.commands;

import org.apache.log4j.Logger;
import ru.nordmine.helpers.RequestHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DownloadVoicesCommand implements Command {

	private static final Logger logger = Logger.getLogger(DownloadVoicesCommand.class);

	@Override
	public void execute(URL siteUrl, String wordsDir, Map<String, Long> frequencyMap) {
		Random random = new Random();
		File dir = new File(wordsDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		for (Map.Entry<String, Long> entry : frequencyMap.entrySet()) {
			File f = new File(wordsDir + File.separator + entry.getKey() + ".mp3");
			if (f.exists()) {
				logger.info(entry.getKey() + ".mp3 already exists");
			} else {
				try {
					String url = "https://ssl.gstatic.com/dictionary/static/sounds/de/0/" + URLEncoder.encode(entry.getKey(), "utf-8") + ".mp3";
					logger.info(url);
					RequestHelper.downloadFile(url, f);
					TimeUnit.MILLISECONDS.sleep(500 + random.nextInt(500));
				} catch (IOException | InterruptedException e) {
					logger.error(e);
				}
			}
		}
	}
}
