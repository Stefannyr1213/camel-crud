package com.camel.crud.route;

import com.camel.crud.dtos.ResponseDTO;
import com.camel.crud.entity.Usuario;
import com.camel.crud.process.Cache;
import com.camel.crud.process.ProcessDataExchangeProcessor;
import com.camel.crud.process.SetDataExchangeProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CrudRoute extends RouteBuilder {
    private JacksonDataFormat jsonUsuario= new JacksonDataFormat(Usuario.class);
    @Autowired
    private SetDataExchangeProcessor setDataExchangeProcessor;
    @Autowired
    private ProcessDataExchangeProcessor processDataExchangeProcessor;
    @Autowired
    private Cache cache;
    @Override

    public void configure() throws Exception {

        restConfiguration() //se empieza a configurar un componente rest para hacer uso de endpoints a partir de REST
                .bindingMode(RestBindingMode.auto)
                .component("jetty") //componente a utilizar //jetty: servidor
                .enableCORS(true) //permite el acceso de recursos crusados http
                .port(10000) //puerto con el que va a trabajar
                .corsHeaderProperty("Access-Control-Allow-Origin", "*") //habilitar las ip de las cuales quiero consumir separadas por coma. * es para todas
                .corsHeaderProperty("Access-Control-Allow-Headers", "*");   //permite definir que tipo de headers pueden invocarse. Accept,Content-Type

        rest("usuario")
                .post("/post").description("Post usuario").type(Usuario.class).outType(ResponseDTO.class)
                .to("direct:postUsuario")
                .get("/").outType(Usuario[].class)
                .to("direct:getUsuarios")
                .get("/{id}").outType(Usuario.class)
                .to("direct:getUsuario")
                .delete("/{id}").outType(String.class)
                .to("direct:delete");

        from("direct:delete")
                .log("Realizando delete usuario")
                //.setBody(simple("${header.id}"))
                .bean(cache, "delete");

        from("direct:getUsuario")
                .log("Realizando get usuario ${header.id}")
              //  .setBody(simple("${header.id}"))
                .bean(cache, "getUser(${header.id})")
                .log("${body}");
        from("direct:getUsuarios")
                        .log("Realizando get")
                        .bean(cache,"getUsers");

        from("direct:postUsuario").streamCaching()
                .log("Realizando post ${body}")
                .bean(cache)
                .process(processDataExchangeProcessor)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("200"))
                .end();



    }

}
