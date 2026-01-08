package models;

/**
 * Clase modelo Producto.
 * Representa la entidad de un producto dentro del sistema.
 */
public class Producto {
    private int id;
    private String nombre;
    private double precio;

    /**
     * Constructor vacío requerido para la deserialización de JSON (Jackson/Gson).
     */
    public Producto() {}

    /**
     * Constructor con todos los campos.
     * * @param id Identificador único del producto.
     * @param nombre Nombre descriptivo.
     * @param precio Valor unitario.
     */
    public Producto(int id, String nombre, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                '}';
    }
}