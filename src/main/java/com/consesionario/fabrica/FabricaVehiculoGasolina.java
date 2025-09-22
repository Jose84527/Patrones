package com.consesionario.fabrica;

import com.consesionario.entidades.AutomovilEntity;
import com.consesionario.entidades.Motorizacion;
import com.consesionario.entidades.ScooterEntity;

import javax.persistence.EntityManager;
import java.math.BigDecimal;

public class FabricaVehiculoGasolina implements FabricaVehiculo {

    private final VehiculoRepository repo;

    public FabricaVehiculoGasolina(EntityManager em) {
        this.repo = new VehiculoRepository(em);
    }

    @Override
    public AutomovilEntity creaAutomovil(String marca, String nombre, int anio, BigDecimal precio) {
        AutomovilEntity a = new AutomovilEntity();
        a.setMarca(marca);
        a.setNombre(nombre);
        a.setAnio(anio);
        a.setPrecioBase(precio);
        a.setMotorizacion(Motorizacion.GASOLINA);
        a.setActivo(true);
        return repo.save(a);
    }

    @Override
    public ScooterEntity creaScooter(String marca, String nombre, int anio, BigDecimal precio) {
        ScooterEntity s = new ScooterEntity();
        s.setMarca(marca);
        s.setNombre(nombre);
        s.setAnio(anio);
        s.setPrecioBase(precio);
        s.setMotorizacion(Motorizacion.GASOLINA);
        s.setActivo(true);
        return repo.save(s);
    }
}
