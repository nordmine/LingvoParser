package ru.nordmine.commands;

import java.util.Map;

public interface Command {

	void execute(String wordsDir, Map<String, Long> frequencyMap, String prefix);
}
