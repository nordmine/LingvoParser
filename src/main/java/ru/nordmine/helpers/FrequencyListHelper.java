package ru.nordmine.helpers;

import com.google.common.base.Splitter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FrequencyListHelper {

	private static final Logger logger = Logger.getLogger(FrequencyListHelper.class);

	public static Map<String, Long> parseFrequencyMap(String sourceFileName) {
		File sourceFile = new File(sourceFileName);
		Map<String, Long> frequencyMap = new LinkedHashMap<String, Long>();
		if (sourceFile.exists()) {
			try {
				List<String> lines = Files.readAllLines(sourceFile.toPath(), Charset.forName("utf-8"));
				for (String line : lines) {
					List<String> parts = Splitter.on(",").trimResults().splitToList(line);
					if (parts.size() >= 1) {
						String word = parts.get(0).toLowerCase();
						if (word.matches("^\\w{2,}$")) {
							if (parts.size() == 2) {
								if (parts.get(1).matches("^\\d+$")) {
									logger.debug(word + " " + parts.get(1));
									if (frequencyMap.get(word) == null) {
										frequencyMap.put(word, Long.parseLong(parts.get(1)));
									}
									continue;
								}
							}
							logger.warn(word + " without frequency");
						} else {
							logger.warn("Word *" + word + "* doesn't matches to regexp");
						}
					}
				}
			} catch (IOException e) {
				logger.error(e);
			}
		} else {
			logger.error("File *" + sourceFileName + "* does not exist");
		}
		return frequencyMap;
	}
}