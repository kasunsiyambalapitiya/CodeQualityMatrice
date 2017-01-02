/*
 *  Copyright (c) Jan 2, 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.server.SocketSecurityException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;


public class Part2 {
    //    creating a final variable for the USER_AGENT
    public final String USER_AGENT="Mozilla/5.0";

    //    Lists for saving approved and commented users on the given pull request


    Scanner user_Input= new Scanner(System.in);
    String productName; // to store the product name
    int pullRequestNo;  //to get the PR number


    ArrayList <String> apprvedUsersList= new ArrayList<String>();
    ArrayList <String> commentedUsersList = new ArrayList<String>();

    public static void main(String[] args) throws Exception{

        Part2 part2Object= new Part2();

        part2Object.sendGET();

        part2Object.printResults();

        part2Object.checkPRMergedOrNot();



    }

    //    Calls the github api and interprete the given outputs

    public void sendGET() throws Exception{

        BufferedReader bufferedReader=null;
        StringBuffer stringB= null;
        BufferedWriter bufferedWriter= null;
        JSONParser parser= new JSONParser();

        String location= System.getProperty("user.dir");        // to get the current location 

        System.out.println("Enter the product name: ");
        productName=user_Input.next();

        System.out.println("Enter the pull request no: ");
        pullRequestNo= user_Input.nextInt();

        String url="https://api.github.com/repos/wso2/"+productName+"/pulls/"+pullRequestNo+"/reviews";




        try{
            URL urlObjct= new URL(url);
            //        URLConnection urlCon= urlObjct.openConnection();         // this returns a URLConnectio object

            //        to get httpsURLConnection we need to cast in here
            HttpsURLConnection httpsURLCon= (HttpsURLConnection)urlObjct.openConnection();

            //        setting the request method
            httpsURLCon.setRequestMethod("GET");

            //         setting the request property to the request header
            httpsURLCon.setRequestProperty("User-Agent", USER_AGENT);

            // sending the accept header for requesting the correct media type as the api is still available only for preview
            httpsURLCon.setRequestProperty("Accept", "application/vnd.github.black-cat-preview+json");

            //        for getting the response code
            int responseCode= httpsURLCon.getResponseCode();

            System.out.println("the response code "+responseCode);

            bufferedReader = new BufferedReader(new InputStreamReader(httpsURLCon.getInputStream()));

            String inputLine;
            stringB= new StringBuffer();


            while ((inputLine=bufferedReader.readLine()) != null){
                stringB.append(inputLine);


            }


        }

        catch(MalformedURLException urlE){
            urlE.printStackTrace();
        }
        catch(IOException ioE){
            ioE.printStackTrace();
        }

        finally{
            //            releasing the resources
            if(bufferedReader !=null){
                bufferedReader.close();


            }
        }


        //===================Reading the output of the api request========================================
        //printing the StirngBuffer
        //        System.out.println("json returned: "+stringB.toString());
        //printing the output from the api to a json file




        try{

            bufferedWriter= new BufferedWriter(new FileWriter(location+"/output.json"));
            bufferedWriter.write(stringB.toString());   //toString() is used to convert the sequence into a string
        }

        catch(IOException ioE1){
            ioE1.printStackTrace();
        }

        finally{
            //closing the bufferedWritter object
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
        }


        //================reading the json file thus saved=======================================================
        try{

            JSONArray jsonArray= (JSONArray)parser.parse(new FileReader(location+"/output.json"));


            for (int i=0;i<jsonArray.size();i++){
                JSONObject jsonObject= (JSONObject)jsonArray.get(i);
                String state=(String)jsonObject.get("state");

                //checking the user who approved the PR
                if (state.equals("APPROVED")){
                    JSONObject jsonObjtApprvdUserDetails=(JSONObject) jsonObject.get("user");
                    String userNameApprved= (String)jsonObjtApprvdUserDetails.get("login"); 
                    apprvedUsersList.add(userNameApprved);


                }
                //checking the user who commented on the PR

                else if (state.equals("COMMENTED")){
                    JSONObject jsonObjctCommnt = (JSONObject)jsonObject.get("user");
                    String commentedUserName= (String)jsonObjctCommnt.get("login");
                    commentedUsersList.add(commentedUserName);

                }


            }

        }
        catch(Exception e){
            e.printStackTrace();

        }


    }

    //    to check whether the PR is merged or not
    public void checkPRMergedOrNot(){

        String toCheckPRMergedOrNot="https://api.github.com/repos/wso2/"+productName+"/pulls/"+pullRequestNo+"/merge";

        try{URL urlObjct= new URL(toCheckPRMergedOrNot);
        //        URLConnection urlCon= urlObjct.openConnection();         // this returns a URLConnectio object

        //        to get httpsURLConnection we need to cast in here
        HttpsURLConnection httpsURLCon= (HttpsURLConnection)urlObjct.openConnection();

        //        setting the request method
        httpsURLCon.setRequestMethod("GET");

        //         setting the request property to the request header
        httpsURLCon.setRequestProperty("User-Agent", USER_AGENT);

        //        for getting the response code
        int responseCode= httpsURLCon.getResponseCode();

        if(responseCode== 204){
            System.out.println("This PR is merged");
        }
        else if (responseCode == 404){
            System.out.println("This PR is not merged");
        }
        else{
            System.out.println("No details about the PR merge status");
        }


        }

        catch(MalformedURLException urlE){
            urlE.printStackTrace();
        }
        catch(IOException ioE){
            ioE.printStackTrace();
        }


    }



    //  printing the arraylist elements

    public void printResults(){


        System.out.println("The users who approved the Pull request");
        System.out.println(apprvedUsersList);

        System.out.println("The users who commented on the Pull request");
        System.out.println(commentedUsersList);



    }

}







