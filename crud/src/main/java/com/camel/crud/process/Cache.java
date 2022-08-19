package com.camel.crud.process;

import com.camel.crud.entity.Usuario;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class Cache {
    private static final Map<String, Usuario> listUsuario= new ConcurrentHashMap<>();
    @Handler
    public void addUser(@Body Usuario usuario){

        listUsuario.put(usuario.getId(),usuario);
    }
    public List<Usuario> getUsers(){
        return new ArrayList<>(listUsuario.values());
    }
    public Usuario getUser(String id){
      return listUsuario.get(id);
    }
    public String delete(@Header("id") String id) {//String
        String m="";
        if (listUsuario.get(id) != null) {
            listUsuario.remove(id);
            m= "el usuario que se removio tiene de id: "+ id;
        }
        else {
            m="El id: "+id+" no existe";
        }
        return m;
    }
    public Usuario update(@Body Usuario usuario){
        listUsuario.put(usuario.getId(), usuario);
        return listUsuario.get(usuario.getId());
    }
}
