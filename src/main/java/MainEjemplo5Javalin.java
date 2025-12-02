import controllers.ProductoController;
import io.javalin.Javalin;
import services.ProductoService;

import java.util.Map;

public class MainEjemplo5Javalin {
    public static void main(String[] args) {
        //Creacion de la aplicacion en el puerto 7070
        Javalin app = Javalin.create(config ->{
            config.http.defaultContentType = "application/json";
            //Servir archivos estaticos desde src/main/resources/public
            config.staticFiles.add("/public");
        }).start(7070);
        //Registrar rutas de la API
        ProductoController.registrarRutas(app);
        //Manejo de excepciones global
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(400).json(Map.of("message", e.getMessage()));
        });

        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(500).json(Map.of("message", e.getMessage()));
            e.printStackTrace();
        });

        //Mostrar informacion del servidor
        System.out.println("=".repeat(60));
        System.out.println("CRUD DE PRODUCTOS CON JAVALIN");
        System.out.println("=".repeat(60));
        System.out.println("Servidor inicializado en http://localhost:7070");
        System.out.println("Endpoints API REST: ");
        System.out.println(" GET    /api/productos      -Obtener todos");
        System.out.println("GET     /api/productos/{id} -Obtener uno");
        System.out.println(" POST   /api/productos      -Crear uno");
        System.out.println(" PUT    /api/productos/{id} -Actualizar uno");
        System.out.println(" DELETE  /api/productos{id} -Eliminar uno");
        System.out.println("=".repeat(60));
    }

}