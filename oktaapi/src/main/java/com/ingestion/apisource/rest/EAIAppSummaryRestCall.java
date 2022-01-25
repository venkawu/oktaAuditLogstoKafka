package com.ingestion.apisource.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

public class EAIAppSummaryRestCall {
	
	private Logger logger = LoggerFactory.getILoggerFactory().getLogger("EAIAppSummaryRestCall");

	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	
	public EAIAppSummaryRestCall(){
		
	}
	public EAIAppSummaryResponse callEAIAppSummary(EAIAppSummaryRequest request, String url) throws Exception {
		
		  
		  RestTemplate restTemplate = new RestTemplate();
		  //String url = "http://appinventoryws-dev.app.wtcdev1.paas.fedex.com/api/v1/ApplicationSummary/";
		  
		  
		  EAIAppSummaryResponse response = restTemplate.postForObject(url, request, EAIAppSummaryResponse.class);
		  
			logger.info("Returned from calling appinventoryws - ApplicationSummary. ");
			
			return response;
	
	}

}
