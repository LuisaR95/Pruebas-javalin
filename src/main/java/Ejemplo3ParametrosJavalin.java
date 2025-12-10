//Importacion de la libreria de javalin
import io.javalin.Javalin;



/*
 * Ejemplo 3: Manejo de parametros de ruta con javalin
 * Demostracion de como capturar parametros de URL
 * El parametro nombre se extrae usando la funcion pathParame() y se usa en la respuesta
 **/
public class Ejemplo3ParametrosJavalin {
    public static void main(String[] args) {


        //Creacion del endpoint con Javalin en el puerto 7070
        Javalin app = Javalin.create().start(7070);

        //Creacion del saludo con texto plano con el metodo GET
        app.get("/saludo/{nombre}", ctx -> {
            String nombre = ctx.pathParam("nombre");
            ctx.result("¡Hola, " + nombre + "!");
        });

        System.out.println("Servidor iniciado en http://localhost:7070");
        System.out.println("Prueba http://localhost:7070/saludo/Sergio");

    }
}
