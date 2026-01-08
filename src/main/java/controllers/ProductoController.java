package controllers;

import models.Producto;
import services.ProductoService;
import io.javalin.Javalin;
import java.util.Map;
import io.javalin.http.Context;

/**
 * Controlador para endpoints de productos.
 * Maneja las peticiones HTTP relacionadas con la entidad Producto.
 */
public class ProductoController {
    private static final ProductoService servicio = new ProductoService();

    /**
     * Registro de todas las rutas de productos en la aplicación.
     */
    public static void registrarRutas(Javalin app) {
        // Rutas API REST
        app.get("/api/productos", ProductoController::obtenerTodos);
        app.get("/api/productos/{id}", ProductoController::obtenerPorId);
        app.post("/api/productos", ProductoController::crear);
        app.put("/api/productos/{id}", ProductoController::actualizar);
        app.delete("/api/productos/{id}", ProductoController::eliminar);
    }

    /**
     * Obtener todos los productos.
     */
    private static void obtenerTodos(Context ctx) {
        ctx.json(servicio.obtenerTodos());
    }

    /**
     * Obtener un producto por ID.
     */
    private static void obtenerPorId(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Producto producto = servicio.obtenerPorId(id);

            if (producto != null) {
                ctx.json(producto);
            } else {
                ctx.status(404).json(Map.of("error", "Producto no encontrado"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "El ID debe ser un número válido"));
        }
    }

    /**
     * Crear un nuevo producto.
     */
    private static void crear(Context ctx) {
        Producto producto = ctx.bodyAsClass(Producto.class);

        // Validaciones básicas
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            ctx.status(400).json(Map.of("error", "El nombre no puede estar vacío"));
            return;
        }

        if (producto.getPrecio() <= 0) {
            ctx.status(400).json(Map.of("error", "El precio debe ser mayor a 0"));
            return;
        }

        Producto creado = servicio.crear(producto);
        ctx.status(201).json(creado);
    }

    /**
     * Actualiza un producto existente.
     */
    private static void actualizar(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Producto datosActualizados = ctx.bodyAsClass(Producto.class);

            // 1. Validar si el producto existe
            if (servicio.obtenerPorId(id) == null) {
                ctx.status(404).json(Map.of("error", "No se puede actualizar: Producto no encontrado"));
                return;
            }

            // 2. Validar datos del cuerpo
            if (datosActualizados.getNombre() == null || datosActualizados.getNombre().trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "El nombre no puede estar vacío"));
                return;
            }

            if (datosActualizados.getPrecio() <= 0) {
                ctx.status(400).json(Map.of("error", "El precio debe ser mayor a 0"));
                return;
            }

            datosActualizados.setId(id);
            Producto actualizado = servicio.actualizar(datosActualizados);

            if (actualizado != null) {
                ctx.status(200).json(actualizado);
            } else {
                ctx.status(500).json(Map.of("error", "Error interno al actualizar el producto"));
            }

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "El ID debe ser un número válido"));
        }
    }

    /**
     * Elimina un producto.
     */
    private static void eliminar(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            if (servicio.eliminar(id)) {
                ctx.status(204); // No Content
            } else {
                ctx.status(404).json(Map.of("error", "Producto no encontrado"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("error", "El ID debe ser un número válido"));
        }
    }
}