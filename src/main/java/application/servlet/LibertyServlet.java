/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
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
package application.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/servlet")
public class LibertyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    	PrintWriter writer = response.getWriter();

        StringBuffer sb = new StringBuffer();
        try {
            performTask(request, response, sb);
        } catch (Throwable t) {
            t.printStackTrace(writer);
        }

        writer.write(sb.toString());
        writer.flush();
        writer.close();		
        
    }
    
    /**
     * Default action for the servlet if not overridden.
     *
     * @param req
     * @param resp
     * @param writer
     * @throws ServletException
     * @throws IOException
     */
    protected void performTask(HttpServletRequest req,
                               HttpServletResponse resp, StringBuffer sb) throws ServletException, IOException {
    	 printValues(req, sb);
    }

 
    /**
     * Print the various programmatic API values we care about.
     *
     * @param req
     * @param writer
     */
    protected void printValues(HttpServletRequest req,
                                              StringBuffer sb) {
        writeLine(sb, "getAuthType: " + req.getAuthType());
        writeLine(sb, "getRemoteUser: " + req.getRemoteUser());
        writeLine(sb, "getUserPrincipal: " + req.getUserPrincipal());

        if (req.getUserPrincipal() != null) {
            writeLine(sb, "getUserPrincipal().getName(): "
                          + req.getUserPrincipal().getName());
        }
 
        String role = req.getParameter("role");
        if (role == null) {
            writeLine(sb, "You can customize the isUserInRole call with the follow paramter: ?role=name");
        }
        writeLine(sb, "isUserInRole(" + role + "): " + req.isUserInRole(role));

        Cookie[] cookies = req.getCookies();
        writeLine(sb, "Getting cookies");
        if (cookies != null && cookies.length > 0) {
            for (int i = 0; i < cookies.length; i++) {
                writeLine(sb, "cookie: " + cookies[i].getName() + " value: "
                              + cookies[i].getValue());
            }
        }
        writeLine(sb, "getRequestURL: " + req.getRequestURL().toString());

    }

    /**
     * "Writes" the msg out to the client. This actually appends the msg
     * and a line delimiters to the running StringBuffer. This is necessary
     * because if too much data is written to the PrintWriter before the
     * logic is done, a flush() may get called and lock out changes to the
     * response.
     *
     * @param sb Running StringBuffer
     * @param msg Message to write
     */
    void writeLine(StringBuffer sb, String msg) {
        sb.append(msg + "\n");
    }


    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    

}
