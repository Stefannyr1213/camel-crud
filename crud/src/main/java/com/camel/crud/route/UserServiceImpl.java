package com.camel.crud.route;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.camel.crud.entity.Usuario;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sql.SqlComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends RouteBuilder {

    @Autowired
    DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    //define the SQL Component bean which will be used as an endpoint in our route
    @Bean
    public SqlComponent sql(DataSource dataSource) {
        SqlComponent sql = new SqlComponent();
        sql.setDataSource(dataSource);
        return sql;
    }

    @Override
    public void configure() throws Exception {

        //Insert Route
        from("direct:insert").log("Processing message: ").setHeader("message", body()).process(new Processor() {
            public void process(Exchange xchg) throws Exception {
                //take the Usuario object from the exchange and create the parameter map
                Usuario usuario = xchg.getIn().getBody(Usuario.class);
                Map<String, Object> userMap = new HashMap<String, Object>();
                userMap.put("UserId", usuario.getId());
                userMap.put("UserName", usuario.getName());
                userMap.put("UserAge", usuario.getAge());
                xchg.getIn().setBody(userMap);
            }
        }).to("sql:INSERT INTO user(userId, userName, userAge) VALUES (:#userId, :#userName, #userAge)");

        // Select Route
        from("direct:select").to("sql:select * from user").process(new Processor() {
            public void process(Exchange xchg) throws Exception {
                //the camel sql select query has been executed. We get the list of users.
                ArrayList<Map<String, String>> dataList = (ArrayList<Map<String, String>>) xchg.getIn().getBody();
                List<Usuario> users = new ArrayList<Usuario>();
                System.out.println(dataList);
                for (Map<String, String> data : dataList) {
                    Usuario user = new Usuario();
                    user.setId(data.get("userId"));
                    user.setName(data.get("userName"));
                    user.setAge(Integer.parseInt(data.get("userAge")));
                    users.add(user);
                }
                xchg.getIn().setBody(users);
            }
        });

    }
}