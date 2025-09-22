package Main.Inicio;

import com.consesionario.entidades.AutomovilEntity;
import com.consesionario.entidades.ScooterEntity;
import com.consesionario.fabrica.FabricaVehiculo;
import com.consesionario.fabrica.FabricaVehiculoElectricidad;
import com.consesionario.fabrica.FabricaVehiculoGasolina;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Inicio {

    private static final String PU_NAME = "ConsesionarioPU";

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            emf = Persistence.createEntityManagerFactory(PU_NAME);
            em  = emf.createEntityManager();
            System.out.println("✅ Conexion JPA OK.\n");

            Scanner sc = new Scanner(System.in);
            while (true) {
                mostrarMenu();
                System.out.print("Seleccione opcion: ");
                String input = sc.nextLine().trim();

                switch (input) {
                    case "1":
                        verCatalogo(em);
                        break;
                    case "2":
                        crearVehiculo(em, sc);
                        break;
                    case "3":
                        System.out.println("👋 Saliendo…");
                        if (em != null) em.close();
                        if (emf != null) emf.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("⚠ Opcion invalida.\n");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void mostrarMenu() {
        System.out.println("============== MENU ==============");
        System.out.println("1. Ver catalogo");
        System.out.println("2. Crear vehiculo");
        System.out.println("3. Salir");
        System.out.println("==================================");
    }

    private static void verCatalogo(EntityManager em) {
        System.out.println("\n📋 Catalogo de vehiculos:");
        List<Object[]> filas = em.createQuery(
                "SELECT v.id, v.marca, v.nombre, v.anio, v.motorizacion, v.precioBase, TYPE(v) " +
                "FROM com.consesionario.entidades.VehiculoBase v " +
                "ORDER BY v.marca, v.nombre, v.anio", Object[].class)
            .getResultList();

        if (filas.isEmpty()) {
            System.out.println("  (sin registros)\n");
            return;
        }

        for (Object[] r : filas) {
            Long id = (Long) r[0];
            String marca = (String) r[1];
            String nombre = (String) r[2];
            Integer anio = (Integer) r[3];
            Object motorizacion = r[4]; // enum imprimible
            BigDecimal precio = (BigDecimal) r[5];
            Class<?> tipoClase = (Class<?>) r[6];
            String tipo = tipoClase.getSimpleName().contains("Automovil") ? "AUTO" : "MOTO";

            System.out.printf(" - ID:%d | %s %s (%d) | %s | %s | $%,.2f%n",
                    id, marca, nombre, anio, tipo, motorizacion, precio);
        }
        System.out.println();
    }

    private static void crearVehiculo(EntityManager em, Scanner sc) {
        try {
            System.out.print("Tipo de vehículo [1=AUTO, 2=MOTO]: ");
            String tipoSel = sc.nextLine().trim();
            boolean esAuto = "1".equals(tipoSel);
            boolean esMoto = "2".equals(tipoSel);
            if (!esAuto && !esMoto) {
                System.out.println("⚠ Opción inválida.\n");
                return;
            }

            System.out.print("Marca: ");
            String marca = sc.nextLine().trim();
            if (marca.isEmpty()) { System.out.println("⚠ Marca obligatoria.\n"); return; }

            System.out.print("Modelo (nombre): ");
            String nombre = sc.nextLine().trim();
            if (nombre.isEmpty()) { System.out.println("⚠ Modelo obligatorio.\n"); return; }

            System.out.print("Precio (ej. 25000.00): ");
            BigDecimal precio;
            try {
                precio = new BigDecimal(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.println("⚠ Precio inválido.\n");
                return;
            }

            System.out.print("Motorización [1=Eléctrico, 2=Gasolina]: ");
            String motSel = sc.nextLine().trim();

            FabricaVehiculo fabrica = "1".equals(motSel)
                    ? new FabricaVehiculoElectricidad(em)
                    : new FabricaVehiculoGasolina(em);

            int anioActual = Year.now().getValue();

            if (esAuto) {
                AutomovilEntity auto = fabrica.creaAutomovil(marca, nombre, anioActual, precio);
                System.out.println("✅ Automovil guardado con ID: " + auto.getId() + "\n");
            } else {
                ScooterEntity moto = fabrica.creaScooter(marca, nombre, anioActual, precio);
                System.out.println("✅ Scooter guardado con ID: " + moto.getId() + "\n");
            }
        } catch (Exception e) {
            System.out.println("❌ Error al guardar: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
}
