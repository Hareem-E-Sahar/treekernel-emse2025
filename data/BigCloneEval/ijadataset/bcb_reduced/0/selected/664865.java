package edu.colorado.emml.construction.parser;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeedPreprocessor implements Preprocessor {

    private String regex = "seed\\s*=\\s*[0-9]+\\s*;";

    public ITreeProcessor[] getProcessors(String text) {
        ArrayList<ITreeProcessor> list = new ArrayList<ITreeProcessor>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String found = text.substring(matcher.start(), matcher.end());
            StringTokenizer st = new StringTokenizer(found, "=; ");
            st.nextToken();
            list.add(new SeedHandler(Integer.parseInt(st.nextToken())));
        }
        return list.toArray(new ITreeProcessor[list.size()]);
    }

    public String stripUsed(String text) {
        text = text.replaceAll(regex, "");
        return text;
    }
}
