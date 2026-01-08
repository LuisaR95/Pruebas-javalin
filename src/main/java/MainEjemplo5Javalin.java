import io.javalin.Javalin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MainEjemplo5Javalin: Sistema de gestión de productos.
 * Incluye el controlador interno para asegurar que la persistencia en memoria funcione.
 */
public class MainEjemplo5Javalin {

    public static void main(String[] args) {

        // 1. Configuración de Javalin
        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            // Asegúrate de que la carpeta src/main/resources/public exista si usas esto
            config.staticFiles.add("/public");
        }).start(7070);

        // 2. Registro de Rutas (Llamando al controlador que definimos abajo)
        ProductoController.registrarRutas(app);

        // 3. Manejo de Excepciones Global (Corregido: no repetir IllegalArgumentException)
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(400).json(Map.of("error", "Solicitud incorrecta", "detalle", e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).json(Map.of("error", "Error interno", "detalle", e.getMessage()));
            e.printStackTrace();
        });

        // Logs de inicio
        System.out.println("\n" + "=".repeat(40));
        System.out.println("SERVIDOR PRODUCTOS INICIADO");
        System.out.println("URL: http://localhost:7070/api/productos");
        System.out.println("=".repeat(40));
    }

    /**
     * Modelo de Datos
     */
    static class Producto {
        public int id;
        public String nombre;
        public double precio;

        // Constructor vacío necesario para que Javalin/Jackson pueda deserializar el JSON
        public Producto() {}

        public Producto(int id, String nombre, double precio) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
        }
    }

    /**
     * Controlador de Productos (Lógica de negocio)
     */
    static class ProductoController {
        // USO DE STATIC: Esto asegura que la lista viva mientras el programa esté corriendo
        private static final Map<Integer, Producto> productos = new ConcurrentHashMap<>();
        private static final AtomicInteger idGenerator = new AtomicInteger(1);

        public static void registrarRutas(Javalin app) {

            // GET: Listar todos
            app.get("/api/productos", ctx -> {
                ctx.json(new ArrayList<>(productos.values()));
            });

            // GET: Buscar uno por ID
            app.get("/api/productos/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Producto p = productos.get(id);
                if (p == null) throw new IllegalArgumentException("Producto no encontrado");
                ctx.json(p);
            });

            // POST: Crear nuevo (Aquí es donde se "guarda")
            app.post("/api/productos", ctx -> {
                // Importante: bodyAsClass convierte el JSON enviado en un objeto Java
                Producto nuevo = ctx.bodyAsClass(Producto.class);
                int id = idGenerator.getAndIncrement();
                nuevo.id = id;
                productos.put(id, nuevo); // Guardado en el mapa estático
                ctx.status(201).json(nuevo);
            });

            // DELETE: Eliminar
            app.delete("/api/productos/{id}", ctx -> {
                int id = Integer.parseInt(ctx.pathParam("id"));
                if (productos.remove(id) == null) throw new IllegalArgumentException("ID no existe");
                ctx.status(204);
            });
        }
    }
}