package com.camel.crud.process;

import com.camel.crud.entity.Usuario;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class SetDataExchangeProcessor implements Processor {
    @Override
    public void process(Exchange exchange)throws Exception{
        System.out.println( exchange.getIn().getBody(String.class));
        Usuario usuario= new Usuario();
        usuario.setUserId("1006188867");
        usuario.setUserName("David");
        usuario.setUserAge(22);
        exchange.getIn().setBody(usuario);
    }
}
