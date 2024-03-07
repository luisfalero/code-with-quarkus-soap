package org.acme;

//import org.apache.camel.Exchange;
//import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.component.cxf.jaxws.CxfEndpoint;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.tempuri.CalculatorSoap;

//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

@ApplicationScoped
public class CxfSoapClientRoutes extends RouteBuilder {
   
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

            .setHeader(CxfConstants.OPERATION_NAME, constant("Add"))
            .setHeader(CxfConstants.OPERATION_NAMESPACE, constant("http://tempuri.org/"))
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
    CxfEndpoint calculatorSoapEndpoint() {
        final CxfEndpoint result = new CxfEndpoint();
        result.setServiceClass(CalculatorSoap.class);
        result.setAddress("http://www.dneonline.com/calculator.asmx");
        result.setWsdlURL("wsdl/CalculatorService.wsdl");
        return result;
    }  
}
