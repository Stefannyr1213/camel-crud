package com.camel.crud.route;

import com.camel.crud.dtos.ResponseDTO;
import com.camel.crud.entity.Usuario;

import com.camel.crud.exception.ExceptionUserNotExists;
import com.camel.crud.exception.ExceptionUserNotFound;
import com.camel.crud.process.Cache;
import com.camel.crud.process.ProcessDataExchangeProcessor;
import com.camel.crud.process.SetDataExchangeProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;

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
        onException(ExceptionUserNotFound.class)
                .handled(Boolean.TRUE)
                .log(LoggingLevel.ERROR,"error:${exception.message}")
                .process(exchange -> exchange.getOut().setBody(new ResponseDTO("Usuario no encontrado")))
                .end();

        onException(ExceptionUserNotExists.class)
                .handled(Boolean.TRUE)
                .log(LoggingLevel.ERROR,"error:${exception.message}")
                .process(exchange -> exchange.getOut().setBody(new ResponseDTO("Usuario no Existe")))
                .end();

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
                .to("direct:delete")
                .put("/put").type(Usuario.class).outType(Usuario.class)
                .to("direct:update");

        from("direct:update").streamCaching()
                .log("Realizando update")
                .choice()
                    .when(simple("${header.id} == null"))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                    .throwException(new ExceptionUserNotFound("Usuario no encontrado"))
                .otherwise()
                    .bean(cache,"update")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("200"))
                .log("${body}")
                .end();

        from("direct:delete")
                .log("Realizando delete usuario")
                .choice()
                    .when(simple("${header.id} == null"))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                    .throwException(new ExceptionUserNotFound("Usuario no encontrado"))
                .otherwise()
                    .bean(cache, "delete")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404"))
                .end();

        from("direct:getUsuario")
                .log("Realizando get usuario ${header.id}")
                .choice()
                    .when(simple("${header.id} == 4"))//validar si ese id ya esta en la lista
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                    .throwException(new ExceptionUserNotExists("Usuario no Existe"))
                .otherwise()
                    .bean(cache, "getUser(${header.id})")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("200"))
                .log("${body}")
                .end();

        from("direct:getUsuarios")
                        .log("Realizando get")
                        .bean(cache,"getUsers");



        from("direct:postUsuario").streamCaching()
                .log("Realizando post ${body}")
                .choice()
                    .when(simple("${body.id} == null"))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                    .throwException(new ExceptionUserNotFound("Usuario no encontrado"))
                  //  .process(exchange -> exchange.getOut().setBody(new ResponseDTO("no envio el id")))
                   // .setBody(bean(new ResponseDTO("no envio el id")))
                .otherwise()
                    .bean(cache)
                    .process(processDataExchangeProcessor)
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("200"))
                .log("${body}")
                .end();



    }

}
