package ru.nordmine;

import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import ru.nordmine.helpers.ParseArticleHelper;
import ru.nordmine.model.Article;

public class ArticleHelperTest {

	private static final Logger logger = Logger.getLogger(ArticleHelperTest.class);

	@Test
	@Ignore
	public void parseArticle() throws Exception {
		String word = "bigfoot";
		ParseArticleHelper articleHelper = new ParseArticleHelper();
		Article article = articleHelper.getArticle("file:///home/nordmine/words/" + word);
		logger.info(
				new GsonBuilder()
						.setPrettyPrinting()
						.create()
						.toJson(article)
		);
	}
}
