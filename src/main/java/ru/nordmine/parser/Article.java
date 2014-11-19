package ru.nordmine.parser;

import com.google.common.base.Joiner;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Article {

	private String word;
	private long frequency;
	private Set<String> sounds = new LinkedHashSet<String>();
	private Map<String, Set<String>> trans = new LinkedHashMap<String, Set<String>>();
	private Map<String, Set<String>> examples = new LinkedHashMap<String, Set<String>>();

	public void setWord(String word) {
		this.word = word;
	}

	public String getWord() {
		return word;
	}

	public Set<String> getSounds() {
		return sounds;
	}

	public Map<String, Set<String>> getTrans() {
		return trans;
	}

	public Map<String, Set<String>> getExamples() {
		return examples;
	}

	public long getFrequency() {
		return frequency;
	}

	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}

	public String toXml() {
		Document request = DocumentHelper.createDocument();
		Element rootElement = request.addElement("request");
		Element wordElement = rootElement.addElement("word");
		wordElement.setText(word);
		Element frequencyElement = rootElement.addElement("frequency");
		frequencyElement.setText(Long.toString(frequency));
		Element soundsElement = rootElement.addElement("sounds");
		for (String sound : sounds) {
			Element soundEl = soundsElement.addElement("sound");
			soundEl.setText(sound);
		}
		Element speechPartsElement = rootElement.addElement("speechParts");
		for (Map.Entry<String, Set<String>> speechPart : trans.entrySet()) {
			Element speechPartElement = speechPartsElement.addElement("speechPart");
			speechPartElement.addAttribute("type", speechPart.getKey());
			for (String tran : speechPart.getValue()) {
				Element tranElement = speechPartElement.addElement("tran");
				tranElement.setText(tran);
			}
		}

		Element examplesElement = rootElement.addElement("examples");
		for (Map.Entry<String, Set<String>> example : examples.entrySet()) {
			Element exampleElement = examplesElement.addElement("example");
			Element originalElement = exampleElement.addElement("original");
			originalElement.setText(example.getKey());
			Element transTextElement= exampleElement.addElement("transText");
			transTextElement.setText(Joiner.on(" ").join(example.getValue()).trim());
		}

		return request.asXML();
	}
}
