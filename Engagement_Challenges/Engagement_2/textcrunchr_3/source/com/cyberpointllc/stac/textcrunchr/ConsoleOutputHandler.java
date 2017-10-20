package com.cyberpointllc.stac.textcrunchr;

import java.util.List;
import java.util.Map;
import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.template.TemplateEngine;

public class ConsoleOutputHandler extends OutputHandler {

    public void do_conclude() {
        do_concludeHelper();
    }

    private void do_concludeHelper() {
        TemplateEngine tp = new  TemplateEngine("    {{name}}\n{{output}}");
        Map<String, String> templateMap = new  HashMap<String, String>();
        for (String filename : sortedFiles) {
            String path = namesToPaths.get(filename);
            System.out.println("        File " + filename + ": ");
            List<TCResult> sampleResults = results.get(path);
            for (TCResult result : sampleResults) {
                templateMap.put("name", result.getName());
                templateMap.put("output", result.getValue());
                String output = tp.replaceTags(templateMap);
                System.out.println(output);
                templateMap.clear();
            }
            System.out.println("________________________");
        }
    }
}
