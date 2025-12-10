package models;

/*
 *Clase modelo Producto
 * Representa un producto con su id, nombre y precio
 **/

public class Producto {
    //Inicializacion de sus variables
    private int  id;
    private String nombre;
    private double precio;

    //Constructo vacio (requerido para Jackson/Gson)
    public Producto() {}




    public Producto(int id, String nombre, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

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

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
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
