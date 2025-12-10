//Importacion de la libreria de javalin
import io.javalin.Javalin;

//GET (select), POST (insert), PUT (update), DELETE(delete)

/*
*API RESTO BASICA EN JAVA CON JAVALIN
* Apliacion simple que proporciona un endpoint tipo get
* El servidor se inicia en el puerto 7070 co un mensaje de texto plano
**/


//http://localhost:7070
public class Ejemplo1JavalinBasico {
    public static void main(String[] args) {
        //Creacion del endpoint con javalin
        Javalin app = Javalin.create().start(7070);

        //Creacion del saludo con texto plano con el metodo GET
        app.get("/", ctx -> {
            ctx.result("Hola desde Javalin");
        });

        System.out.println("Servidor iniciado en http://localhost:7070");
    }


}
