package Modelo;

// Cliente registrado en su propio formulario. Hereda de Persona y tiene un codigo (C001, C002...)
public class Cliente extends Persona {
    public static final int EDAD_MINIMA_DESCUENTO = 60;
    public static final double PORCENTAJE_DESCUENTO = 0.15;

    private final String codigo;
    private int edad;
    private String dni;
    private String telefono;

    public Cliente(String codigo, String nombre, int edad, String dni, String telefono) {
        super(nombre);
        this.codigo = codigo;
        setEdad(edad);
        setDni(dni);
        setTelefono(telefono);
    }

    public String getCodigo() { return codigo; }
    public int getEdad() { return edad; }
    public String getDni() { return dni; }
    public String getTelefono() { return telefono; }

    public void setEdad(int edad) {
        if (edad < 1 || edad > 120) {
            throw new IllegalArgumentException("La edad debe estar entre 1 y 120.");
        }
        this.edad = edad;
    }

    public void setDni(String dni) {
        if (dni == null || !dni.trim().matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener 8 digitos.");
        }
        this.dni = dni.trim();
    }

    public void setTelefono(String telefono) {
        String t = (telefono == null) ? "" : telefono.trim();
        if (!t.isEmpty() && !t.matches("\\d{9}")) {
            throw new IllegalArgumentException("El telefono debe tener 9 digitos (o dejarlo vacio).");
        }
        this.telefono = t;
    }

    public boolean tieneDescuento() {
        return edad > EDAD_MINIMA_DESCUENTO;
    }

    public double getPorcentajeDescuento() {
        return tieneDescuento() ? PORCENTAJE_DESCUENTO : 0;
    }

    @Override
    public String getDescripcion() {
        return codigo + " - " + getNombre();
    }

    @Override
    public String toString() {
        return getDescripcion();
    }
}
