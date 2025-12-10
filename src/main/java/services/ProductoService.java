package services;

import models.Producto;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/*
 * Servicio para la logica de negocio de productos
 * Gestiona el almacenamiento de memoria y operaciones CRUD
 **/

public class ProductoService {
    //Inicializamos la bbdd en memoria
    private Map<Integer, Producto> productos = new HashMap();
    private int siguienteId = 1;

    //Inicializa el servicio con algunos productos de ejemplo
    public ProductoService() {
        productos.put(1, new Producto(1, "Portatil", 1000));
        productos.put(2, new Producto(2, "Raton", 2000));
        productos.put(3, new Producto(3, "Teclado", 3000));
        siguienteId = 4;
    }
    /*
     * Obtener todos los produtos
     **/
    public List<Producto> obtenerTodos() {
        return new ArrayList<>(productos.values());
    }
    /*
     * Obtener un producto por su ID
     **/

    public Producto obtenerPorId(int id) {
        return productos.get(id);
    }
    /*
     * Crear nuevo producto
     **/

    public Producto crear(Producto producto) {
        producto.setId(++siguienteId);
        productos.put(producto.getId(), producto);
        return producto;
    }

    /*
     * Actualizar un producto
     **/
    public Producto actualizar(Producto producto) {
        productos.put(producto.getId(), producto);
        return producto;
    }
    /*
     * Eliminar un producto
     **/
    public boolean eliminar(int id) {
        return productos.remove(id) != null;
    }
}
