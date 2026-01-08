package services;

import models.Producto;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio para la lógica de negocio de productos.
 * Gestiona el almacenamiento en memoria de forma segura para hilos (thread-safe).
 */
public class ProductoService {

    // ConcurrentHashMap asegura que el mapa sea accesible por múltiples hilos sin errores
    private static final Map<Integer, Producto> productos = new ConcurrentHashMap<>();

    // AtomicInteger para manejar IDs de forma segura y evitar duplicados
    private static final AtomicInteger siguienteId = new AtomicInteger(1);

    /**
     * Inicializa el servicio con datos de ejemplo si la lista está vacía.
     */
    public ProductoService() {
        if (productos.isEmpty()) {
            crearEjemplo(new Producto(0, "Portatil", 1000.0));
            crearEjemplo(new Producto(0, "Raton", 20.0));
            crearEjemplo(new Producto(0, "Teclado", 50.0));
        }
    }

    /**
     * Helper para cargar datos iniciales sin saltar IDs.
     */
    private void crearEjemplo(Producto p) {
        int id = siguienteId.getAndIncrement();
        p.setId(id);
        productos.put(id, p);
    }

    /**
     * Obtener todos los productos.
     */
    public List<Producto> obtenerTodos() {
        return new ArrayList<>(productos.values());
    }

    /**
     * Obtener un producto por su ID.
     */
    public Producto obtenerPorId(int id) {
        return productos.get(id);
    }

    /**
     * Crear un nuevo producto.
     * El ID se genera automáticamente.
     */
    public Producto crear(Producto producto) {
        int id = siguienteId.getAndIncrement();
        producto.setId(id);
        productos.put(id, producto);
        return producto;
    }

    /**
     * Actualizar un producto existente.
     */
    public Producto actualizar(Producto producto) {
        if (productos.containsKey(producto.getId())) {
            productos.put(producto.getId(), producto);
            return producto;
        }
        return null;
    }

    /**
     * Eliminar un producto.
     */
    public boolean eliminar(int id) {
        return productos.remove(id) != null;
    }
}