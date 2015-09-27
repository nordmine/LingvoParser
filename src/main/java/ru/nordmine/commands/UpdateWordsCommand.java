package ru.nordmine.commands;

import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import ru.nordmine.helpers.ParseArticleHelper;
import ru.nordmine.helpers.RequestHelper;
import ru.nordmine.model.Article;

import java.io.File;
import java.net.URL;
import java.util.Map;

public class UpdateWordsCommand implements Command {

	private static final Logger logger = Logger.getLogger(UpdateWordsCommand.class);
	private ParseArticleHelper parserHelper = new ParseArticleHelper();

	@Override
	public void execute(URL siteUrl, String wordsDir, Map<String, Long> frequencyMap) {
		int counter = 0;
		for (Map.Entry<String, Long> item : frequencyMap.entrySet()) {
			logger.info("Progress: " + counter + " / " + frequencyMap.size());
			File articleFile = new File(wordsDir + "/" + item.getKey());
			if (articleFile.exists()) {
				parseAndUpdateArticle(siteUrl, articleFile.toString(), frequencyMap);
			} else {
				logger.error("File " + articleFile.getName() + " doesn't exists");
			}
			counter++;
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
				String json = new GsonBuilder()
//						.setPrettyPrinting()
						.create()
						.toJson(article);

				logger.info(json);
				HttpResponse response = RequestHelper.executeRequest(json, siteUrl.toString() + "/admin/update_article");
				if (response.getStatusLine().getStatusCode() == 200) {
					String responseString = EntityUtils.toString(response.getEntity());
					logger.info("Response for " + article.getWord() + ": " + responseString);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
}
