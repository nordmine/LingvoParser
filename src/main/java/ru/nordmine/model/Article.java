package ru.nordmine.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Article {

	private String word;
	private long frequency;
	private String mainSpeechPart;
	private Set<String> sounds = new LinkedHashSet<>();
	private Map<String, Set<Translation>> trans = new LinkedHashMap<>();
	private Map<String, String> examples = new LinkedHashMap<>();

	public void setWord(String word) {
		this.word = word;
	}

	public String getWord() {
		return word;
	}

	public Set<String> getSounds() {
		return sounds;
	}

	public Map<String, Set<Translation>> getTrans() {
		return trans;
	}

	public String getMainSpeechPart() {
		return mainSpeechPart;
	}

	public void setMainSpeechPart(String mainSpeechPart) {
		this.mainSpeechPart = mainSpeechPart;
	}

	public Map<String, String> getExamples() {
		return examples;
	}

	public long getFrequency() {
		return frequency;
	}

	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}
}
