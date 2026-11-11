package Componentes;

/**
 * Contrato de cada modulo (pestaña) de la ventana principal: se llama cada vez que
 * el modulo se muestra, para que recargue sus datos.
 * Es una interfaz (no una clase abstracta) para que cada formulario herede
 * directamente de JPanel y NetBeans pueda mostrar su diseño en la pestaña Design.
 */
public interface Modulo {
    void actualizar();
}
