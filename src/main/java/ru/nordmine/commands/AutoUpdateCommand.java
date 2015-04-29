package ru.nordmine.commands;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.net.URL;
import java.util.Map;

public class AutoUpdateCommand implements Command {

	private static final Logger logger = Logger.getLogger(AutoUpdateCommand.class);

	private final DownloadVoicesCommand downloadVoicesCommand = new DownloadVoicesCommand();
	private final UpdateWordsCommand updateWordsCommand = new UpdateWordsCommand();
	private final GetContentCommand getContentCommand = new GetContentCommand();

	@Override
	public void execute(URL siteUrl, String wordsDir, Map<String, Long> frequencyMap) {
		// список слов frequencyMap берётся с сайта /admin/new_words/

		wordsDir = wordsDir + DateTimeFormat.forPattern("yyyy-MM-dd/").print(DateTime.now());

		logger.info("====== Скачиваем все доступные словарные статьи ======");
		getContentCommand.execute(siteUrl, wordsDir + "words/", frequencyMap);

		logger.info("====== Скачиваем все доступные звуковые файлы ======");
		downloadVoicesCommand.execute(siteUrl, wordsDir + "voices/", frequencyMap);

		logger.info("====== Создаём статьи на сайте, используя полученные файлы ======");
		updateWordsCommand.execute(siteUrl, wordsDir + "words/", frequencyMap);
	}
}
