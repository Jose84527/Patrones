
package com.consesionario.entidades;

/**
 *
 * @author josej
 */


import javax.persistence.*;

@Entity
@DiscriminatorValue("AUTO") // coincide con ENUM 'tipo' en BD
public class AutomovilEntity extends VehiculoBase {

    @Override
    public void mostrarCaracteristicas() {
        System.out.println("Automóvil " + getMarca() + " " + getNombre()
                + " (" + getAnio() + ") - Motorización: " + getMotorizacion()
                + " - Precio: " + getPrecioBase());
    }
}
