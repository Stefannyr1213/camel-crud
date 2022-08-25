package com.camel.crud.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;


public class Usuario {

    @NotBlank
    @JsonProperty("id")
    private String id;
    @NotBlank
    @JsonProperty("name")
    private String name;
    @Min(value = 1)
    @JsonProperty("age")
    private int age;

    public Usuario()
    {

    }
    public Usuario(String id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString(){
        return "id: "+id+" name: "+name+" age: "+age;
    }
}
