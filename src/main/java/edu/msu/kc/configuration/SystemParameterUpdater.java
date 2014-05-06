package edu.msu.kc.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.coreservice.impl.parameter.ParameterBo;

public class SystemParameterUpdater {

	public static void main(String[] args) {
		
        File dataFile = getDataFile();
        while (!dataFile.exists()) {
        	dataFile = getDataFile();
        }

		SystemParameterUpdater parameterUpdater = new SystemParameterUpdater();
		try {            
			parameterUpdater.generateUpdateStatements(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void generateUpdateStatements(File dataFile) throws IOException{
		CSVFormat format = CSVFormat.RFC4180.withHeader().withDelimiter(',').withIgnoreSurroundingSpaces(true).withIgnoreEmptyLines(true);
         
        CSVParser parser = CSVParser.parse(dataFile, format);
         
        for(CSVRecord record : parser){        	
        	ParameterBo parameter = new ParameterBo();
            
        	String namespace = escapeSqlSpecialCharacter(record.get("Namespace"));
        	parameter.setNamespaceCode(namespace);
            
        	String name = escapeSqlSpecialCharacter(record.get("System Parameter Name"));
            parameter.setName(name);
            
        	String value = escapeSqlSpecialCharacter(record.get("MSU Value"));
            parameter.setValue(value);
            
        	String description = escapeSqlSpecialCharacter(record.get("System Parameter Description"));
            parameter.setDescription(description);
            
            //String componentCode = escapeSqlSpecialCharacter(record.get("Component Code"));
            //componentCode = StringUtils.isBlank(componentCode) ? "Document" : componentCode;
            parameter.setComponentCode("Document");
            
            if(StringUtils.isBlank(parameter.getNamespaceCode()) || StringUtils.isBlank(parameter.getName())){
            	continue;
            }
            
            System.out.println(this.generateUpdateStatement(parameter));
        }
        parser.close();
	}
	
	public String generateUpdateStatement(ParameterBo parameter){
		String statement = "UPDATE KRCR_PARM_T SET ";
		
		statement += "VAL='" +  parameter.getValue() + "' \n";
		statement += "WHERE NMSPC_CD='" + parameter.getNamespaceCode() + "' ";
		statement += "AND CMPNT_CD='" + parameter.getComponentCode() + "' ";
		statement += "AND PARM_NM='" + parameter.getName() + "';\n";
		
		return statement;
	}
	
	public String generateSelectStatement(ParameterBo parameter){
		String statement = "SELECT NMSPC_CD,CMPNT_CD,PARM_NM FROM KRCR_PARM_T \n";
		statement += "WHERE NMSPC_CD='" + parameter.getNamespaceCode() + "' ";
		statement += "AND PARM_NM='" + parameter.getName() + "';";
		
		return statement;
	}
	
	public String escapeSqlSpecialCharacter(String value){
		String newValue = StringEscapeUtils.escapeSql(value);
		
		newValue = StringUtils.replace(newValue, "\n", "<br/>");
		newValue = StringUtils.replace(newValue, "\r", "<br/>");
		
		return newValue;		
	}
	
    public static File getDataFile() {
        System.out.print("Enter data file location:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String inputString = null;

        try {
            inputString = br.readLine();
        } catch (IOException ioe) {
            System.out.println("IO error trying to read input!");
            System.exit(1);
        } 

        File dataFile = new File(inputString);
        if (!dataFile.exists()) {
            System.out.println("The data file doesn't exist: " + inputString);
        }
        
        return dataFile;
    }
}
