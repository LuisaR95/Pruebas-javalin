//Importacion de la libreria de javalin
import io.javalin.Javalin;

import java.util.HashMap;
import java.util.Map;


/*
 * Ejemplo 4: Respuesta JSON con Javalin
 * Ejemplo de serializacion automatica de objetos Java a JSON.
 * Javalin usa Jackson para convertir el objeto Map en JSON sin
 **/

public class Ejemplo4JsonJavalin {
    public static void main(String[] args) {
        //Creacion del endpoint con Javalin en el puerto 7070
        Javalin app = Javalin.create().start(7070);

        //Devolucion de un usuario con Map a traves del metodo GET
        app.get("/usuario", ctx -> {
            Map<String, Object> usuario = Map.of(
                    "id", 1,
                    "nombre", "Sergio",
                    "edad", 20
            );
            //Conversion del objeto de java en json
            ctx.json(usuario);
        });

        System.out.println("Servidor iniciado en http://localhost:7070");
        System.out.println("Prueba http://localhost:7070/usuario");

    }
}

/* Resultado esperado
{
    "edad": 20,
    "nombre": "Sergio",
    "id": 1
}
 **/