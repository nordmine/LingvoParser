package ru.nordmine.commands;

import org.apache.log4j.Logger;
import ru.nordmine.helpers.ParseArticleHelper;
import ru.nordmine.helpers.RequestHelper;
import ru.nordmine.parser.Article;

import java.io.File;
import java.util.Map;

public class UpdateWordsCommand implements Command {

	private static final Logger logger = Logger.getLogger(UpdateWordsCommand.class);
	private ParseArticleHelper parserHelper = new ParseArticleHelper();

	@Override
	public void execute(String wordsDir, Map<String, Long> frequencyMap, String prefix) {
		for (Map.Entry<String, Long> item : frequencyMap.entrySet()) {
			if (prefix != null && !item.getKey().startsWith(prefix)) {
				continue;
			}
			File articleFile = new File(wordsDir + "/" + item.getKey());
			if (articleFile.exists()) {
				parseAndUpdateArticle(articleFile.toString(), frequencyMap);
			} else {
				logger.error("File " + articleFile.getName() + " doesn't exists");
			}
		}
	}

	private void parseAndUpdateArticle(String sourceFileName, Map<String, Long> frequencyMap) {
		try {
			Article article = parserHelper.getArticle(sourceFileName);
			if (article != null) {
				if (frequencyMap.containsKey(article.getWord())) {
					article.setFrequency(frequencyMap.get(article.getWord()));
				}
				String xml = article.toXml();
				logger.info(xml);
				RequestHelper.executeRequest(xml, "service/update_article");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
