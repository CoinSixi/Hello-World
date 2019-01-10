package com.citi.csvvalidator.tool;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.citi.csvvalidator.service.CsvValidatorService;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class Validator {
    private Validator() {}

    public static class Builder {
        private Validator validator;

        public Builder() {
            validator = new Validator();
        }

        public Builder setDelimiter(String delimiter) {
            validator.setDelimiter(delimiter);
            return this;
        }

        public Builder setDefinitions(Map<String, String> definitions) {
            validator.setDefinitions(definitions);
            return this;
        }

        public Builder setDefinitionOrders(List<String> definitionOrders) {
            validator.setDefinitionOrders(definitionOrders);
            return this;
        }

        public Builder setFilePath(String filePath) {
            validator.setFilePath(filePath);
            return this;
        }

        public Builder setHeaderExists(boolean headerExists) {
            validator.setHeaderExists(headerExists);
            return this;
        }

        public Validator build() {
            return validator;
        }

        public Validator buildWithConfigurations() {
            validator = this.setDelimiter(Configurations.getDelimiter())
                    .setDefinitions(Configurations.getDefinitions())
                    .setDefinitionOrders(Configurations.getDefinitionOrders())
                    .setFilePath(Configurations.getFilePath())
                    .setHeaderExists(Configurations.isHeaderExist())
                    .build();
            if (!Configurations.isHeaderExist() && Configurations.getDefinitions() == null) {
                validator.columnSizeChecker = new HashMap<>();
                validator.columnSizeCounter = new HashMap<>();
            }
            return validator;
        }
    }
    private boolean isHeaderExists;
    private int headerCount;
    private String delimiter;
    private Set<String> headers;
    private Map<String, String> definitions;
    private List<String> definitionOrders;
    private String filePath;

    private Map<Integer, Integer> columnSizeChecker;
    private Map<Integer, List<Long>> columnSizeCounter;

    public void setHeaderExists(boolean headerExists) {
        isHeaderExists = headerExists;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    private void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    private void setDefinitions(Map<String, String> definitions) {
        this.definitions = definitions;
    }

    private void setDefinitionOrders(List<String> definitionOrders) {
        this.definitionOrders = definitionOrders;
    }

    private boolean valueValidate(CSVRecord record) {
        boolean result = true;
        if (definitions != null) {
            for (int i = 0; i < record.size() && i < definitions.size(); i ++) {  //avoid out of index
                String pattern = definitions.get(definitionOrders.get(i));
                if (pattern.isEmpty()) {
                    continue;
                }
                boolean isMatch = Pattern.matches(pattern, record.get(i));
                if (! isMatch) {
                    result = false;
                    CsvValidatorService.setReport("Value not matched at line[" + record.getRecordNumber() + "], column[" + i + "] where value:" + record.get(i) + ", pattern:" + pattern + "\n");
                    System.out.println("Value not matched at line[" + record.getRecordNumber() + "], column[" + i + "] where value:" + record.get(i) + ", pattern:" + pattern);
                }
            }
        }

        return result;
    }

    private void checkMissingColumns(CSVRecord record) {
    	CsvValidatorService.setReport("Column(s): ");
        System.out.print("Column(s): ");
        for (String column : headers) {
            if (record.toMap() != null) {
                if (!record.toMap().containsKey(column)) {
                	CsvValidatorService.setReport("[" + column + "] ");
                    System.out.print("[" + column + "] ");
                }
            } else {

            }
        }
        CsvValidatorService.setReport("not found!" + "\n");
        System.out.println("not found!");
    }

    private void checkRedundantValues(CSVRecord record) {
    	CsvValidatorService.setReport("Value(s): ");
        System.out.print("Value(s): ");
        for (int i = headers.size(); i < record.size(); i ++) {
        	CsvValidatorService.setReport("[" + record.get(i) + "] ");
            System.out.print("[" + record.get(i) + "] ");
        }
        CsvValidatorService.setReport("is(are) redundant!" + "\n");
        System.out.println("is(are) redundant!");
    }

    private boolean sizeValidate(CSVRecord record) {
        boolean result = true;
        if (isHeaderExists) {
            if (headerCount > record.size()) {
            	CsvValidatorService.setReport("Missing data at line[" + record.getRecordNumber() + "]!" + "\n");
                System.out.println("Missing data at line[" + record.getRecordNumber() + "]!");
                checkMissingColumns(record);
                result = false;
            } else if (record.toMap().size() < record.size()) {
            	CsvValidatorService.setReport("Redundant data at line[" + record.getRecordNumber() + "]!" + "\n");
                System.out.println("Redundant data at line[" + record.getRecordNumber() + "]!");
                checkRedundantValues(record);
                result = false;
            }
        } else if(definitions != null) {
            headerCount = definitions.size();
            headers = definitions.keySet();
            if (headerCount > record.size()) {
            	CsvValidatorService.setReport("Missing data at line[" + record.getRecordNumber() + "]!" + "\n");
                System.out.println("Missing data at line[" + record.getRecordNumber() + "]!");
                checkMissingColumns(record);
                result = false;
            } else if (headerCount < record.size())  {
            	CsvValidatorService.setReport("Redundant data at line[" + record.getRecordNumber() + "]!" + "\n");
                System.out.println("Redundant data at line[" + record.getRecordNumber() + "]!");
                checkRedundantValues(record);
                result = false;
            }
        } else {
            Integer count = columnSizeChecker.get(record.size());
            List<Long> counter = columnSizeCounter.get(record.size());
            if (count == null) {
                count = 1;
                counter = new ArrayList<>();
                counter.add(record.getRecordNumber());
            }
            else {
                count++;
                counter.add(record.getRecordNumber());
            }
            columnSizeChecker.put(record.size(), count);
            columnSizeCounter.put(record.size(), counter);
        }
        return result;
    }

    private void validateLinePipelines(CSVRecord record) {
        boolean isValid = sizeValidate(record) & valueValidate(record);
        if (! isValid) {
            if (isHeaderExists) {

            }
            CsvValidatorService.setReport("---------------------------------------------------------------------------------------------------------------------" + "\n");
            System.out.println("---------------------------------------------------------------------------------------------------------------------");
        }
    }

    public void validate() {
        CSVParser records = lazyLoad();
        if (records == null) {
        	CsvValidatorService.setReport("Empty file content, exiting..." + "\n");
            System.out.println("Empty file content, exiting...");
            return;
        }
        setHeaderMap(records);
        Iterator<CSVRecord> iterator = records.iterator();
        CsvValidatorService.setReport("Validating..." + "\n");
        System.out.println("Validating...");
        while (iterator.hasNext()) {
            CSVRecord record = iterator.next();
            validateLinePipelines(record);
        }
        finalCheck();
        CsvValidatorService.setReport("Validate successful." + "\n");
        System.out.println("Validate successful.");
    }

    private CSVParser lazyLoad() {
    	CsvValidatorService.setReport("Loading file..." + "\n");
        System.out.println("Loading file...");
        try {
            Reader in = new FileReader(filePath);
            // TODO
            CSVFormat temp = CSVFormat.DEFAULT.withDelimiter(delimiter.charAt(0));
            if (isHeaderExists)
                temp = temp.withFirstRecordAsHeader();
            else if (definitions != null)
                temp = temp.withHeader(definitions.keySet().toArray(new String[definitions.size()]));

            return temp.parse(in);
        } catch (FileNotFoundException e) {
        	CsvValidatorService.setReport("File not Found! File path: " + filePath + "\n");
            System.err.println("File not Found! File path: " + filePath);
            e.printStackTrace();
            return null;
            //System.exit(1);
        } catch (IOException e) {
        	CsvValidatorService.setReport("IOException while reading file." + "\n");
            System.err.println("IOException while reading file.");
            e.printStackTrace();
        }
        return null;
    }

    private void finalCheck() {
        if (columnSizeChecker != null) {
            //TODO
            Integer max = 0;
            Integer maxIndex = 0;
            for (Integer each : columnSizeChecker.keySet()) {
                if (columnSizeChecker.get(each) > max) {
                    max = columnSizeChecker.get(each);
                    maxIndex = each;
                }
            }
            columnSizeCounter.remove(maxIndex);
            for (Integer each : columnSizeCounter.keySet()) {
                for (Long column :columnSizeCounter.get(each)){
                	CsvValidatorService.setReport("Exception column size:" + each + ", and line number is(are):" + column + "\n");
                	System.out.println("Exception column size:" + each + ", and line number is(are):" + column);
                }
                   
            }
        }
    }

    private void setHeaderMap(CSVParser records) {
        if (records == null)
            return;
        if (isHeaderExists) {
            headerCount = records.getHeaderMap().size();
            headers = records.getHeaderMap().keySet();
        }
    }

}
