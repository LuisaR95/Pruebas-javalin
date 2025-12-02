package models;



//Clase modelo producto
//Representa a un producto con su id, nombre y precio
public class Producto {
    //Inicializacion de sus variables
    private int id;
    private String nombre;
    private double precio;

    // Constructor vacio (requerido para Jackson/Gson)
    public Producto() {}

    //Constructor con parametros
    public Producto(int id, String nombre, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }
    //Getters y setters


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
                ", precio='" + precio + '\'' +
                '}';
    }
}

