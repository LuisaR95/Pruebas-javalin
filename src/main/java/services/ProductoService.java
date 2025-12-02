package services;

import models.Producto;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

// Servicio para la logica de negocio de productos
// Gestiona el almacenamiento en memoria y operaciones CRUD
public class ProductoService {
    //Inicializacion de la "bbdd" en memoria
    private Map<Integer,Producto> productos = new HashMap();
    private int siguienteId= 1;

    //Inicializar el servicio con algunos porductos de ejemplo
    public ProductoService() {
        productos.put(1, new Producto(1, "Portatil", 999.99));
        productos.put(2, new Producto(2, "Ratón", 29.99));
        productos.put(3, new Producto(3, "Teclado", 99.99));
        siguienteId = 4;
    }

    //Obtener todos los productos
    public List<Producto> obtenerTodos() {
        return new ArrayList<>(productos.values());
    }

    // Obtener un producto por ID
    public  Producto obtenerPorId(int id) {
        return productos.get(id);
    }

    //Crear un nuevo producto
    public  Producto crear(Producto producto) {
        producto.setId(siguienteId++);
        productos.put(producto.getId(), producto);
        return producto;
    }

    // Actualizar producto
    public Producto actualizar (Producto producto) {
        productos.put(producto.getId(), producto);
        return producto;
    }

    // Elimiar producto
    public boolean eliminar (int id) {
        return productos.remove(id) != null;
    }

}
