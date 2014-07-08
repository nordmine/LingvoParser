package ru.nordmine.yandex.parser;


import com.google.common.base.Splitter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

public class Parser {

	private static final Logger logger = Logger.getLogger(Parser.class);
	private static String siteBaseAddress = "http://nordmine.test/";

	public static void main(String[] args) throws Exception {
		String word = null;
		if (args.length >= 1) {
			word = args[0];
		} else {
			return;
		}
		Document doc = null;
		try {
			doc = parse("/home/nordmine/words/" + word);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		if (doc == null) {
			return;
		}

		Article article = new Article();

		List<Node> rootNodes = doc.selectNodes("//div[@class='b-translation__card b-translation__card_examples_three']");
		Node rootNode = toSingleNode(rootNodes);

		List<Node> headerNodes = rootNode.selectNodes("//h1");
		Node headerNode = toSingleNode(headerNodes);

		List<Node> wordNodes = headerNode.selectNodes("span[@class='b-translation__text']");
		article.setWord(toSingleNode(wordNodes).getText().trim());

		List<Node> soundNodes = headerNode.selectNodes("span[@class='b-translation__tr']");
		Set<String> sounds = new LinkedHashSet<String>();
		for (Node sound : soundNodes) {
			sounds.addAll(Splitter.on(",").omitEmptyStrings().trimResults().splitToList(sound.getText()));
		}
		article.getSounds().addAll(sounds);
		List<Node> translationGroups = rootNode.selectNodes("div[contains(@class,'b-translation__group')]");
		for (Node group : translationGroups) {
			String speechPart = extractFuckingSpeechPart(group);
			if(!speechPart.matches("[a-z]+")) {
				continue;
			}
			Map<String, Set<String>> parts = article.getTrans();
			Set<String> transSet = new LinkedHashSet<String>();
			parts.put(speechPart, transSet);
			List<Node> transNodes = group.selectNodes("ol/li");
			for (Node trans : transNodes) {
				List<Node> transItemNodes = trans.selectNodes("div/span/a/span[@class='b-translation__text']");
				for (Node transItem : transItemNodes) {
					if (transSet.size() < 5) {
						transSet.add(transItem.getText());
					}
				}
				List<Node> exampleNodes = trans.selectNodes("div[@class='b-translation__examples']/div[@class='b-translation__example']");
				Map<String, Set<String>> examples = article.getExamples();
				for (Node example : exampleNodes) {
					String phrase = toSingleNode((example.selectNodes("span[@class='b-translation__example-original']/span"))).getText();
					Set<String> exList = new LinkedHashSet<String>();
					List<Node> exNodeList = example.selectNodes("span[@class='b-translation__text']");
					for (Node exNode : exNodeList) {
						exList.add(exNode.getText());
					}
					examples.put(phrase, exList);
				}
			}
		}

		String xml = article.toXml();
		System.out.println(xml);
		executeRequest(xml, "update_article");
	}

	/**
	 * Грязный хак, ибо по-другому не работает
	 * @param group
	 * @return
	 */
	private static String extractFuckingSpeechPart(Node group) {
		String groupXml = group.asXML();
		String beginString = "<h2 class=\"b-translation__group-title\" id=\"";
		int startIndex = groupXml.indexOf(beginString) + beginString.length();
		return groupXml.substring(startIndex, groupXml.indexOf("\"", startIndex));
	}

	private static Node toSingleNode(List<Node> nodes) throws Exception {
		if (nodes.isEmpty()) {
			throw new Exception("node list is empty");
		}
		if (nodes.size() > 1) {
			throw new Exception("one element in node list expected, but was " + nodes.size());
		}
		return nodes.get(0);
	}

	public static Document parse(String url) throws DocumentException {
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

	private static int executeRequest(String xml, String action) {
		int updateCounter = 0;
		String url = siteBaseAddress + "service/" + action;
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(xml, "UTF-8"));

		HttpResponse response;
		try {
			response = client.execute(post);
			logger.info("Response Code: " + response.getStatusLine().getStatusCode());
			String responseString = EntityUtils.toString(response.getEntity());
			logger.info("Response: " + responseString);
		} catch (IOException e) {
			logger.error(e);
		}
		return updateCounter;
	}
}
