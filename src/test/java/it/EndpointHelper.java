/*******************************************************************************
  * Copyright (c) 2017 IBM Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/ 

package it;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Base64;

import org.apache.cxf.helpers.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class EndpointHelper {
	
	public void testEndpoint(String endpoint, String expectedOutput) throws ClientProtocolException, IOException {
	    String port = System.getProperty("liberty.test.port");
	    String war = System.getProperty("war.context");
	    System.out.println("Port: " + port + "\nWar: " + war);
        String url = "http://localhost:" + port + "/" + war + endpoint;
        System.out.println("Testing " + url);
        if (expectedOutput.equalsIgnoreCase("Set-Cookie: LtpaToken2")){
            testCredentials(url, expectedOutput);
            testNoCredentials(url);
            testWithWrongPassword(url);
            testWrongUserId(url, expectedOutput);
        }
        else {
            testUnsecure(url, expectedOutput);
        }
	}
	
	//test without credentials
	public void testUnsecure(String url, String expectedOutput) throws ClientProtocolException, IOException {
	    System.out.println("Test without credentials started");
	    HttpResponse response = sendRequest(url, "GET", null, null);
	    testWithResponse(response, expectedOutput);
        System.out.println("Test without credentials finished");
	}
	
	//test secure endpoint with credentials
	public void testCredentials(String url, String expectedOutput) throws ClientProtocolException, IOException {
	    System.out.println("Test secure endpoint with credentials started");
	    //login with username and password to get authenticated
	    HttpResponse response = sendRequest(url, "GET", "user1", "password");
	    //From redirect response, obtain LtpaToken2 cookie
	    String cookieString=testWithRedirectResponse(response, expectedOutput);
	    //Access the url again with the cookie to confirm the user has the role assigned.
	    accessWithCookie(url, cookieString, 200 );
        System.out.println("Test secure endpoint with credentials finished");
	}
	
	//test secure endpoint with no credentials
	public void testNoCredentials(String url) throws ClientProtocolException, IOException {
	    System.out.println("Test secure endpoint with no credentials started");
	    HttpResponse response = sendRequest(url, "GET", null, null);
	    testWithRedirectResponse(response, "login.html");
        System.out.println("Test secure endpoint with no credentials finished");
	}
	
	//test secure endpoint with incorrect credentials
	public void testWithWrongPassword(String url) throws ClientProtocolException, IOException {
	    System.out.println("Test secure endpoint with incorrect credentials started");
	    HttpResponse response = sendRequest(url, "GET", "nutAUser", "notAPassword");
	    testWithRedirectResponse(response,"loginError.html");
        System.out.println("Test secure endpoint with incorrect credentials finished");
	}
	
	//test secure endpoint with valid but unprivileged credentials
	public void testWrongUserId(String url, String expectedOutput) throws ClientProtocolException, IOException {
	    System.out.println("Test secure endpoint with valid but unprivileged credentials started");
	    //login with username and password to get authenticated
	    HttpResponse response = sendRequest(url, "GET", "user2","password");
	   //From redirect response, obtain LtpaToken2 cookie
	    String cookieString = testWithRedirectResponse(response,"Set-Cookie: LtpaToken2");
	    //Access the url again with the cookie to confirm the user does not have the role
	    accessWithCookie(url, cookieString, 403 );
        System.out.println("Test secure endpoint with valid but unprivileged credentials finished");
	}
    
	public void testWithResponse(HttpResponse response, String expectedOutput) throws UnsupportedOperationException, IOException
	{
        int responseCode = response.getStatusLine().getStatusCode();
        assertTrue("Incorrect response code: " + responseCode,
                   responseCode == 200);
        String responseString = IOUtils.toString(response.getEntity().getContent());
        assertTrue("Incorrect response, response is: " + responseString + "Expected: " + expectedOutput, responseString.contains(expectedOutput));
	}
	
	public String testWithRedirectResponse(HttpResponse response, String expectedOutput) throws UnsupportedOperationException, IOException
	{
        int responseCode = response.getStatusLine().getStatusCode();
        assertTrue("Incorrect response code: " + responseCode,
                   responseCode == 302);
        assertTrue("Incorrect response, response is: " + response + "Expected: " + expectedOutput, response.toString().contains(expectedOutput));
        return getSSOCookie(response.toString());
	}
	
	public void testWithoutResponse(int expectedResponseCode, HttpResponse response)
	{
	    int responseCode = response.getStatusLine().getStatusCode();
        assertTrue("Incorrect response code (expected " + expectedResponseCode + "): " + responseCode,
                   responseCode == expectedResponseCode);
	}
	
    //send HTTP request with credentials
    public HttpResponse sendRequest(String url, String requestType, String username, String password) throws ClientProtocolException, IOException {
        if ((username!=null) && (password!=null)){
        	String url1 = url + "/j_security_check?j_username="+username+"&j_password="+password;
            HttpPost httppost = new HttpPost(url1);
            System.out.println("Executing secure request " + httppost.getRequestLine());
            HttpClient httpclient = HttpClientBuilder.create().build();
            HttpResponse response = httpclient.execute(httppost);
            return response;
        }
        else
        {
            HttpPost httppost = new HttpPost(url);
            System.out.println("Executing request " + httppost.getRequestLine());
            HttpClient httpclient = HttpClientBuilder.create().build();
            HttpResponse response = httpclient.execute(httppost);
            return response;
        }

    }
    
    String getSSOCookie(String response) {
    	String cookieString = null; 
    	
    	if (response.contains("LtpaToken2")) {
    		int start = response.indexOf("LtpaToken2=");
    		cookieString = response.substring(start + "LtpaToken2=".length());
    		int end = cookieString.indexOf(";");
    		cookieString = cookieString.substring(0, end);
    	}
    	System.out.println("cookeString=" + cookieString);
    	return cookieString;
    }
    
    
    public String accessWithCookie(String url, String cookie, int expectedStatusCode) {
    	HttpResponse response = null;
    	String ssoCookieName="LtpaToken2";
    	HttpGet getMethod = new HttpGet(url);
    	getMethod.setHeader("Cookie",ssoCookieName + "=" + cookie);
    	HttpClient httpclient = HttpClientBuilder.create().build();
    	try {
    	  response = httpclient.execute(getMethod);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	if (response !=null)
    		return response.toString();
    	else
    		return null;
    }
	
}