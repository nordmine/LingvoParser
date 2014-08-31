package ru.nordmine.parser;

import com.google.common.base.Joiner;
import org.apache.log4j.Logger;
import ru.nordmine.commands.Command;
import ru.nordmine.commands.CreateUrlsCommand;
import ru.nordmine.commands.UpdateWordsCommand;
import ru.nordmine.helpers.FrequencyListHelper;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class Program {

	private static final Logger logger = Logger.getLogger(Program.class);
	private static final String FREQUENCY_FILE_NAME = "frequency.csv";

	public static void main(String[] args) throws Exception {
		Map<String, Command> commandMap = new LinkedHashMap<String, Command>();
		commandMap.put("urls", new CreateUrlsCommand("http://slovari.yandex.ru/", "", "urls.txt"));
		commandMap.put("voices", new CreateUrlsCommand("https://ssl.gstatic.com/dictionary/static/sounds/de/0/", ".mp3", "voices.txt"));
		commandMap.put("update", new UpdateWordsCommand());

		if (args.length >= 2) {
			String command = args[0];
			String wordsDir = args[1];
			File wordsDirFile = new File(wordsDir);
			if(!wordsDirFile.exists()) {
				logger.error("specified directory doesn't exists");
				return;
			}
			String prefix = null;
			if(args.length >= 3) {
				prefix = args[2];
			}

			if (commandMap.containsKey(command)) {
				commandMap.get(command).execute(
						wordsDir,
						FrequencyListHelper.parseFrequencyMap(wordsDir + "/" + FREQUENCY_FILE_NAME),
						prefix
				);
			} else {
				logger.error("Invalid command");
			}
		} else {
			logger.info("1st param - [" + Joiner.on(", ").join(commandMap.keySet()) + "]");
			logger.info("2nd param - words directory (" + FREQUENCY_FILE_NAME + " expected)");
			logger.info("3rd param - word prefix [optional]");
		}
	}
}
