package ru.nordmine.parser;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import ru.nordmine.commands.Command;
import ru.nordmine.commands.CreateUrlsCommand;
import ru.nordmine.commands.GetContentCommand;
import ru.nordmine.commands.UpdateWordsCommand;
import ru.nordmine.helpers.FrequencyListHelper;
import ru.nordmine.helpers.RequestHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
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
		commandMap.put("content", new GetContentCommand());

		if (args.length >= 3) {
			URL siteUrl = new URL("http://" + args[0]);
			String wordsDir = args[1];
			File wordsDirFile = new File(wordsDir);
			if (!wordsDirFile.exists()) {
				logger.error("specified directory doesn't exists");
				return;
			}
			String command = args[2];
			Map<String, Long> frequencyMap;
			if (args.length >= 4) {
				String frequencyFileName = args[3];
				frequencyMap = FrequencyListHelper.parseFrequencyFile(frequencyFileName);
			} else {
				frequencyMap = FrequencyListHelper.parseFrequencyLines(getWordListFromSite(siteUrl, "/admin/word_list/"));
			}

			// удаляем из списка все слова, которые уже активны на сайте
			/*int lengthBefore = frequencyMap.size();
			for(String activeWord : getWordListFromSite(siteUrl, "/admin/active_words/")) {
				frequencyMap.remove(activeWord);
			}
			logger.info(lengthBefore - frequencyMap.size() + " words already active on site");*/

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

	private static List<String> getWordListFromSite(URL siteUrl, String path) throws IOException {
		HttpResponse response = RequestHelper.executeRequest("", siteUrl.toString() + path);
		if (response.getStatusLine().getStatusCode() == 200) {
			return Splitter.on("\n")
					.trimResults()
					.omitEmptyStrings()
					.splitToList(EntityUtils.toString(response.getEntity()));
		}
		return Collections.emptyList();
	}
}
