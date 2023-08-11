package com.qa.base.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.qa.base.utils.Databaseutils;

/**
 * ExcelUtils class that gets test data from excel file to be used for driving tests.
 * 
 *
 */
public class Xslutils
{
	private XSSFSheet excelWSheet;
	private XSSFWorkbook excelWBook;
	private XSSFCell cell;
	private Databaseutils datadbutils = new Databaseutils();
	public HashMap<String, String> testData = new HashMap<String, String>();	

	/**
	 * Get WPR data based on a Facets like query
	 * 
	 * @param	dataMap	Existing data map
	 * @param 	methodName	Test method name
	 */
	public static void cacheWprDataFromFacetsLikeQuery(Map<String, String> dataMap, String methodName) {
		Object[][] dataColumnArray = null;
		Object[][] dataTableArray = null;
		String wprQueryWithParameterValues;

		if ( dataMap.get("FACETS_LIKE_INNER_QUERY") != null && !"".equals(dataMap.get("FACETS_LIKE_INNER_QUERY")) ) {
			
			dataColumnArray = new Databaseutils().getFacetsColumnArray(dataMap.get("FACETS_LIKE_INNER_QUERY").toString());
			dataTableArray = new Databaseutils().getFacetsTableArray(dataMap.get("FACETS_LIKE_INNER_QUERY").toString());
			debugQueryPrint(dataMap.get("FACETS_LIKE_INNER_QUERY"), methodName);
		}
		if ( dataMap.get("WPR_LIKE_QUERY") != null && !"".equals(dataMap.get("WPR_LIKE_QUERY")) ) {
			wprQueryWithParameterValues = inputParameterValuesLikeQuery(dataMap.get("WPR_LIKE_QUERY").toString(), dataColumnArray, dataTableArray);
			dataColumnArray = new Databaseutils().getWprColumnArray(wprQueryWithParameterValues);
			dataTableArray = new Databaseutils().getWprTableArray(wprQueryWithParameterValues);
			debugQueryPrint(dataMap.get("WPR_LIKE_QUERY"), methodName);
		}

	}
	
	public Map<String, Map<String, String>> cacheAllExcelData(String xlsPath) {
		Map<String, Map<String, String>> excelDataMap = null;

		String sheetName = System.getenv("ENVNAME");

		if (sheetName == null || "".equals(sheetName)) {
			sheetName = "Sheet1";
		}

		excelDataMap = getAllExcelDataMap(getColumnArray(xlsPath, sheetName), getTableArray(xlsPath, sheetName));

		return excelDataMap;
	}
	
	private static Map<String, Map<String, String>> getAllExcelDataMap(Object[][] columnArray,
			Object[][] testDataArray) {
		Map<String, Map<String, String>> dataMap = new HashMap<String, Map<String, String>>();

		int testMethodIndex = 0;
		for (testMethodIndex = 0; testMethodIndex < columnArray.length; testMethodIndex++) {
			if ("TestMethod".equals(columnArray[0][testMethodIndex])) {
				break;
			}
		}
		
		if (testDataArray != null) {
			for (int i = 0; i < testDataArray.length; i++) {
				Map<String, String> rowMap = new HashMap<String, String>();
				for (int j = 0; j < columnArray[0].length; j++) {
					rowMap.put(columnArray[0][j].toString(), testDataArray[i][j].toString());
				}
				dataMap.put((String) testDataArray[i][testMethodIndex], rowMap);
			}
		}
		return dataMap;
	}

	private static void debugQueryPrint(String query, String testMethodName) {
		System.out.println("Debug: " + testMethodName + "Query: ---------------"); 
		System.out.println(query);
	}
	
	/**
	 * Get WPR data based on a Facets IN query
	 * 
	 * @param	dataMap	Existing data map
	 * @param 	methodName	Test method name
	 */
	public static void cacheWprDataFromFacetsInQuery(Map<String, String> dataMap, String methodName) {
		Object[][] dataColumnArray = null;
		Object[][] dataTableArray = null;
		String wprQueryWithParameterValues;

		if ( dataMap.get("FACETS_IN_INNER_QUERY") != null && !"".equals(dataMap.get("FACETS_IN_INNER_QUERY")) ) {
			dataColumnArray = new Databaseutils().getFacetsColumnArray(dataMap.get("FACETS_IN_INNER_QUERY").toString());
			dataTableArray = new Databaseutils().getFacetsTableArray(dataMap.get("FACETS_IN_INNER_QUERY").toString());
			debugQueryPrint(dataMap.get("FACETS_IN_INNER_QUERY"), methodName);

		}
		if ( dataMap.get("WPR_IN_QUERY") != null && !"".equals(dataMap.get("WPR_IN_QUERY")) ) {
			wprQueryWithParameterValues = inputParameterValuesInQuery(dataMap.get("WPR_IN_QUERY").toString(), dataColumnArray, dataTableArray);
			dataColumnArray = new Databaseutils().getWprColumnArray(wprQueryWithParameterValues);
			dataTableArray = new Databaseutils().getWprTableArray(wprQueryWithParameterValues);
			debugQueryPrint(dataMap.get("WPR_LIKE_QUERY"), methodName);
		}

	}
	
	/**
	 * Insert parameter values in the LIKE clause of the query
	 * 
	 * @param sqlQuery	SQL query
	 * @param dataColumnArray	Columnn name array
	 * @param dataTableArray	Data array
	 * @return	sqlQuery	with parameters in a like qwuery
	 */
	private static String inputParameterValuesLikeQuery( String sqlQuery, Object[][] dataColumnArray, Object[][] dataTableArray) {
		for (int i = 0; i < dataColumnArray[0].length; i++) {
			sqlQuery = sqlQuery.replace(dataColumnArray[0][i].toString(), dataTableArray[0][i].toString());
		}
		return sqlQuery;
	}
	
	/**
	 * Insert parameters values in the IN clause of the query
	 * 
	 * @param sqlQuery	SQL query
	 * @param dataColumnArray	Columnn name array
	 * @param dataTableArray	Data array
	 * @return	sqlQuery	with parameters in a like qwuery
	 */
	private static String inputParameterValuesInQuery( String sqlQuery, Object[][] dataColumnArray, Object[][] dataTableArray) {
		StringBuilder inPrameter = new StringBuilder();
		for (int i = 0; i < dataTableArray.length; i++) {
			inPrameter.append("'").append(dataTableArray[i][0].toString()).append("'");
			if (i != dataTableArray.length - 1) {
				inPrameter.append(", ");
			}
		}
		sqlQuery = sqlQuery.replace(dataColumnArray[0][0].toString(), inPrameter.toString());
		return sqlQuery;
	}
	

	/**
	 * Get all test data for a test method and store in the test data map
	 * 
	 * @param columnArray	Column name array
	 * @param testDataArray	Test data array
	 * @param methodName	Test method name
	 */
	private static void getAllDBTestData(Map<String, String> dataMap, Object[][] columnArray, Object[][] testDataArray, String methodName) {

		if (testDataArray != null){
			for (int i=0; i<testDataArray.length; i++) {
				for (int j=0; j< columnArray[0].length; j++) {
					if (columnArray[0][j] != null && testDataArray[i][j] != null) {
						dataMap.put(columnArray[0][j].toString(), testDataArray[i][j].toString());
					}
				}
			}
		}
	}
	
	/**
	 * Get all test data for a test method and store in the test data map
	 * 
	 * @param columnArray	Column name array
	 * @param testDataArray	Test data array
	 */
	private static Map<String, String> getAllTestData(String testMethodName, Object[][] columnArray, Object[][] testDataArray) {
		Object[][] dataColumnArray = null;
		Object[][] dataTableArray = null;
		Map<String, String> dataMap = new HashMap<String, String>();

		if (testDataArray != null) {
			for (int i=0; i<testDataArray.length; i++) {
				if ( testMethodName.equals( testDataArray[i][1] ) ) {
					
					for (int j=0; j< columnArray[0].length; j++) {
						dataMap.put(columnArray[0][j].toString(), testDataArray[i][j].toString());
					}
					if ( dataMap.get("FACETS_SQL_INPUT") != null && !"".equals(dataMap.get("FACETS_SQL_INPUT")) ) {
						dataColumnArray = new Databaseutils().getFacetsColumnArray(dataMap.get("FACETS_SQL_INPUT").toString());
						dataTableArray = new Databaseutils().getFacetsTableArray(dataMap.get("FACETS_SQL_INPUT").toString());
						getAllDBTestData(dataMap, dataColumnArray, dataTableArray, dataMap.get("TestMethod").toString());
					}
					if ( dataMap.get("FACETS_SQL_EXPECTED") != null && !"".equals(dataMap.get("FACETS_SQL_EXPECTED")) ) {
						dataColumnArray = new Databaseutils().getFacetsColumnArray(dataMap.get("FACETS_SQL_EXPECTED").toString());
						dataTableArray = new Databaseutils().getFacetsTableArray(dataMap.get("FACETS_SQL_EXPECTED").toString());
						getAllDBTestData(dataMap, dataColumnArray, dataTableArray, dataMap.get("TestMethod").toString());
					}
					if ( dataMap.get("WPR_SQL_INPUT") != null && !"".equals(dataMap.get("WPR_SQL_INPUT")) ) {
						dataColumnArray = new Databaseutils().getWprColumnArray(dataMap.get("WPR_SQL_INPUT").toString());
						dataTableArray = new Databaseutils().getWprTableArray(dataMap.get("WPR_SQL_INPUT").toString());
						getAllDBTestData(dataMap, dataColumnArray, dataTableArray, dataMap.get("TestMethod").toString());
					}
					if ( dataMap.get("WPR_SQL_EXPECTED") != null && !"".equals(dataMap.get("WPR_SQL_EXPECTED")) ) {
						dataColumnArray = new Databaseutils().getWprColumnArray(dataMap.get("WPR_SQL_EXPECTED").toString());
						dataTableArray = new Databaseutils().getWprTableArray(dataMap.get("WPR_SQL_EXPECTED").toString());
						getAllDBTestData(dataMap, dataColumnArray, dataTableArray, dataMap.get("TestMethod").toString());
					}
					if (dataMap.get("UPDATE_QUERY")!= null && !"".equals(dataMap.get("UPDATE_QUERY").trim())) {
						int result = new Databaseutils().executeUpdate("wpr", dataMap.get("UPDATE_QUERY"));
						dataMap.put("UPDATE_QUERY_RESULT", String.valueOf(result));
						getAllDBTestData(dataMap, dataColumnArray, dataTableArray, dataMap.get("TestMethod").toString());
					}

				}
			}
		}
		return dataMap;
	}


	private static List<Map<String, String>> getAllExcelData(Object[][] columnArray, Object[][] testDataArray) {
		Object[][] dataColumnArray = null;
		Object[][] dataTableArray = null;
		Map<String, String> dataMap = new HashMap<String, String>();
		List<Map<String, String>> dataMapList = new ArrayList<Map<String, String>>();

		if (testDataArray != null) {
			for (int i=0; i<testDataArray.length; i++) {
					
					for (int j=0; j< columnArray[0].length; j++) {
						dataMap.put(columnArray[0][j].toString(), testDataArray[i][j].toString());
					}
					dataMapList.add(dataMap);
			}
		}
		return dataMapList;
	}

	/**
	 * Assemble the test case's populated data fields from the data spreadsheet 
	 * 
	 * @param xlsPath	Excel file path
	 * @param testMethodName	Test method name
	 * @return	excelDataMap	Excel data in a map
	 */
	public  Map<String, String> getTestMethodData(String xlsPath, String testMethodName) 
	{
		Map <String, String> excelData = null;
		System.out.println("TestMethodName: " + testMethodName);

		String sheetName = System.getenv("ENVNAME");

		if (sheetName == null || "".equals(sheetName)) {
			sheetName = "Sheet1";
		}
		
		excelData = getAllTestData(testMethodName, getColumnArray(xlsPath, sheetName), getTableArray(xlsPath, sheetName));
		
		return excelData;
	}
		
	public  List<Map <String, String>> getAllExcelDataOnly(String xlsPath) 
	{
		List<Map <String, String>> excelDataMapList = null;

		String sheetName = System.getenv("ENVNAME");

		if (sheetName == null || "".equals(sheetName)) {
			sheetName = "Sheet1";
		}
		
		excelDataMapList = getAllExcelData(getColumnArray(xlsPath, sheetName), getTableArray(xlsPath, sheetName));
		
		return excelDataMapList;
	}


	/**
	 * Returns the cell value as a String
	 * 
	 * @param rowNum	Row number
	 * @param colNum	Column Number
	 * @return cell value as a string
	 */
	public String getCellData(int rowNum, int colNum) 
	{
		try
		{
			cell = excelWSheet.getRow(rowNum).getCell(colNum);
			String cellData = null;
			if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
				cellData = cell.getStringCellValue();
			}
			else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) 
			{
				cellData = String.valueOf(cell.getNumericCellValue());
			}
			else if (cell.getCellType() == org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK)
			{
				cellData = "";
			}
			return cellData;
		} 
		catch (Exception e)
		{
			return "";
		}
	}

	/**
	 * Get Excel data as an array
	 * 
	 * @param filePath	Excel file path
	 * @param sheetName	Excel sheet name
	 * @return	Object array of data in the table
	 */
	public  Object[][] getTableArray(String filePath, String sheetName) {   
		String[][] tableArray = null;
		int startRow = 1;
		int startCol = 0;
		int ci,cj;
		int totalRows;
		int totalCols;
	
		try {
			FileInputStream excelFile = new FileInputStream(filePath);			
			excelWBook = new XSSFWorkbook(excelFile);
			excelWSheet = excelWBook.getSheet(sheetName);
			if (excelWSheet == null) {
				excelWSheet = excelWBook.getSheet("Sheet1");
			}
			totalRows = excelWSheet.getPhysicalNumberOfRows()-1;
			totalCols = excelWSheet.getRow(0).getPhysicalNumberOfCells()-1;
			tableArray=new String[totalRows][totalCols+1];
			ci=0;
			for (int i=startRow;i<=totalRows;i++, ci++) {           	   
				cj=0;
				for (int j=startCol;j<=totalCols;j++, cj++){
					//System.out.println(ci+":"+cj+"->"+i+":"+j);
					if(getCellData(i,j) != null && getCellData(i,j).trim()!=""){
						tableArray[ci][cj]=getCellData(i,j);
					}
					else {
						tableArray[ci][cj]="";
					}
				}
			}
		}catch (FileNotFoundException e){
			System.out.println("Could not read the Excel sheet, file not found at path: " + filePath + " ,sheet: " + sheetName);
		}catch (IOException e){
			System.out.println("Could not read the Excel sheet, io exception: " + filePath + " ,sheet: " + sheetName);
		}
		return(tableArray);
	}


	/**
	 * Get columns names array
	 * 
	 * @param filePath	Excel file path
	 * @param sheetName	Excel sheet name
	 * @return	Object array of column names
	 */
	public  Object[][] getColumnArray(String filePath, String sheetName) {   
		String[][] columnArray = null;
		int ci;
		int totalRows;
		int totalCols;
	
		try {
			FileInputStream excelFile = new FileInputStream(filePath);			
			excelWBook = new XSSFWorkbook(excelFile);
			excelWSheet = excelWBook.getSheet(sheetName);
			if (excelWSheet == null) {
				excelWSheet = excelWBook.getSheet("Sheet1");
			}
			totalRows = excelWSheet.getPhysicalNumberOfRows()-1;
			totalCols = excelWSheet.getRow(0).getPhysicalNumberOfCells()-1;
			columnArray=new String[totalRows][totalCols+1];
			ci=0;

			for (int j=0;j<=totalCols;j++){
				if(getCellData(ci,j).trim()!=""){
					columnArray[ci][j]=getCellData(ci,j);
				}
			}
		}catch (FileNotFoundException e){
			System.out.println("Could not read the Excel sheet, file not found at path: " + filePath + " ,sheet: " + sheetName);
		}catch (IOException e){
			System.out.println("Could not read the Excel sheet, io exception: " + filePath + " ,sheet: " + sheetName);
		}
		return(columnArray);
	}
	
	/**
	 * Returns a count of the number of columns
	 * 
	 * @return column count as an integer
	 */
	public  int getColumnCount()  
	{
		int rowNum = 0;
		int colNum = 0;
		int colCt = 0;
		try 
		{
			while (getCellData(rowNum, colNum) == null || getCellData(rowNum, colNum).isEmpty())
			{
				colCt++;
				colNum++;
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return colCt;
	}

	public List<HashMap<String, String>> loadAllExcelData(String xlsPath) {
		List<HashMap<String, String>> excelDataMap = null;
		String sheetName = System.getenv("ENVNAME");
		if (sheetName == null || "".equals(sheetName)) {
			sheetName = "Sheet1";
		}

		excelDataMap = getAllExcelDataList(getColumnArray(xlsPath, sheetName), getTableArray(xlsPath, sheetName));
		return excelDataMap;
	}
	
	private static List<HashMap<String, String>> getAllExcelDataList(Object[][] columnArray, Object[][] testDataArray) {
		List<HashMap<String, String>> dataMap = new ArrayList<HashMap<String, String>>();

		int testMethodIndex;
		for(testMethodIndex = 0; testMethodIndex < columnArray.length && !"TestMethod".equals(columnArray[0][testMethodIndex]); ++testMethodIndex) {
		}

//		System.out.println("testMethodIndex = " + testMethodIndex);
		if (testDataArray != null) {
//			System.out.println("**************************");
//			System.out.println("*   READING EXCEL FILE   *");
//			System.out.println("**************************");
			for(int i = 0; i < testDataArray.length; ++i) {
				HashMap<String, String> rowMap = new HashMap<String, String>();

				for(int j = 0; j < columnArray[0].length; ++j) {
					rowMap.put(columnArray[0][j].toString(), testDataArray[i][j].toString());
//					System.out.print(columnArray[0][j] + " = " + testDataArray[i][j].toString() + "\t");
				}
//				System.out.println();
				dataMap.add(rowMap);
			}
//			System.out.println("**************************");
		}

		return dataMap;
	}
	
    public Object[][] getDataForCurrentTest(List<HashMap<String, String>> testDataMap, String testMethodName) {
		int newSize = 0;
		int dataCt = testDataMap.size();
		Object[][] tempData = new Object[dataCt][1];

//		System.out.println("\nCURRENT METHOD =" + testMethodName);
		for (int idx = 0; idx < dataCt; idx++) {
			if (testDataMap.get(idx).get("TestMethod").contentEquals(testMethodName)) {								
				tempData[newSize++][0] = testDataMap.get(idx);				
//				System.out.println(tempData[newSize-1][0]);
			}
		}
//		System.out.println("data set count =" + tempData.length + "\n");
		
    	return Arrays.copyOf(tempData, newSize);
    }

}
