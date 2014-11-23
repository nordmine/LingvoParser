package ru.nordmine.commands;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import ru.nordmine.helpers.ParseArticleHelper;
import ru.nordmine.helpers.RequestHelper;
import ru.nordmine.parser.Article;

import java.io.File;
import java.net.URL;
import java.util.Map;

public class UpdateWordsCommand implements Command {

	private static final Logger logger = Logger.getLogger(UpdateWordsCommand.class);
	private ParseArticleHelper parserHelper = new ParseArticleHelper();

	@Override
	public void execute(URL siteUrl, String wordsDir, Map<String, Long> frequencyMap) {
		for (Map.Entry<String, Long> item : frequencyMap.entrySet()) {
			File articleFile = new File(wordsDir + "/" + item.getKey());
			if (articleFile.exists()) {
				parseAndUpdateArticle(siteUrl, articleFile.toString(), frequencyMap);
			} else {
				logger.error("File " + articleFile.getName() + " doesn't exists");
			}
		}
	}

	private void parseAndUpdateArticle(URL siteUrl, String sourceFileName, Map<String, Long> frequencyMap) {
		try {
			logger.info(sourceFileName);
			Article article = parserHelper.getArticle(sourceFileName);
			if (article != null) {
				if (frequencyMap.containsKey(article.getWord())) {
					article.setFrequency(frequencyMap.get(article.getWord()));
				}
				logger.info(article.getFrequency());
				String xml = article.toXml();
				logger.info(xml);
				HttpResponse response = RequestHelper.executeRequest(xml, siteUrl.toString() + "/service/update_article");
				if (response.getStatusLine().getStatusCode() == 200) {
					String responseString = EntityUtils.toString(response.getEntity());
					logger.info("Response: " + responseString);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
}
