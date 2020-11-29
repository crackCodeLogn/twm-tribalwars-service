package com.vv.personal.twm.tribalwars.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Vivek
 * @since 29/11/20
 */
public class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static String readFileFromLocation(String src) {
        StringBuilder data = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader(new File(src)))) {
            String line;
            while ((line = in.readLine()) != null) data.append(line.trim()).append("\n");
        } catch (IOException e) {
            LOGGER.error("Failed to read file contents from '{}'. ", src, e);
        }
        LOGGER.info("Data read in => \n{}", data);
        return data.toString();
    }
}
