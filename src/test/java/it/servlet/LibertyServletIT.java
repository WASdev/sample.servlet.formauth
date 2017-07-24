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

package it.servlet;

import it.EndpointHelper;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

public class LibertyServletIT extends EndpointHelper {

    @Test
    public void testDeployment() throws ClientProtocolException, IOException {
    	//In this test, login is performed by adding "/j_security_check?j_username="+username+"&j_password="+password" to URL
    	//If authenticated, LtpaToken2 is returned.   If not authenticated, user will be redirected to loginError.html
    	testEndpoint("/servlet", "Set-Cookie: LtpaToken2");
    }
}