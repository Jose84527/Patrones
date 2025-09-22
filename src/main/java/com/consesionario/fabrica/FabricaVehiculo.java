
package com.consesionario.fabrica;

/**
 *
 * @author josej
 */


import com.consesionario.entidades.AutomovilEntity;
import com.consesionario.entidades.ScooterEntity;
import java.math.BigDecimal;

public interface FabricaVehiculo {
    AutomovilEntity creaAutomovil(String marca, String nombre, int anio, BigDecimal precio);
    ScooterEntity   creaScooter  (String marca, String nombre, int anio, BigDecimal precio);
}
