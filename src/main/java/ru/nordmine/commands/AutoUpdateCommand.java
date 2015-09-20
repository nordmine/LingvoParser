package ru.nordmine.commands;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import ru.nordmine.helpers.ContentHelper;

import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AutoUpdateCommand implements Command {

	private static final Logger logger = Logger.getLogger(AutoUpdateCommand.class);

	private final UpdateWordsCommand updateWordsCommand = new UpdateWordsCommand();
	private final ContentHelper contentHelper = new ContentHelper();

	@Override
	public void execute(URL siteUrl, String wordsDir, Map<String, Long> frequencyMap) {
		// список слов frequencyMap берётся с сайта /admin/new_words/

		wordsDir = wordsDir + DateTimeFormat.forPattern("yyyy-MM-dd/").print(DateTime.now());

		logger.info("====== Скачиваем все доступные словарные статьи и звуковые файлы ======");
		Random random = new Random();
		int counter = 0;
		for (Map.Entry<String, Long> entry : frequencyMap.entrySet()) {
			logger.info("Progress: " + counter + " / " + frequencyMap.size());

			int statusCode = contentHelper.downloadArticleFromYandex(wordsDir + "words/", entry.getKey());
			if (statusCode == 200 || statusCode == 302) {
				contentHelper.downloadVoiceFromGoogle(wordsDir + "voices/", entry.getKey());
			}

			if (statusCode > 0) {
				try {
					TimeUnit.MILLISECONDS.sleep(250 + random.nextInt(250));
				} catch (InterruptedException e) {
					logger.error(e);
				}
			}
			counter++;
		}

		logger.info("====== Создаём статьи на сайте, используя полученные файлы ======");
		updateWordsCommand.execute(siteUrl, wordsDir + "words/", frequencyMap);
	}
}
