package com.cyberpointllc.stac.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This engine takes a template in the form of a string and possibly a start tag
 * and end tag that define how the template identifies keys. Once an engine is
 * created, replaceTags takes a dictionary containing the template keys and
 * associated values. It outputs a string with the template keys replaced by
 * their associated values.
 * 
 * An example:
 * Template engine reads in "Hello, {{name}}. Good {{timeOfDay}}."
 * 
 * Our dictionary contains the key "name" with value "Bob" and the key
 * "timeOfDay" with value "evening".
 * 
 * Our output from replaceTags with this dictionary is "Hello, Bob. Good evening."
 *
 */
public class TemplateEngine {

    private String startTag;

    private String endTag;

    private Pattern pattern;

    private String text;

    public TemplateEngine(String startTag, String endTag, String text) {
        this.startTag = startTag;
        this.endTag = endTag;
        this.pattern = Pattern.compile(startTag + ".*?" + endTag);
        this.text = text;
    }

    public TemplateEngine(String text) {
        this("\\{\\{", "\\}\\}", text);
    }

    /**
     * finds the start and end location of each tag
     * 
     * @return A list of Pairs, where a Pair is the start and end location of a
     *         tag
     */
    public List<Pair<Integer, Integer>> findTags() {
        Matcher matcher = pattern.matcher(text);
        List<Pair<Integer, Integer>> tagsList = new  ArrayList();
        while (matcher.find()) {
            findTagsHelper(tagsList, matcher);
        }
        return tagsList;
    }

    /**
     * Creates a new String where the tags in text have been replaced with the
     * values specified in the dictionary
     * 
     * @param dictionary
     *            a Map with template keys and their corresponding values
     * @return new version of text where the tags and their keys have been
     *         replaced with the keys' corresponding values
     */
    public String replaceTags(Map<String, String> dictionary) {
        StringBuilder sb = new  StringBuilder();
        replaceTagsBuilder(dictionary, sb);
        return sb.toString();
    }

    /**
     * Adds to the string builder, the template where the tags in text have been replaced with the
     * values specified in the dictionary
     *
     * @param dictionary
     *            a Map with template keys and their corresponding values
     * @param sb
     *            The string builder to put the data in
     */
    public void replaceTagsBuilder(Map<String, String> dictionary, StringBuilder sb) {
        replaceTagsBuilderHelper(sb, dictionary);
    }

    public String replaceTags(Templated templated) {
        return replaceTags(templated.getTemplateMap());
    }

    /**
     * Applies the template to each item in 'templateds' returning the string
     * @param templateds the items to apply to the template
     * @param separator the separator to put after each item
     *
     * @return a string representing all of the templated items
     */
    public String replaceTags(List<? extends Templated> templateds, String separator) {
        StringBuilder sb = new  StringBuilder();
        for (Templated templated : templateds) {
            replaceTagsBuilder(templated.getTemplateMap(), sb);
            sb.append(separator);
        }
        return sb.toString();
    }

    /**
     * Applies the template to each item in 'templateds' returning the string
     * @param templateds the items to apply to the template
     *
     * @return a string representing all of the templated items
     */
    public String replaceTags(List<? extends Templated> templateds) {
        return replaceTags(templateds, "");
    }

    private void findTagsHelper(List<Pair<Integer, Integer>> tagsList, Matcher matcher) {
        tagsList.add(Pair.of(matcher.start(), matcher.end()));
    }

    private void replaceTagsBuilderHelper(StringBuilder sb, Map<String, String> dictionary) {
        // keep track of where we are on the text string
        int linePointer = 0;
        int startTagLength = StringEscapeUtils.unescapeJava(startTag).length();
        int endTagLength = StringEscapeUtils.unescapeJava(endTag).length();
        List<Pair<Integer, Integer>> tagsList = findTags();
        for (int i = 0; i < tagsList.size(); i++) {
            int startTagLocation = tagsList.get(i).getLeft();
            int endTagLocation = tagsList.get(i).getRight();
            // append the part of the text that doesn't have tags
            sb.append(text.substring(linePointer, startTagLocation));
            // get the dictionary key
            String key = text.substring(startTagLocation + startTagLength, endTagLocation - endTagLength).trim();
            // append the value to the text instead of the key
            sb.append(dictionary.get(key));
            linePointer = endTagLocation;
        }
        // append the last part of the text that doesn't have tags
        sb.append(text.substring(linePointer, text.length()));
    }
}
