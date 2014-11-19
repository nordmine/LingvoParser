package ru.nordmine.commands;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CreateUrlsCommand implements Command {

	private static final Logger logger = Logger.getLogger(CreateUrlsCommand.class);

	private String urlPrefix;
	private String urlSuffix;
	private String fileName;

	public CreateUrlsCommand(String urlPrefix, String urlSuffix, String fileName) {
		this.urlPrefix = urlPrefix;
		this.urlSuffix = urlSuffix;
		this.fileName = fileName;
	}

	@Override
	public void execute(URL siteUrl, String wordsDir, Map<String, Long> frequencyMap) {
		List<String> urls = new LinkedList<String>();
		for (Map.Entry<String, Long> item : frequencyMap.entrySet()) {
			urls.add(urlPrefix + item.getKey() + urlSuffix);
		}
		File urlsFile = new File(wordsDir + File.separator + fileName);
		try {
			Files.write(urlsFile.toPath(), urls, Charset.forName("utf-8"));
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
