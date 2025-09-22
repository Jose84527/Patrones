
package com.consesionario.entidades;

/**
 *
 * @author josej
 */


import javax.persistence.*;

@Entity
@DiscriminatorValue("MOTO") // coincide con ENUM 'tipo' en BD
public class ScooterEntity extends VehiculoBase {

    @Override
    public void mostrarCaracteristicas() {
        System.out.println("Scooter " + getMarca() + " " + getNombre()
                + " (" + getAnio() + ") - Motorización: " + getMotorizacion()
                + " - Precio: " + getPrecioBase());
    }
}

