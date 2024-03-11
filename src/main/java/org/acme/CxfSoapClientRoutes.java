package org.acme;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.tempuri.CalculatorSoap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
public class CxfSoapClientRoutes extends RouteBuilder {

    @Inject
    SoapConfiguration configuration;
       
    @Override
    public void configure() throws Exception {
        rest()
            .post("/add")
            .routeId("RouteRest")
            .consumes("application/json")
            .produces("application/json")
            .to("direct:consumeSoapService");

        from("direct:consumeSoapService")
            .routeId("RouteSoap") 
            //.setBody(constant(new Object[] {10, 20}))
            .log("[REQUEST] Received POST with parameters: ${body}")

            .unmarshal().json(JsonLibrary.Jackson, RequestDao.class)
            .log("[REQUEST] Data transformation: ${body}")
            .setBody(simple("${body.getOperation}"))           

            /*
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {                 
                    String jsonBody = exchange.getIn().getBody(String.class);
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(jsonBody);                  
                    int param1 = jsonNode.get("param1").asInt();
                    int param2 = jsonNode.get("param2").asInt();
                    Object[] params = new Object[]{param1, param2};
                    exchange.getMessage().setBody(params);
                }                
            })
            */

            .setHeader(CxfConstants.OPERATION_NAME, constant(configuration.operationName()))
            .setHeader(CxfConstants.OPERATION_NAMESPACE, constant(configuration.operationNamespace()))
                    
            .process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {                 
                    Map<String, Object> requestContext = new HashMap<String, Object>();
                    HTTPClientPolicy clientPolicy = new HTTPClientPolicy();
                    clientPolicy.setConnectionTimeout(configuration.connectionTimeout());
                    clientPolicy.setReceiveTimeout(configuration.receiveTimeout());
                    requestContext.put(HTTPClientPolicy.class.getName(), clientPolicy);
                    exchange.getIn().setHeader(Client.REQUEST_CONTEXT , requestContext);
                }
            })
            
            .to("cxf:bean:calculatorSoapEndpoint?dataFormat=POJO")
            //.to("cxf://http://www.dneonline.com/calculator.asmx?wsdlURL=wsdl/CalculatorService.wsdl&dataFormat=POJO&serviceClass=org.tempuri.CalculatorSoap")           
            
            .log("[RESPONSE] Received POST with parameters: ${body}")
            .convertBodyTo(String.class)
            .marshal().json(JsonLibrary.Jackson)
            .setBody(simple("{\"message\":${body}}"))
            .log("[RESPONSE] Data transformation: ${body}");            
    }

    @Produces
    @SessionScoped
    @Named("calculatorSoapEndpoint")
    CxfEndpoint calculatorSoapEndpoint() throws Exception {
        final CxfEndpoint result = new CxfEndpoint();
        result.setServiceClass(CalculatorSoap.class);
        //result.setContinuationTimeout(1);
        //result.setPollingConsumerBlockTimeout(1);        
        result.setAddress(configuration.operationAddress());
        result.setWsdlURL(configuration.operationWsdlURL());
        
        // Configuración del cliente CXF
        //Client client = ClientProxy.getClient(result.createClient());
        //HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
        //HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        //httpClientPolicy.setConnectionTimeout(5000); // Tiempo de espera de conexión en milisegundos
        //httpClientPolicy.setReceiveTimeout(5000);    // Tiempo de espera para recibir en milisegundos
        //httpClientPolicy.setAllowChunking(false);    // Deshabilitar el uso de transferencia de datos fragmentados
        //httpConduit.setClient(httpClientPolicy);     
        return result;
    }
}
