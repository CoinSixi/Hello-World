package com.citi.csvvalidator.tool;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import com.citi.csvvalidator.service.CsvValidatorService;

public class Configurations {
 
    private static Map<String, String> definitions = null;
    private static List<String> definitionOrders = null;
    private static boolean headerExist;
    private static String delimiter;
    private static String filePath;


    public static String getFilePath() {
        return filePath;
    }

    public static Map<String, String> getDefinitions() {
        return definitions;
    }

    public static List<String> getDefinitionOrders() {
        return definitionOrders;
    }

    public static boolean isHeaderExist() {
        return headerExist;
    }

    public static String getDelimiter() {
        return delimiter;
    }

    public static boolean START_UP(String configure, String schema) throws Exception {
    	CsvValidatorService.setReport("Loading configurations..." + "\n");
        System.out.println("Loading configurations...");
        Properties properties = new Properties();
        properties.load(new StringReader(configure));
        if(!checkDefaultProperties(properties,schema)){
        	return false;
        }
        printProperties(properties);
        return true;
    }

    private static void printProperties(Properties properties) {
        properties.list(System.out);
    }

    private static boolean checkDefaultProperties(Properties properties, String schema) {
        if ((filePath = properties.getProperty("filePath", null)) == null || filePath.isEmpty()) {
        	CsvValidatorService.setReport("File path not set!" + "\n");
            System.err.println("File path not set!");
            return false;
        }
        Boolean manual = Optional.of(properties).map(p -> p.getProperty("manualDefined")).filter(p -> !p.isEmpty()).map(Boolean::valueOf).orElse(Boolean.FALSE);
        if (manual) {
            loadSchemaDefinitions(schema);
        }
        headerExist = Optional.of(properties).map(p -> p.getProperty("headerExist")).filter(p -> !p.isEmpty()).map(Boolean::valueOf).orElse(Boolean.FALSE);
        delimiter = Optional.of(properties).map(p -> p.getProperty("delimiter").trim()).filter(p -> !p.isEmpty()).orElse(",");
        return true;
    }

    private static void loadSchemaDefinitions(String schema) {
        
        definitions = new LinkedHashMap<>();
        definitionOrders = new LinkedList<>();
        String[] ss = schema.split("\\n");
        for(int i=0; i<ss.length; i++){
            if (ss[i] != null) {
                Stream.of(ss[i]).filter(l -> !l.isEmpty()).map(l -> l.split(":", 2)).peek(vars -> {
                    definitions.put(vars[0].trim(), vars[1].trim());
                    definitionOrders.add(vars[0].trim());
                }).count();     //need a final operation otherwise peek would not execute.
            } else
                break;
        }
    }
}
