package ru.nordmine.commands;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CreateUrlsCommand implements Command {

	private static final Logger logger = Logger.getLogger(CreateUrlsCommand.class);
	public static final String YANDEX_DICT_BASE_URL = "http://slovari.yandex.ru/";

	@Override
	public void execute(String wordsDir, Map<String, Long> frequencyMap, String prefix) {
		List<String> urls = new LinkedList<String>();
		for (Map.Entry<String, Long> item : frequencyMap.entrySet()) {
			if (prefix != null && !item.getKey().startsWith(prefix)) {
				continue;
			}
			urls.add(YANDEX_DICT_BASE_URL + item.getKey());
		}
		File urlsFile = new File(wordsDir + "/urls.txt");
		try {
			Files.write(urlsFile.toPath(), urls, Charset.forName("utf-8"));
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
