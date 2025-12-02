// Importación de libreria
import io.javalin.Javalin;


// GET (select), POST (insert), PUT (update), DELETE (delete)
/*
* API REST BASICA EN JAVA CON JAVALIN
* Aplicación simple que proporciona un endpoint tipo GET para obtener un saludo
* El servidor se inicia en el pùerto 7070 con un mensaje de texto plano
 */

// Ruta: http://localhost:7070/
public class Ejemplo1JavalinBasico {
    public static void main(String[] args) {
        // Creación del endpoint con javalin en el puerto 7070
        Javalin app = Javalin.create().start(7070);

        // Craeción del saludo con texto plano con el metodo GET
        app.get("/", ctx -> {
            ctx.result("Hola desde Javalin");
        });

        System.out.println("Servidor iniciado en http:localhost:7070");
    }
}
