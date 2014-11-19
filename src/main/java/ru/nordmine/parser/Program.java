package ru.nordmine.parser;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.log4j.Logger;
import ru.nordmine.commands.Command;
import ru.nordmine.commands.CreateUrlsCommand;
import ru.nordmine.commands.UpdateWordsCommand;
import ru.nordmine.helpers.FrequencyListHelper;
import ru.nordmine.helpers.RequestHelper;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Program {

	private static final Logger logger = Logger.getLogger(Program.class);

	public static void main(String[] args) throws Exception {
		Map<String, Command> commandMap = new LinkedHashMap<String, Command>();
		commandMap.put("urls", new CreateUrlsCommand("http://slovari.yandex.ru/", "", "urls.txt"));
		commandMap.put("voices", new CreateUrlsCommand("https://ssl.gstatic.com/dictionary/static/sounds/de/0/", ".mp3", "voices.txt"));
		commandMap.put("update", new UpdateWordsCommand());

		if (args.length >= 3) {
			URL siteUrl = new URL("http://" + args[0]);
			String wordsDir = args[1];
			File wordsDirFile = new File(wordsDir);
			if(!wordsDirFile.exists()) {
				logger.error("specified directory doesn't exists");
				return;
			}
			String command = args[2];
			Map<String, Long> frequencyMap;
			if(args.length >= 4) {
				String frequencyFileName = args[3];
				frequencyMap = FrequencyListHelper.parseFrequencyFile(frequencyFileName);
			} else {
				frequencyMap = getWordListFromSite(siteUrl);
			}

			if (commandMap.containsKey(command)) {
				commandMap.get(command).execute(siteUrl, wordsDir, frequencyMap);
			} else {
				logger.error("Invalid command");
			}
		} else {
			logger.info("1st param - site url (e.g. \"nordmine.ru\")");
			logger.info("2nd param - absolute path for words directory");
			logger.info("3rd param - [" + Joiner.on(", ").join(commandMap.keySet()) + "]");
			logger.info("4th param - frequency file [optional, if not specified - list from site]");
		}
	}

	private static Map<String, Long> getWordListFromSite(URL siteUrl) {
		Map<String, Long> frequencyMap;
		String response = RequestHelper.executeRequest("", siteUrl.toString() + "/admin/word_list/");

		List<String> lines = Splitter.on("\n")
				.trimResults()
				.omitEmptyStrings()
				.splitToList(response);

		frequencyMap = FrequencyListHelper.parseFrequencyLines(lines);
		return frequencyMap;
	}
}
