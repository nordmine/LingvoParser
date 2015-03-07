package ru.nordmine.helpers;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.xml.sax.SAXException;
import ru.nordmine.model.Article;
import ru.nordmine.model.Translation;

import java.io.IOException;
import java.util.*;

public class ParseArticleHelper {

	private static final Logger logger = Logger.getLogger(ParseArticleHelper.class);

	public Article getArticle(String wordFile) throws Exception {
		Document doc = null;
		try {
			doc = parse(wordFile);
		} catch (DocumentException e) {
			logger.error(e);
		}
		if (doc == null) {
			return null;
		}

		Article article = new Article();

		List<Node> rootNodes = doc.selectNodes("//div[@class='b-translation__card b-translation__card_examples_three']");
		Node rootNode = toSingleNode("root", rootNodes);

		List<Node> headerNodes = rootNode.selectNodes("//h1");
		Node headerNode = toSingleNode("header", headerNodes);

		List<Node> wordNodes = headerNode.selectNodes("span[@class='b-translation__text']");
		article.setWord(toSingleNode("word", wordNodes).getText().trim().toLowerCase());
		logger.info(article.getWord());

		List<Node> soundNodes = headerNode.selectNodes("span[@class='b-translation__tr']");
		if (soundNodes.isEmpty()) {
			soundNodes = rootNode.selectNodes("div[1]/span[@class='b-translation__tr']");
		}
		Set<String> sounds = new LinkedHashSet<>();
		for (Node sound : soundNodes) {
			sounds.addAll(Splitter.on(",").omitEmptyStrings().trimResults().splitToList(sound.getText()));
		}
		for (String sound : sounds) {
			// добиваемся того, чтобы каждая транскрипция была обрамлена квадратными скобками
			article.getSounds().add("[" + sound.replace("[", "").replace("]", "") + "]");
		}
		int mainSpeechPartRating = 0;
		List<Node> translationGroups = rootNode.selectNodes("div[contains(@class,'b-translation__group')]");
		for (Node group : translationGroups) {
			Node speechPartNode = group.selectSingleNode("h2[@class='b-translation__group-title']/@id");
			if (speechPartNode == null) {
				continue;
			}
			String speechPart = speechPartNode.getText().trim().toLowerCase();
			if (!speechPart.matches("[a-z]+")) {
				continue;
			}
			Map<String, Set<Translation>> parts = article.getTrans();
			Set<Translation> transSet = new LinkedHashSet<>();
			parts.put(speechPart, transSet);
			List<Node> transNodes = group.selectNodes("ol/li");
			int exampleCounter = 0;
			int transCounter = 0;
			for (Node trans : transNodes) {
				// формируем список сокращений для данного перевода
				List<Node> abbrNodes = trans.selectNodes("//node()[@class = 'b-translation__abbr']");
				Set<String> abbrs = new HashSet<>();
				for (Node abbrNode : abbrNodes) {
					abbrs.add(abbrNode.getStringValue());
				}
				List<Node> transItemNodes = trans.selectNodes("div/span");
				for (Node transItem : transItemNodes) {
					List<String> splittedTranslations = Splitter.on(";")
							.omitEmptyStrings()
							.splitToList(
									replaceSymbolInParentheses(transItem.getStringValue(), ';', ',')
							);

					for (String splittedTranslation : splittedTranslations) {
						for (String abbr : abbrs) {
							if (splittedTranslation.contains(abbr)) {
								// исключаем сокращения из текста перевода
								splittedTranslation = splittedTranslation.replace(abbr, "");
							}
						}
						String trimmedTranslation = CharMatcher.anyOf(";,- ").or(CharMatcher.inRange('a', 'z')).trimFrom(splittedTranslation);
						Translation translation = populateTranslation(trimmedTranslation.replace('«', '"').replace('»', '"'));
						if (translation.getText() != null && translation.getText().matches("^[а-яА-Я\"][а-яА-Я\\s\\-,\\.\\?!\"]{0,98}[а-яА-Я\\.\\?!\"]$")) {
							if (transSet.size() < 5) {
								transSet.add(translation);
							}
							transCounter++;
						}
					}
				}
				List<Node> exampleNodes = trans.selectNodes("div[@class='b-translation__examples']/div[@class='b-translation__example']");
				Map<String, String> examples = article.getExamples();
				for (Node example : exampleNodes) {
					try {
						String phrase = toSingleNode("phrase", example.selectNodes("span[@class='b-translation__example-original']/span")).getText();
						Set<String> exList = new LinkedHashSet<>();
						List<Node> exNodeList = example.selectNodes("span[@class='b-translation__text']");
						for (Node exNode : exNodeList) {
							exList.add(exNode.getText());
						}
						if (examples.size() < 10) {
							examples.put(phrase, Joiner.on(' ').join(exList));
						}
						exampleCounter += exampleNodes.size();
					} catch (Exception e) {
						logger.warn(e.getMessage() + " phrase ignored");
					}
				}
			}
			int currentSpeechPartRating = transCounter + exampleCounter;
			logger.info(speechPart + " rate = " + currentSpeechPartRating);
			if (mainSpeechPartRating < currentSpeechPartRating) {
				mainSpeechPartRating = currentSpeechPartRating;
				article.setMainSpeechPart(speechPart);
			}
		}
		return article;
	}

	private static Translation populateTranslation(String transText) throws Exception {
		int beginParentheses = transText.indexOf('(');
		Translation translation = new Translation();
		if (beginParentheses == -1) {
			translation.setText(transText.trim());
		} else {
			int endParentheses = transText.indexOf(')', beginParentheses);
			if (endParentheses == -1) {
				// если не найдена закрывающая скобка, считаем, что комментарий идёт до конца
				endParentheses = transText.length();
			}
			if (beginParentheses >= endParentheses) {
				throw new Exception("Bad parentheses");
			}
			String comment = transText.substring(beginParentheses + 1, endParentheses).trim();
			if (comment.length() <= 150) {
				translation.setComment(comment);
			}
			if (beginParentheses == 0) {
				// комментарий идёт в начале
				if (endParentheses < transText.length()) {
					translation.setText(transText.substring(endParentheses + 1).trim());
				}
			} else {
				// комментарий идёт в конце
				translation.setText(transText.substring(0, beginParentheses).trim());
			}
		}
		return translation;
	}

	private static String replaceSymbolInParentheses(String source, char symbol, char replacement) throws Exception {
		StringBuilder result = new StringBuilder();
		int openedParentheses = 0;
		for (char c : source.toCharArray()) {
			if (c == '(') {
				openedParentheses++;
			}
			if (c == ')') {
				openedParentheses--;
				if (openedParentheses < 0) {
					logger.warn("Opened parentheses index less than zero! Skipped.");
					return "";
				}
			}
			if (c == symbol && openedParentheses > 0) {
				result.append(replacement);
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	private Node toSingleNode(String caption, List<Node> nodes) throws Exception {
		if (nodes.isEmpty()) {
			throw new Exception(caption + " is empty");
		}
		if (nodes.size() > 1) {
			List<String> nodeListText = new LinkedList<String>();
			for (Node n : nodes) {
				nodeListText.add(n.getText());
			}
			throw new Exception("one element for " + caption + " expected, but was " + nodes.size()
					+ ": " + Joiner.on(",").join(nodeListText));
		}
		return nodes.get(0);
	}

	private Document parse(String url) throws DocumentException {
		org.apache.xerces.xni.parser.XMLParserConfiguration config =
				new org.cyberneko.html.HTMLConfiguration();
		config.setFeature("http://cyberneko.org/html/features/scanner/allow-selfclosing-tags", true);
		config.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
		DOMParser parser = new DOMParser(config);
		try {
			parser.parse(url);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		DOMReader reader = new DOMReader();
		org.dom4j.Document doc = reader.read(parser.getDocument());
		return doc;
	}
}
