package Modelo;

/**
 * Clase ABSTRACTA: toda persona del sistema (comprador o usuario).
 * El atributo es privado (encapsulamiento)que valida el dato*/
public abstract class Persona {
    private String nombre;

    public Persona(String nombre) {
        setNombre(nombre);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el nombre.");
        }
        this.nombre = nombre.trim();
    }

    // METODO ABSTRACTO 1
    public abstract String getDescripcion();
}
