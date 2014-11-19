package ru.nordmine.commands;

import java.net.URL;
import java.util.Map;

public interface Command {

	void execute(URL siteUrl, String wordsDir, Map<String, Long> frequencyMap);
}
