package com.camel.crud.process;


import com.camel.crud.dtos.ResponseDTO;
import com.camel.crud.entity.Usuario;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProcessDataExchangeProcessor implements Processor {
private static final Logger LOGGER= LoggerFactory.getLogger(ProcessDataExchangeProcessor.class);
    @Override
    public void process(Exchange exchange)throws Exception{
       Usuario usuario=exchange.getIn().getBody(Usuario.class);
       LOGGER.info(" Post Usuario: id: {},name: {},age: {}"
               ,usuario.getUserId()
       ,usuario.getUserName()
       ,usuario.getUserAge());
        ResponseDTO responseDTO=new ResponseDTO("El usuario se agrego correctamente");

        exchange.getOut().setBody(responseDTO);
    }
}
