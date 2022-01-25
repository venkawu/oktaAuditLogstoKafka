package com.ingestion.apisource.rest.okta;

import java.io.IOException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.MessageBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.cloud.stream.messaging.Source;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OktaRestCall {
	
	private final static Logger logger = LoggerFactory.getILoggerFactory().getLogger("oktarestcall");
	private final static Logger errorLogger = LoggerFactory.getLogger("oktareadererrors");
	private final static Logger nextUrlLogger = LoggerFactory.getLogger("oktareadernextUrl");
	private final OkHttpClient okHttpClient = new OkHttpClient();
	private final Proxy proxy;  
	private final int retries;
	private final String accessToken; 
    private final Source source;
    private final String[] clientErrorCodeList;   
    private final String[] serverErrorCodeList;                        
    private final int delayTillDataAvail;
    private final int delayBetweenRequests;
    private final int readTimeout; 
    private final int delayBetweenErrors; 
	
	public OktaRestCall(Proxy proxy, int retries, String accessToken, Source source, String[] clientErrorCodeList, String[] serverErrorCodeList, int delayTillDataAvail, int delayBetweenRequests, int readTimeout, int delayBetweenErrors){
		this.proxy = proxy; 
    	this.retries = retries;
    	this.accessToken = accessToken; 
    	this.source = source;
    	this.clientErrorCodeList = clientErrorCodeList;
    	this.serverErrorCodeList = serverErrorCodeList;
    	this.delayTillDataAvail = delayTillDataAvail; 
    	this.delayBetweenRequests = delayBetweenRequests;
    	this.readTimeout = readTimeout; 
    	this.delayBetweenErrors = delayBetweenErrors; 
	}
	
	public void callOkta(String url) throws Exception {
		String nextUrl=new String();
		do{
			logger.info("Url: "+url);
			nextUrl = getRequest(url, 0);
			if(!nextUrl.isEmpty()) {
	        	url = nextUrl;
	        } 
			else {
				url=null;
			}
		}while(url!=null);	
	}
	
	public String getRequest(String url, int retry){
		String nextUrl = null; 
    	OkHttpClient client = okHttpClient.newBuilder()
    							.proxy(proxy)
    							.readTimeout(readTimeout, TimeUnit.MILLISECONDS)
    							.build();

    	
		Request request = new Request.Builder()
				.url(url)
				.header("Accept", "application/json")
				.header("Authorization", "SSWS " + accessToken)
				.build();

		Response response;
		
		try {
			logger.info("Delay "+delayBetweenRequests+ "ms till next request");
			TimeUnit.MILLISECONDS.sleep(delayBetweenRequests);
			logger.info("Making GET request to URL");
			response = client.newCall(request).execute();
			String responseString = response.body().string(); 
			
			//Delay if we catch up to okta write rate and nextUrl returns an empty response. Give okta enough time to write latest logs to the api
			if (responseString.equals("[]")) {
				logger.info("Response empty. Delay for "+ (delayTillDataAvail/60000)+ "min before retry"); 
				TimeUnit.MILLISECONDS.sleep(delayTillDataAvail);
			}
			
			//delay till X-Rate-Limit-Reset time and re-try if code 429 encountered 
			if(response.code()==429) {
				String rateLimitReset = response.header("X-Rate-Limit-Reset");
				long resetTime = Long.parseLong(rateLimitReset);
				long timeNow = (Instant.now().toEpochMilli())/1000;
				long timeTillRateLimitReset = resetTime - timeNow; 
				
				logger.info("Delay for " + (timeTillRateLimitReset+2) + " seconds");
				TimeUnit.SECONDS.sleep(timeTillRateLimitReset+2);
				retry++;
	            
	            if(retry <= retries) {
	            	logger.info("Retry "+ (retry) + " after response code 429 encountered");  
	            	nextUrl = getRequest(url, retry); 
	            	return nextUrl;
	            }else {
	            	errorLogger.error("429 continuously encountered, unable to pull data from " + url);
	            	nextUrlLogger.error(url);
	            	return null; 
	            }
			}else if (Arrays.asList(serverErrorCodeList).contains(Integer.toString(response.code()))) { 
				retry++;
				int sleepTime = delayBetweenErrors * retry; 
				logger.warn(response.code()+ " response - Server Side Error. Retrying in "+(sleepTime/1000)+ "s");
		        TimeUnit.MILLISECONDS.sleep(sleepTime);
	            
	            if(retry <= retries) {
	            	logger.info("Retry "+ retry + " after response code "+ response.code() + " encountered");  
	            	nextUrl = getRequest(url, retry); 
	            	return nextUrl;
	            }else {
	            	errorLogger.error(response.code()+" continuously encountered, unable to pull data from " + url);
	            	nextUrlLogger.error(url);
	            	return null;
	            }
			}else if (Arrays.asList(clientErrorCodeList).contains(Integer.toString(response.code()))) {
				logger.info("Client side error encountered, response code: "+response.code());
				errorLogger.error("Client side error: "+responseString);			
				nextUrlLogger.error(url);
				System.exit(0);   //no retries on client side error - OVO alerts and we investigate
			}
			
			logger.info(response.message() +" Response");
			logger.info("ResponseCode:" + response.code());
			logger.debug("Okta Log Message to be returned to be published = " + responseString);
			parseResponse(responseString);
			
			Headers responseHeaders = response.headers();
			List<String> values = responseHeaders.values("link");
			
			for(String value:values){
	              if(value.contains("next")){
	            	  Integer greaterSignIndex = value.indexOf(">");
	              	  nextUrl = value.substring(1, greaterSignIndex);
	              	  logger.info("*Next URL attained");
	              	  logger.debug(nextUrl);
	              	  break;
	              }
			}
		} catch (SocketTimeoutException ex) { 
			try {
				retry++;
				int sleepTime = delayBetweenErrors * retry; //increase delay time geometrically - T, 2T, 3T
				
	            logger.warn(ex + " when trying to retrieve data. Retrying in "+(sleepTime/1000)+ "s");
	            TimeUnit.MILLISECONDS.sleep(sleepTime);

	            if(retry <= retries) {
	            	logger.info("Retry "+ retry + " for getting data associated with url");  
	            	nextUrl = getRequest(url, retry); 	 
	            }else {
	            	errorLogger.error("Socket Connection Error: Timed out "+ retries +" times retrieveing data from " + url);
	            	nextUrlLogger.error(url);
	            	return null;
	            }
			} catch (InterruptedException e) {
				errorLogger.error(e.getMessage());
				nextUrlLogger.error(url);
			}
			
        } catch (IOException e) {
			errorLogger.error(e.getMessage());
			nextUrlLogger.error(url);
		} catch (InterruptedException e) {
			errorLogger.error(e.getMessage());
			nextUrlLogger.error(url);
		}
		return nextUrl;
    }
	
	public void parseResponse(String jsonString) {
        JsonElement jsonTree = JsonParser.parseString(jsonString);
        
        if(jsonTree.isJsonArray()) {
        	logger.info("parsing contentUri response"); 
        	logger.info("*sending msges to Kafka*");
            JsonArray contents = jsonTree.getAsJsonArray();
            for (JsonElement content : contents) {
            	JsonObject contentObj = content.getAsJsonObject();
            	sendMessageToKafka(contentObj.toString());
            }
            logger.info("*All msgs sent*");
        }  
    }
	
	public void sendMessageToKafka(String msg) {
		try {
			Message<String> message = MessageBuilder.withPayload(msg).build();
			logger.debug("Payload of the Message to be sent = " + message.getPayload().toString());
			source.output().send(message);
		} catch (MessageHandlingException ex) {
			errorLogger.error("Message Handling Exception: " + ex.getMessage());
		} catch (RecordTooLargeException recordToLarge) {
			recordToLarge.printStackTrace();
			errorLogger.error("Record too large exception: " + recordToLarge.getMessage());
		} catch (Exception e) {
			errorLogger.error(e.getMessage());
		}
	}
}
