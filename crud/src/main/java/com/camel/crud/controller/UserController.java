package com.camel.crud.controller;

import java.util.List;

import com.camel.crud.entity.Usuario;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {

    @Autowired
    ProducerTemplate producerTemplate;

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public List<Usuario> getAllEmployees() {
        List<Usuario> users = producerTemplate.requestBody("direct:select", null, List.class);
        return users;

    }

    @RequestMapping(value = "/users", consumes = "application/json", method = RequestMethod.POST)
    public boolean insertUser(@RequestBody Usuario user) {
        producerTemplate.requestBody("direct:insert", user, List.class);
        return true;
    }
}