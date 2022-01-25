package com.ingestion.apisource;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.ingestion.apisource.rest.okta.OktaRestCall;

@EnableBinding(Source.class)
@RefreshScope
@Configuration
@EnableScheduling
public class ApiSender{
	
	private static final Logger logger = LoggerFactory.getLogger(ApiSender.class);
	private final static Logger errorLogger = LoggerFactory.getLogger("oktareadererrors");
	
	@Autowired
	private Source source;
	
	@Autowired
	private ConfigurableApplicationContext context;
	
	@Value("${oktarequest.url}")
	private String startUrl;
	
	@Value("${oktarequest.accessToken}")
    private String accessToken;
	
	@Value("${oktarequest.proxy_host}")
    private String proxyHost; 
	
	@Value("${oktarequest.proxy_port}")
    private int proxyPort;
	
	@Value("${oktarequest.retries}")
    private int retries;
	
	@Value("${oktarequest.clientResponseCodes}")
    private String clientErrorCodes;
	
	@Value("${oktarequest.serverErrorCodes}")
    private String serverErrorCodes;
	
	@Value("${oktarequest.delayTillDataAvail}")
    private String delayTillDataAvail;
	
	@Value("${oktarequest.readTimeout}")
    private String readTimeout;
	
	@Value("${oktarequest.delayBetweenRequests}")
    private String delayBetweenRequests;
	
	@Value("${oktarequest.failedNextUriFilePath}")
	private String failedNextUriFilePath;
	
	@Value("${oktarequest.delayBetweenErrors}")
	private String delayBetweenErrors; 
	
	private Proxy proxy;

	@Scheduled(fixedDelayString = "${schedule.interval}")
	public void sendEvents(){
		String[] clientErrorCodeList = clientErrorCodes.split(",");
		String[] serverErrorCodeList = serverErrorCodes.split(",");
		String strLine = new String();
		String lastFailedUrl = new String();
		
		try {
            BufferedReader br = new BufferedReader(new FileReader(failedNextUriFilePath));
            while (br.ready())
            {
               strLine = br.readLine();
               lastFailedUrl=strLine;
            }
            br.close();
       } catch (FileNotFoundException e) {
           System.err.println("File not found");
   
       } catch (IOException e) {
           System.err.println("Unable to read the file.");
       }
		
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
		OktaRestCall restCall = new OktaRestCall(proxy, retries, accessToken, source, clientErrorCodeList, serverErrorCodeList, Integer.parseInt(delayTillDataAvail), Integer.parseInt(delayBetweenRequests), Integer.parseInt(readTimeout), Integer.parseInt(delayBetweenErrors));
		
		try {
			if (!lastFailedUrl.isEmpty()) {
				logger.info("_____RESTARTING____");
				logger.debug(lastFailedUrl);
				restCall.callOkta(lastFailedUrl);
			}else {
				logger.debug(startUrl);
				restCall.callOkta(startUrl);
			}
			
		} catch (Exception e) {
			errorLogger.error("Exception encountered in okta api app:" + e.getMessage());
		}
	}
}
