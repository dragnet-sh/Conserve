package com.bbudhathoki.sync.requesthandler;


import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiHandler extends RequestHandler {
    private String parseApiRoot = "http://ec2-18-220-200-115.us-east-2.compute.amazonaws.com:80/parse/";
    private String applicationId = "47f916f7005d19ddd78a6be6b4bdba3ca49615a0";
    private String masterKey = "NLI214vDqkoFTJSTtIE2xLqMme6Evd0kA1BbJ20S";
    private String classPath = "classes/";

    private String preAuditClassName = "PreAuditMain";
    private String auditClassName = "AuditMain";
    private String zoneClassName = "ZoneMain";
    private String roomClassName = "RoomMain";
    private String plugLoadClassName = "PlugLoadMain";
    private String plugLoad = "PlugLoad";

    public String getApplicationId() {
        return this.applicationId;
    }

    public String getParseApiRoot() {
        return this.parseApiRoot;
    }

    public String getMasterKey() {
        return this.masterKey;
    }

    public String getClassPath() {
        return this.classPath;
    }

    public boolean createParseObject(JSONObject data, String classname) {
        Boolean returnData;
        String userCreateUrl = this.getParseApiRoot() + this.getClassPath() + classname;

        try {
            RequestHandler requestHandler = new RequestHandler(
                    data, getApplicationId(), getMasterKey(), userCreateUrl, "post", classname);
            requestHandler.run();
            returnData = true;

        } catch (IOException e) {
            System.out.println("Exception Occurred.");
            returnData = false;
        }

        return returnData;
    }

    public void getParseObject(JSONObject data, String classname, String objectId) {
        String userCreateUrl = this.getParseApiRoot() + this.getClassPath() + classname + "/" + objectId;

        try {
            RequestHandler requestHandler = new RequestHandler(
                    data, getApplicationId(), getMasterKey(), userCreateUrl, "get", classname);
            requestHandler.run();

        } catch (IOException e) {
            System.out.println("Exception Occurred.");
        }
    }


    public void queryPlugload() {

        try {
            String param = URLEncoder.encode("where", "UTF-8");
            String plugloadGetUrl = this.getParseApiRoot() + this.getClassPath() + "PlugLoad?where=";

            JSONObject outer = new JSONObject();
            JSONObject inner = new JSONObject();
            inner.put("data.style_type", "Reach-in");
            inner.put("data.total_volume", 17.89);
            outer.put("where", inner);

            String finalUrl = plugloadGetUrl + inner;

            JSONObject response = get(finalUrl);
            System.out.println(response.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private JSONObject get(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "OkHttp Headers.java")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Parse-Application-Id", getApplicationId())
                .addHeader("X-Parse-REST-API-Key", getMasterKey())
                .build();

        try {
            Response response = client.newCall(request).execute();
            JSONObject jsonResponse = new JSONObject();
            try {
                JSONObject data = new JSONObject(response.body().string());
                jsonResponse.put("status", "success");
                jsonResponse.put("response", data);
            } catch (Exception e) {
            }
            return jsonResponse;
        } catch (Exception e) {
            JSONObject exceptionResponse = new JSONObject();
            try {
                exceptionResponse.put("status", "fail");
                exceptionResponse.put("message", "Exception occurred while executing get.");
            } catch (Exception ex) {
            }
            return exceptionResponse;
        }
    }


}

