package com.camel.crud.route;

import com.camel.crud.dtos.ResponseDTO;
import com.camel.crud.entity.Usuario;

import com.camel.crud.exception.ExceptionUserNotExists;
import com.camel.crud.exception.ExceptionUserNotFound;
import com.camel.crud.exception.ExceptionUserRepeat;
import com.camel.crud.process.Cache;
import com.camel.crud.process.ProcessDataExchangeProcessor;
import com.camel.crud.process.SetDataExchangeProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;

import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class CrudRoute extends RouteBuilder {


    private JacksonDataFormat jsonDTO= new JacksonDataFormat(ResponseDTO.class);
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
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                .process(exchange ->  {
                        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, "400");
                        exchange.getOut().setBody(new ResponseDTO("Usuario no encontrado"));})
                .log("${body}").marshal(jsonDTO)
                .log("Despues de unmarsharl ${body}")
                .end();

        onException(ExceptionUserNotExists.class)
                .handled(Boolean.TRUE)
                .log(LoggingLevel.ERROR,"error:${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                .process(exchange -> {
                    exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, "400");
                    exchange.getOut().setBody(new ResponseDTO("Usuario no Existe"));})
                .log("${body}").marshal(jsonDTO)
                .log("Despues de unmarsharl ${body}")
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
                .log("Realizando update ${body.userId}")
                .setHeader("userId",simple("${body.userId}"))
                .setProperty("bodyOriginal",body()) //backup body de ingreso
                .setProperty("userIdBody",simple("${body.userId}")) //coloco user id
                .choice()
                    .when(simple("${body.userId} == null"))
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                        //.setBody(constant(new ResponseDTO("Usuario no encontrado")))
                        .process((exchange) -> {
                            String id = exchange.getProperty("userIdBody", String.class);
                            exchange.getIn().setBody(new ResponseDTO("Usuario no encontrado"));
                        })
                        .marshal(jsonDTO)
                        .throwException(new ExceptionUserNotFound("Usuario no encontrado"))//segundo filtro: buscar si el id existe
                    .when(simple("${body.userId} != null"))
                        .to("sql:{{selectById}}:#${body.userId}{{sqlDatasource}}{{sqlDatasourceParameter}}")
                        .log("Body despues de consulta ${body}")
                        .choice()
                            //verifica si la consulta a la bd es nula, si es asi, puedo guardarlo en bd
                            .when(body().isNotNull())
                            .process((exchange) -> {
                                String id = exchange.getProperty("userIdBody", String.class);
                                exchange.getIn().setBody(exchange.getProperty("bodyOriginal"));
                            })
                            .log(" Body despues de process ${body}")
                //.otherwise()
                    //.bean(cache,"update")
                    .setHeader("userId",simple("${body.userId}"))
                    .setHeader("userName",simple("${body.userName}"))
                    .setHeader("userAge",simple("${body.userAge}"))
                    .to("sql: UPDATE user SET userName = :#userName, userAge = :#userAge WHERE userId=:#${body.userId}?dataSource=#dataSource")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("200"))
                .log("${body}")
                .end();

        from("direct:delete")
                .log("Realizando delete usuario")
                .to("sql:{{deleteById}}:#id{{sqlDatasource}}")
                .choice()
                    .when(simple("${header.id} == null"))
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                        .throwException(new ExceptionUserNotFound("Usuario no encontrado"))
                .otherwise()
                    //.bean(cache, "delete")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404"))
                .end();

        from("direct:getUsuario").streamCaching()
                .log("Realizando get usuario ${header.id}")
                //.bean(cache, "getUser(${header.id})")
                //.setHeader("userId",simple("${header.id}"))
                .to("sql:{{selectById}}:#id{{sqlDatasource}}{{sqlDatasourceParameter}}")
                .log("${body}")
                .choice()
                    //.bean(cache,"getUsers")
                    //.setBody(simple("getUsers.userId"))
                    .when(body().isNull())//v
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("400"))
                        .throwException(new ExceptionUserNotExists("Usuario no Existe"))
                .endChoice()
                .otherwise()
                    .log("otherwise")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("200"))
                .end()
                .log("${body}")
                .end();

        from("direct:getUsuarios")
                .log("Realizando get")
                // .bean(cache,"getUsers")//si
                .to("sql:{{selectAll}}{{sqlDatasource}}")
                .log("sql ${body}");

        from("direct:postUsuario").streamCaching()
                .log("Realizando post ${body.userId}")
                .to("bean-validator:validar-usuario")//
                .log("Body de json -> ${body}")
                    .setProperty("bodyOriginal",body()) //backup body de ingreso
                .setProperty("userIdBody",simple("${body.userId}")) //coloco user id
                //consulto a la bd si existe otro user con mismo id
                //.to("sql: select * from user where userId= :#${body.userId}?dataSource=#dataSource") //se reescribe body con resultado de la consulta
                .to("sql:{{selectById}}:#${body.userId}{{sqlDatasource}}{{sqlDatasourceParameter}}")
                .log("Body despues de consulta ${body}")
                .choice()
                    //verifica si la consulta a la bd es nula, si es asi, puedo guardarlo en bd
                    .when(body().isNotNull())
                        .process((exchange) -> {
                            String id = exchange.getProperty("userIdBody", String.class);
                            exchange.getIn().setBody(new ResponseDTO("El usuario con Id:" + id + " ya existe"));
                        })
                    .log(" Body despues de process ${body}")
                .otherwise()
                    .process((exchange) -> {
                        exchange.getIn().setBody(exchange.getProperty("bodyOriginal"));
                    })
                    .setHeader("userId",simple("${body.userId}"))
                    .setHeader("userName",simple("${body.userName}"))
                    .setHeader("userAge",simple("${body.userAge}"))
                    .to("sql:{{insertUser}}(:#userId, :#userName, :#userAge){{sqlDatasource}}")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("200"))
                .end();

    }

}
