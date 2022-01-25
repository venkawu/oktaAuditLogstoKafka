# oktaAuditLogstoKafka
Description
This app will pull okta logs data by calling the okta logs api - https://purpleid.okta.com/api/v1/logs - and then write the data into kafka using spring cloud streams

Okta Api Documentations
https://developer.okta.com/docs/reference/api/system-log/

Application Properties
spring.cloud.stream.kafka.binder.configuration.ssl.keystore.password=
spring.cloud.stream.kafka.binder.configuration.ssl.truststore.password=
oktarequest.accessToken=


Security
The certificate used for two way SSL authentication to Kafka is in the security folder. You will need to update the path to the keystore and tuststore location in the application properties file before running.

Logging
update the path specified in the property name section of the logback-spring.xml file to correspond to your environment. cloud
