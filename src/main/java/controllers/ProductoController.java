package controllers;

import models.Producto;
import services.ProductoService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Map;

// Controlador para endpoints de productos
public class ProductoController {

    private static final ProductoService servicio = new ProductoService();

    // Registro de rutas
    public static void registrarRutas(Javalin app) {
        app.get("/api/productos", ProductoController::obtenerTodos);
        app.get("/api/productos/{id}", ProductoController::obtenerPorId);
        app.post("/api/productos", ProductoController::crear);
        app.put("/api/productos/{id}", ProductoController::actualizar);
        app.delete("/api/productos/{id}", ProductoController::eliminar);
    }

    // Obtener todos los productos
    private static void obtenerTodos(Context ctx) {
        ctx.json(servicio.obtenerTodos());
    }

    // Obtener producto por ID
    private static void obtenerPorId(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Producto producto = servicio.obtenerPorId(id);

        if (producto != null) {
            ctx.json(producto);
        } else {
            ctx.status(404).json(Map.of("error", "Producto no encontrado"));
        }
    }

    // Crear producto
    private static void crear(Context ctx) {
        Producto nuevo = ctx.bodyAsClass(Producto.class);

        // Validación del nombre
        if (nuevo.getNombre() == null || nuevo.getNombre().trim().isEmpty()) {
            ctx.status(400).json(Map.of("error", "El nombre es obligatorio"));
            return;
        }

        // Validación del precio
        if (nuevo.getPrecio() < 0) {
            ctx.status(400).json(Map.of("error", "El precio no puede ser negativo"));
            return;
        }

        Producto creado = servicio.crear(nuevo);
        ctx.status(201).json(creado);
    }

    // Actualizar producto
    private static void actualizar(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Producto actualizado = ctx.bodyAsClass(Producto.class);

        // Verificar si existe
        Producto existente = servicio.obtenerPorId(id);
        if (existente == null) {
            ctx.status(404).json(Map.of("error", "Producto no encontrado"));
            return;
        }

        // Validación del nombre
        if (actualizado.getNombre() == null || actualizado.getNombre().trim().isEmpty()) {
            ctx.status(400).json(Map.of("error", "El nombre es obligatorio"));
            return;
        }

        // Validación del precio
        if (actualizado.getPrecio() < 0) {
            ctx.status(400).json(Map.of("error", "El precio no puede ser negativo"));
            return;
        }

        // Fijar ID
        actualizado.setId(id);

        Producto resultado = servicio.actualizar(actualizado);
        ctx.status(200).json(resultado);
    }

    // Eliminar producto
    private static void eliminar(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));

        if (servicio.eliminar(id)) {
            ctx.status(204);  // No Content
            ctx.result("");
        } else {
            ctx.status(404).json(Map.of("error", "Producto no encontrado"));
        }
    }
}
