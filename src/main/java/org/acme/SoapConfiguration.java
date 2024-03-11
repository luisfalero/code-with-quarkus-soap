package org.acme;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "soap")
public interface SoapConfiguration {

    String operationName();
    String operationNamespace();
    String operationAddress();
    String operationWsdlURL();
    int connectionTimeout();
    int receiveTimeout();
    
}
