package com.hackcambridge.cognitive;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Endpoint {

    HttpClient client;
    HttpPost request;

    public Endpoint(String host) {
        this.request = new HttpPost(host);
        this.request.setHeader("Content-Type", "application/octet-stream");
        this.request.setHeader("Ocp-Apim-Subscription-Key", "b540d97a382c422b972096232eab64e4");
        this.client = new DefaultHttpClient();
    }

    public Endpoint() {
        this("https://westeurope.api.cognitive.microsoft.com/vision/v1.0/ocr");
    }

    public JSONObject post(File file) throws IOException, JSONException {
        return post(new FileEntity(file));
    }

    public JSONObject post(ByteBuffer buffer) throws IOException, JSONException {
        return post(new ByteArrayEntity(buffer.array()));
    }

    public JSONObject post(byte[] buffer) throws IOException, JSONException {
        return post(new ByteArrayEntity(buffer));
    }

    public JSONObject post(HttpEntity entity) throws IOException, JSONException {
        this.request.setEntity(entity);
        HttpEntity response = client.execute(this.request).getEntity();
        return new JSONObject(EntityUtils.toString(response));
    }

    /*public static void main(String[] args) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("https://westeurope.api.cognitive.microsoft.com/vision/v1.0/ocr");
        post.setHeader("Content-Type", "application/octet-stream");
        HttpEntity entity = new FileEntity(new File("C:/Users/ajbon/CloudStation/Uni work/Part IB/Other/Hackathons/Receipt1.jpg"));
        post.setEntity(entity);
        //HttpResponse response = client.execute(post);
        //JSONObject result = new JSONObject(EntityUtils.toString(response.getEntity()));
        //System.out.println(result.toString(4));
    }*/

}
