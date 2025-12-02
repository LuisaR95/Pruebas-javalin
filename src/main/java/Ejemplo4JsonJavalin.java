// Importación de librería
import io.javalin.Javalin;
import java.util.Map;

// Ejemplo 4: Respuestas JSON con Javalin
// Ejemplo de serialización automática de objetos Java a JSON
// Javalin utiliza Jackson para convertir el objeto Map en JSON

public class Ejemplo4JsonJavalin {
    public static void main(String[] args) {

        // Creación del endpoint con Javalin en el puerto 7070
        Javalin app = Javalin.create().start(7070);

        // Ejemplo de usuario como Map a través del método GET
        app.get("/usuario", ctx -> {
            Map<String, Object> usuario = Map.of(
                    "id", 1,
                    "nombre", "Luisa Rincon",
                    "edad", 21
            );

            // Conversión del objeto de Java a JSON
            ctx.json(usuario);
        });

        System.out.println("Servidor iniciado en http://localhost:7070");
        System.out.println("Prueba: http://localhost:7070/usuario");
    }
}
