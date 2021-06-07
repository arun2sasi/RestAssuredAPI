
package testcase;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import static io.restassured.RestAssured.given;
import java.io.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class EmailAPI 
{

@Test(priority=1)

	//This is the driver function which would read the property file and run the associated functions for the automated tests
	public void driverModule() throws IOException
	{
		//With this approach, we can integrate the automated test cases continuously and add it to the property file 
		//With this approach,we can leverage the reporting functionality of testNG and can view the results of tests getting automated
		//We can have multiple modules grouped logically for each of API functionality. Accordingly naming conventions of keys can be imposed
		
		try {
			// Reading the Property files and checking if the naming conventions of the key is TC
			// The property file should have keys like TCNumber and value for the key is the function name
			// For instance TC1=validateDomainName
			
			Properties prop = new Properties();
			String propFileName = "Filepath.properties";
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			if (inputStream != null)
			{
				prop.load(inputStream);
				Set<String> keys = prop.stringPropertyNames();
				// For each of the key values, check for the substring TC and call the associated function
				for (String key:keys)
				{
					if(key.toString().contains("TC"))
					{
						String functiontocall=prop.getProperty(key);
						Method methodtocall=getClass().getMethod(functiontocall);  
						methodtocall.invoke(getClass().newInstance());
						               
					}
										
				}	
			
			}

			else 
			{
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	//readExcel() function is used to retrieve the work sheet which has the test data based on the information provided in the property file
@BeforeSuite
	public Sheet readExcel() throws IOException 
	{
		//Key values are:
		//filepath- Path where the test data file is stored
		//filename- Name of the xlsx file to be processed. Please note that the current implementation supports only xlsx file type
		//sheetname- Name of the sheet where data is stored


		//The below code is used to retrieve the filepath,filename and sheetname from Property file

		Properties prop = new Properties();
		String propFileName = "Filepath.properties";
		InputStream propstream = getClass().getClassLoader().getResourceAsStream(propFileName);
		prop.load(propstream);
//		File file = new File(prop.getProperty("filepath") + "\\" + prop.getProperty("filename"));
		File file = new File(System.getProperty("user.dir") + "/TestData/" + prop.getProperty("filename"));
		
		// Create an object of FileInputStream class to read excel file
		FileInputStream inputStream = new FileInputStream(file);

		// For xlsx file then create object of XSSFWorkbook class
		Workbook wbtoread = new XSSFWorkbook(inputStream);
		String sheet= prop.getProperty("sheetname");
		Sheet sheettoread = wbtoread.getSheet(sheet);
		wbtoread.close();
		//Returns the sheet to the calling function
		return sheettoread;

	}
	
		//This is the function for the test case where in the retrieved domain name for a valid email address is asserted

	
	public void validateDomainName() throws IOException {

		// Calling the function readExcel to retrieve the test sheet for processing	
		Sheet emailsheet = readExcel();
		
		// Row number 1 is retrieved as the expectation is that test data for test case n is at row number n 
		Row r = emailsheet.getRow(1);
		
		int columncount = r.getLastCellNum();
		int count = 0;
		List<String> list = new ArrayList<String>();
		
		// For increasing the readability in test report
		Reporter.log ("TestCase 1: " );
		
		try
		{
			
			// Test data are available from the third column in test data sheet. Each of the email ids to be tested are in colmns 3,5,7 etc
			// Tester can add many test data for each test case. The first data in Column3, second in Column 5 , third in Column 7 etc 
			// The below logic retrieves each of the associated test data and compares it with the expected value provided  
			
			for (int i = 2; i < columncount; i = i + 2) {
				
				// Row number 1 is retrieved as the expectation is that test data for test case n is at row number n 
				Row row = emailsheet.getRow(1);
				RestAssured.defaultParser = Parser.JSON;
				
				// send the API request for each of the test data provided
				Response res = given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).when()
						.get("https://api.eva.pingutil.com/email?email=" + "" + row.getCell(i).getStringCellValue()).then()
						.extract().response();
				ResponseBody resbody = res.getBody();

				//	Add the response result to a list.This can be used for other purposes like call to other APIs or write to external files etc
				
				list.add(count, resbody.jsonPath().get("data.domain").toString());
				
				//	Verify if the expected value and the retrieved value matches 
				Assert.assertEquals(row.getCell(i+1).getStringCellValue(), list.get(count));
				
                //	Print the test data number and the result for the same
				
				Reporter.log("Test Data  " + (count+1));
				Reporter.log("Domain name is  " + list.get(count));
				count++;

			}
		}
		catch(Exception e)
		{
			Reporter.log(e.getMessage());
		}

	}
	
	//This is the function for the test case where status "success" is received for valid email address

	public void validateSuccessStatus() throws IOException {


		// Calling the function readExcel to retrieve the test sheet for processing
		Sheet emailsheet = readExcel();
		int count = 0;
		
		// Row number 3 is retrieved as the expectation is that test data for test case n is at row number n 
		Row r = emailsheet.getRow(3);
		int columncount = r.getLastCellNum();
		
		Reporter.log ("TestCase 3: " );
		List<String> list = new ArrayList<String>();

		try
		{
			// Test data are available from the third column in test data sheet. Each of the email ids to be tested are in colmns 3,5,7 etc
			// Tester can add many test data for each test case. The first data in Column3, second in Column 5 , third in Column 7 etc 
			// The below logic retrieves each of the associated test data and compares it with the expected value provided  
						

			for (int i = 2; i < columncount; i = i + 2) {
				Row row = emailsheet.getRow(3);
				RestAssured.defaultParser = Parser.JSON;
				
				// send the API request for each of the test data provided
				
				Response res = given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).when()
						.get("https://api.eva.pingutil.com/email?email=" + "" + row.getCell(i).getStringCellValue()).then()
						.extract().response();
				ResponseBody resbody = res.getBody();

				//Add the response result to a list.This can be used for other purposes like call to other APIs or write to external files etc
                list.add(count, resbody.jsonPath().get("status").toString());
				
				//value of the response header is validated against the expected value
				Assert.assertEquals(row.getCell(i+1).getStringCellValue(), list.get(count));
				
				Reporter.log("Test Data  " + (count+1));
				Reporter.log("status is  " + list.get(count));
				count++;

			}

		}
		catch(Exception e)
		{
			Reporter.log(e.getMessage());
		}
	}
//This is the function for the test case where the status is retrieved as Failure for incorrect domain

	public void validateFailureStatus() throws IOException {
		// Calling the function readExcel to retrieve the test sheet for processing
		Sheet emailsheet = readExcel();
		int count = 0;
		// Row number 4 is retrieved as the expectation is that test data for test case n is at row number n 
		Row r = emailsheet.getRow(4);
		int columncount = r.getLastCellNum();
		Reporter.log ("TestCase 4: " );
		List<String> list = new ArrayList<String>();
		try
		{
			// Test data are available from the third column in test data sheet. Each of the email ids to be tested are in colmns 3,5,7 etc
			// Tester can add many test data for each test case. The first data in Column3, second in Column 5 , third in Column 7 etc 
			// The below logic retrieves each of the associated test data and compares it with the expected value provided  
			
			for (int i = 2; i < columncount; i = i + 2) {
				Row row = emailsheet.getRow(4);
				RestAssured.defaultParser = Parser.JSON;
				
				// send the API request for each of the test data provided
				Response res = given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).when()
						.get("https://api.eva.pingutil.com/email?email=" + "" + row.getCell(i).getStringCellValue()).then()
						.extract().response();
				ResponseBody resbody = res.getBody();
				
				//Add the response result to a list.This can be used for other purposes like call to other APIs or write to external files etc
				list.add(count, resbody.jsonPath().get("status").toString());
				
				//Verify if the expected value and the retrieved value matches
				Assert.assertEquals(row.getCell(i+1).getStringCellValue(), list.get(count));
				
				//Print the test data number and the result for the same
				Reporter.log("Test Data  " + (count+1));
				Reporter.log("status is  " + list.get(count));
				count++;

			}
		}
		catch(Exception e)
		{
			Reporter.log(e.getMessage());
		}

	}
	//This is the function to verify if the email sent in the request is processed and passed correctly in the response
	public void validatePassedRequestInput() throws IOException {


		// Calling the function readExcel to retrieve the test sheet for processing	
		Sheet emailsheet = readExcel();
		int count = 0;
		
		// Row number 6 is retrieved as the expectation is that test data for test case n is at row number n 
		Row r = emailsheet.getRow(6);
		
		int columncount = r.getLastCellNum();
		List<String> list = new ArrayList<String>();
		Reporter.log ("TestCase 6: " );
		
			// The below logic retrieves each of the associated test data and compares it with the expected value provided  
			// Tester can add many test data for each test case. The first data in Column3, second in Column 5 , third in Column 7 etc
			// Test data are available from the third column in test data sheet. Each of the email ids to be tested are in colmns 3,5,7 etc
			
			for (int i = 2; i < columncount; i = i + 2) {
				Row row = emailsheet.getRow(6);
				RestAssured.defaultParser = Parser.JSON;
				
				//send the API request for each of the test data provided
				Response res = given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).when()
						.get("https://api.eva.pingutil.com/email?email=" + "" + row.getCell(i).getStringCellValue()).then()
						.extract().response();
				ResponseBody resbody = res.getBody();
				//Add the response result to a list.This can be used for other purposes like call to other APIs or write to external files etc
				list.add(count, resbody.jsonPath().get("data.email_address").toString());

				Assert.assertEquals(row.getCell(i+1).getStringCellValue(), list.get(count));
				Reporter.log("Test Data  " + (count+1));
				Reporter.log("Input Email is  " + list.get(count));
				count++;
			}
		
		
		
					

	}

}

