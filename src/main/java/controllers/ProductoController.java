package controllers;
import models.Producto;
import services.ProductoService;
import io.javalin.Javalin;
import java.util.Map;
import io.javalin.http.Context;


/*
 * Controlador para endpoints de productos
 * Maneja las peticiones HTTP relacionadas con productos
 **/
public class ProductoController {
    private static ProductoService servicio = new ProductoService();


    /*
     * Registro de todas las rutas de productos en la aplicacion
     **/

    public static void registrarRutas(Javalin app) {
        //Rutas API REST
        app.get("/api/productos", ProductoController::obtenerTodos);
        app.get("/api/productos/{id}", ProductoController::obtenerPorId);
        app.get("/api/productos", ProductoController::crear);
        app.get("/api/productos/{id}", ProductoController::actualizar);
        app.get("/api/productos/{id}", ProductoController::eliminar);

    }
    /*
     * Obtener todos los productos
     **/
    private static void obtenerTodos(Context ctx) {
        ctx.json(servicio.obtenerTodos());
    }
    /*
     * Obtener un producto por ID
     **/
    private static void obtenerPorId(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Producto producto = servicio.obtenerPorId(id);

        if (producto != null) {
            ctx.json(producto);
        } else {
            ctx.status(404).json("Error, Producto no encontrado");
        }
    }
    /*
     * Crear un nuevo producto
     **/
    private static void crear(Context ctx) {
        Producto producto = ctx.bodyAsClass(Producto.class);

        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            ctx.status(400).json(Map.of("Error", "El nombre no puede estar vacio"));
        }

        if (producto.getPrecio() <= 0) {
            ctx.status(400).json(Map.of("Error", "El precio debe ser mayor a 0"));
        }

        Producto creado = servicio.crear(producto);
        ctx.status(201).json(creado);
    }
    /*
     * Actualiza un producto existente
     **/
    private static void actualizar(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Producto producto = ctx.bodyAsClass(Producto.class);

        //Validar si el prodcuto existe
        if (servicio.obtenerPorId(id) == null) {
            ctx.status(400).json(Map.of("Error", "El nombre no puede estar vacio"));
        }

        if (producto.getPrecio() <= 0) {
            ctx.status(400).json(Map.of("Error", "El precio debe ser mayor a 0"));
        }

        producto.setId(id);
        Producto actualizado = servicio.actualizar(producto);
        ctx.status(200).json(actualizado);
    }
    /*
     * Elimina un producto
     **/
    private static void eliminar(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));

        if (servicio.eliminar(id)) {
            ctx.status(204);
        }else  {
            ctx.status(404).json("Error, Producto no encontrado");
        }
    }
}
