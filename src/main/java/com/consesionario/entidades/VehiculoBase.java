
package com.consesionario.entidades;

/**
 *
 * @author josej
 */


import java.io.Serializable;
import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "vehiculo")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo") // valores en BD: AUTO / MOTO
public abstract class VehiculoBase implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 80)
    private String marca;


    @Column(nullable = false, length = 120)
    private String nombre;


    @Column(nullable = false)
    private Integer anio;


    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(nullable = false)
    private Boolean activo = true;


    @Column(name = "stock_disponible", nullable = false)
    private Integer stockDisponible = 0;


    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    // En BD es ENUM('ELECTRICO','GASOLINA')
    @Enumerated(EnumType.STRING)
    @Column(name = "motorizacion", length = 15, nullable = false)
    private Motorizacion motorizacion;

    // Getters/Setters
    public Long getId() { return id; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public BigDecimal getPrecioBase() { return precioBase; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Integer getStockDisponible() { return stockDisponible; }
    public void setStockDisponible(Integer stockDisponible) { this.stockDisponible = stockDisponible; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public Motorizacion getMotorizacion() { return motorizacion; }
    public void setMotorizacion(Motorizacion motorizacion) { this.motorizacion = motorizacion; }

    public abstract void mostrarCaracteristicas();
}


