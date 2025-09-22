/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package Main.Inicio;

/**
 *
 * @author josej
 */

import com.consesionario.entidades.Motorizacion;
import com.consesionario.entidades.AutomovilEntity;
import com.consesionario.entidades.ScooterEntity;
import com.consesionario.fabrica.FabricaVehiculo;
import com.consesionario.fabrica.FabricaVehiculoElectricidad;
import com.consesionario.fabrica.FabricaVehiculoGasolina;

import javax.persistence.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.text.NumberFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;
import java.text.DecimalFormat;
import javax.swing.table.DefaultTableCellRenderer;

public class NeevosCarros extends javax.swing.JFrame {


private static final String PU_NAME = "ConsesionarioPU";
// Estado del flujo
private boolean modoAjusteStock = false;
private Long vehiculoSeleccionadoId = null;


    public NeevosCarros() {
        initComponents();
        configurarControles();
        configurarControles();     // lo que ya tenías
    configurarTablaSeleccion(); // para habilitar "Aumentar Stock" cuando hay selección
    setModoAjusteStock(false); // modo inicial

            ((AbstractDocument) Marca.getDocument()).setDocumentFilter(new DocumentFilter() {
        private final int max = 100;
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (fb.getDocument().getLength() + string.length() <= max) {
                super.insertString(fb, offset, string, attr);
            }
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (fb.getDocument().getLength() - length + (text != null ? text.length() : 0) <= max) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    });

    // Limitar Modelo (idéntico, mismo patrón)
    ((AbstractDocument) Modelo.getDocument()).setDocumentFilter(new DocumentFilter() {
        private final int max = 100;
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (fb.getDocument().getLength() + string.length() <= max) {
                super.insertString(fb, offset, string, attr);
            }
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (fb.getDocument().getLength() - length + (text != null ? text.length() : 0) <= max) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    });}
        
    private void configurarControles() {

    // ------------- ComboBox Tipo -------------
    DefaultComboBoxModel<String> tipoModel = new DefaultComboBoxModel<>(
            new String[]{"Elige una opción", "Automovil", "Motocicleta"});
    Tipo.setModel(tipoModel);

    // ------------- ComboBox Motorización -------------
    DefaultComboBoxModel<String> motorModel = new DefaultComboBoxModel<>(
            new String[]{"Elige una opción", "Eléctrico", "Gasolina"});
    Motor.setModel(motorModel);

// ------------- Spinner Año -------------
// Modelo con límites (pero ojo: al permitir vacío, no validamos aquí todavía)
SpinnerNumberModel anioModel = new SpinnerNumberModel(2025, 1950, 2025, 1);
Año.setModel(anioModel);

// Editor con formato de 4 dígitos
JSpinner.NumberEditor anioEditor = new JSpinner.NumberEditor(Año, "####");
Año.setEditor(anioEditor);

JFormattedTextField tfAnio = anioEditor.getTextField();
tfAnio.setFocusLostBehavior(JFormattedTextField.PERSIST); // no corrige automáticamente
javax.swing.text.NumberFormatter fmtAnio =
        (javax.swing.text.NumberFormatter) tfAnio.getFormatter();
fmtAnio.setAllowsInvalid(true);       // permite borrar todo o escribir parcial
fmtAnio.setCommitsOnValidEdit(true);  // aplica al modelo si es válido
fmtAnio.setMinimum(1950);
fmtAnio.setMaximum(2025);

// ------------- Spinner Precio (dos decimales) -------------
// min 25,000.00 ; max 1,500,000.00 ; paso 100.00
SpinnerNumberModel precioModel =
        new SpinnerNumberModel(25000.00, 25000.00, 1500000.00, 100.00);
Precio.setModel(precioModel);

JSpinner.NumberEditor precioEditor = new JSpinner.NumberEditor(Precio, "#,##0.00");
Precio.setEditor(precioEditor);

JFormattedTextField tfPrecio = precioEditor.getTextField();
tfPrecio.setFocusLostBehavior(JFormattedTextField.PERSIST);
javax.swing.text.NumberFormatter fmtPrecio =
        (javax.swing.text.NumberFormatter) tfPrecio.getFormatter();
fmtPrecio.setAllowsInvalid(true);       // permite vacío/ediciones parciales
fmtPrecio.setCommitsOnValidEdit(true);  // cuando es válido, actualiza el modelo
fmtPrecio.setMinimum(25000.00);
fmtPrecio.setMaximum(1500000.00);


    // ------------- Spinner y botón de stock (bloqueados por ahora) -------------
    Stock.setEnabled(false);

    // ------------- Tabla: columnas si aún no las definiste -------------
    if (TablaVehiculos.getModel() == null || TablaVehiculos.getColumnCount() < 6) {
        // Si aún no la tienes creada, crea el modelo con la nueva columna "Stock"
TablaVehiculos.setModel(new javax.swing.table.DefaultTableModel(
        new Object[][]{},
        new String[]{"ID","Marca","Modelo","Tipo","Motorización","Año","Stock","Precio"}
) {
    @Override public boolean isCellEditable(int r, int c) { return false; }
    // opcional: tipos de columna para mejor ordenamiento
    @Override public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Long.class;       // ID
            case 5 -> Integer.class;    // Año
            case 6 -> Integer.class;    // Stock
            case 7 -> java.math.BigDecimal.class; // Precio
            default -> String.class;
        };
    }
});

    }

    // Carga inicial
    cargarTablaVehiculos();    
    }
    
    
private void cargarTablaVehiculos() {
    EntityManagerFactory emf = null;
    EntityManager em = null;
    try {
        emf = Persistence.createEntityManagerFactory(PU_NAME);
        em  = emf.createEntityManager();

        List<Object[]> filas = em.createQuery(
            "SELECT v.id, v.marca, v.nombre, TYPE(v), v.motorizacion, v.anio, " +
            "       v.stockDisponible, v.precioBase " +
            "FROM com.consesionario.entidades.VehiculoBase v " +
            "ORDER BY v.id DESC", Object[].class
        ).getResultList();

        DefaultTableModel m = (DefaultTableModel) TablaVehiculos.getModel();
        m.setRowCount(0);
        for (Object[] r : filas) {
            Long id            = (Long) r[0];
            String marca       = (String) r[1];
            String nombre      = (String) r[2];
            Class<?> tipoClase = (Class<?>) r[3];
            String tipo        = tipoClase.getSimpleName().contains("Automovil") ? "Automovil" : "Motocicleta";
            Object motoriz     = r[4];
            Integer anio       = (Integer) r[5];
            Integer stock      = (Integer) r[6];
            BigDecimal precio  = (BigDecimal) r[7];

            m.addRow(new Object[]{id, marca, nombre, tipo, motoriz, anio, stock, precio});
        }

        // ---------- Renderers ----------
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        // para columnas numéricas normales
        TablaVehiculos.getColumnModel().getColumn(0).setCellRenderer(right); // ID
        TablaVehiculos.getColumnModel().getColumn(5).setCellRenderer(right); // Año
        TablaVehiculos.getColumnModel().getColumn(6).setCellRenderer(right); // Stock

        // para Precio con formato #,##0.00
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
        DefaultTableCellRenderer precioRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof BigDecimal bd) {
                    super.setValue(df.format(bd));
                } else if (value instanceof Number n) {
                    super.setValue(df.format(n.doubleValue()));
                } else {
                    super.setValue(value);
                }
            }
        };
        precioRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        TablaVehiculos.getColumnModel().getColumn(7).setCellRenderer(precioRenderer);

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error al cargar tabla: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    } finally {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }
}

private void configurarTablaSeleccion() {
    // Habilita botón Aumentar Stock solo si hay una fila seleccionada
    TablaVehiculos.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            int row = TablaVehiculos.getSelectedRow();
            boolean haySel = row >= 0;
            GuardarStock.setEnabled(haySel && !modoAjusteStock);
        }
    });
}
private void setModoAjusteStock(boolean on) {
    modoAjusteStock = on;

    // Entradas de “alta de vehículo”
    Marca.setEnabled(!on);
    Modelo.setEnabled(!on);
    Tipo.setEnabled(!on);
    Motor.setEnabled(!on);
    Año.setEnabled(!on);
    Precio.setEnabled(!on);

    // Controles de stock
    Stock.setEnabled(on);
    Guardar.setEnabled(on);               // Guardar solo en modo stock
    GuardarStock.setEnabled(!on && TablaVehiculos.getSelectedRow() >= 0);

    // Si entras al modo, valor inicial 0 y foco
    if (on) {
        if (Stock.getModel() == null || !(Stock.getModel() instanceof SpinnerNumberModel)) {
            Stock.setModel(new javax.swing.SpinnerNumberModel(0, 0, 100000, 1));
        }
        Stock.setValue(0);
        // permitir edición por teclado, respetando límites y permitiendo vacío temporal
        JSpinner.NumberEditor ed = new JSpinner.NumberEditor(Stock, "####");
        Stock.setEditor(ed);
        javax.swing.JFormattedTextField tf = ed.getTextField();
        tf.setFocusLostBehavior(javax.swing.JFormattedTextField.PERSIST);
        javax.swing.text.NumberFormatter nf = (javax.swing.text.NumberFormatter) tf.getFormatter();
        nf.setAllowsInvalid(true);
        nf.setCommitsOnValidEdit(true);
        nf.setMinimum(0);
        nf.setMaximum(100000);
        tf.requestFocusInWindow();
    } else {
        // salir del modo: limpia selección, deja UI como al inicio
        TablaVehiculos.clearSelection();
        Guardar.setEnabled(false);
        Stock.setEnabled(false);
        // si quieres, limpia el form de alta:
        // limpiarFormulario();
    }
}
private Integer leerStockDeltaOrNull() {
    String txt = ((JSpinner.NumberEditor) Stock.getEditor())
            .getTextField().getText().trim();
    if (txt.isEmpty()) return null;
    try {
        ((JSpinner.NumberEditor) Stock.getEditor()).getTextField().commitEdit();
        Object v = Stock.getValue();
        return (v instanceof Number) ? ((Number) v).intValue() : null;
    } catch (Exception ex) {
        return null;
    }
}

private void limpiarFormulario() {
    Marca.setText("");
    Modelo.setText("");
    Tipo.setSelectedIndex(0);
    Motor.setSelectedIndex(0);
    Año.setValue(Year.now().getValue());
    Precio.setValue(25000.00);
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        Marca = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        Motor = new javax.swing.JComboBox<>();
        Modelo = new javax.swing.JTextField();
        Tipo = new javax.swing.JComboBox<>();
        Precio = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        TablaVehiculos = new javax.swing.JTable();
        Guardar = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        Año = new javax.swing.JSpinner();
        Stock = new javax.swing.JSpinner();
        GuardarStock = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        jLabel1.setText("Ingresar Nuevos Vehiculos A La BD");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel2.setText("Marca:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel3.setText("Modelo:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel4.setText("Tipo:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel5.setText("Motorización:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel6.setText("Precio B:");

        Marca.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        Marca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MarcaActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        Motor.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        Motor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Gasolina", "Electrico" }));
        Motor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MotorActionPerformed(evt);
            }
        });

        Modelo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        Modelo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ModeloActionPerformed(evt);
            }
        });

        Tipo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        Tipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Automovil", "Motocicleta" }));
        Tipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TipoActionPerformed(evt);
            }
        });

        TablaVehiculos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(TablaVehiculos);

        Guardar.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        Guardar.setText("Registrar");
        Guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GuardarActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 2, 14)); // NOI18N
        jLabel8.setText("Año:");

        GuardarStock.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        GuardarStock.setText("Aumentar Stock");
        GuardarStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                GuardarStockActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(229, 229, 229)
                                .addComponent(jLabel1))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(160, 160, 160)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(Tipo, javax.swing.GroupLayout.Alignment.LEADING, 0, 200, Short.MAX_VALUE)
                                            .addComponent(Motor, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(Modelo, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(Marca))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel7))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(Año, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                            .addComponent(Precio, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(Stock, javax.swing.GroupLayout.Alignment.LEADING))
                                        .addGap(18, 18, 18)
                                        .addComponent(GuardarStock)))))
                        .addGap(297, 297, 297)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(330, 330, 330)
                .addComponent(Guardar, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Marca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel7)
                    .addComponent(Modelo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(Tipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(Motor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(Precio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(Año, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Stock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(GuardarStock))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(Guardar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void MarcaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MarcaActionPerformed
        Marca.setText(Marca.getText().trim());
    }//GEN-LAST:event_MarcaActionPerformed

    private void ModeloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ModeloActionPerformed
        Modelo.setText(Modelo.getText().trim());
    }//GEN-LAST:event_ModeloActionPerformed

    private void TipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TipoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TipoActionPerformed

    private void MotorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MotorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MotorActionPerformed

    private void GuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GuardarActionPerformed

    // ====== MODO AJUSTE DE STOCK ======
    if (modoAjusteStock) {
        if (vehiculoSeleccionadoId == null) {
            JOptionPane.showMessageDialog(this, "No hay vehículo seleccionado.");
            return;
        }

        // Leer spinner de stock permitiendo campo vacío
        Integer delta = null;
        try {
            JSpinner.NumberEditor ed = (JSpinner.NumberEditor) Stock.getEditor();
            JFormattedTextField tf = ed.getTextField();
            String txt = tf.getText().trim();
            if (!txt.isEmpty()) {
                tf.commitEdit(); // fuerza el parseo
                Object v = Stock.getValue();
                if (v instanceof Number) delta = ((Number) v).intValue();
            }
        } catch (Exception ignore) {}

        if (delta == null) {
            JOptionPane.showMessageDialog(this, "Ingresa una cantidad de stock válida (>= 1).");
            return;
        }
        if (delta <= 0) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.");
            return;
        }

        EntityManagerFactory emf = null;
        EntityManager em = null;
        try {
            emf = Persistence.createEntityManagerFactory(PU_NAME);
            em  = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            // Bloqueo pesimista para evitar condiciones de carrera
            com.consesionario.entidades.VehiculoBase v =
                em.find(com.consesionario.entidades.VehiculoBase.class,
                        vehiculoSeleccionadoId, LockModeType.PESSIMISTIC_WRITE);

            if (v == null) {
                tx.rollback();
                JOptionPane.showMessageDialog(this, "El vehículo ya no existe.");
                return;
            }

            int actual = (v.getStockDisponible() == null ? 0 : v.getStockDisponible());
            int nuevo  = actual + delta;
            v.setStockDisponible(nuevo);

            tx.commit();

            JOptionPane.showMessageDialog(this,
                "Stock actualizado: " + actual + " → " + nuevo + " (+" + delta + ")",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);

            cargarTablaVehiculos();
            vehiculoSeleccionadoId = null;
            setModoAjusteStock(false);   // volver al modo inicial

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al ajustar stock: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (em != null) em.close();
            if (emf != null) emf.close();
        }
        return; // ya terminamos en modo stock
    }

    // ====== MODO ALTA NORMAL ======

    // Validaciones básicas de texto / combos
    String marca  = Marca.getText().trim();
    String modelo = Modelo.getText().trim();
    if (marca.isEmpty()) { JOptionPane.showMessageDialog(this, "La marca es obligatoria.", "Validación", JOptionPane.WARNING_MESSAGE); return; }
    if (marca.length() > 100) { JOptionPane.showMessageDialog(this, "La marca no debe exceder 100 caracteres.", "Validación", JOptionPane.WARNING_MESSAGE); return; }
    if (modelo.isEmpty()) { JOptionPane.showMessageDialog(this, "El modelo es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE); return; }
    if (modelo.length() > 100) { JOptionPane.showMessageDialog(this, "El modelo no debe exceder 100 caracteres.", "Validación", JOptionPane.WARNING_MESSAGE); return; }
    if (Tipo.getSelectedIndex() <= 0) { JOptionPane.showMessageDialog(this, "Selecciona un tipo válido (Automovil o Motocicleta).", "Validación", JOptionPane.WARNING_MESSAGE); return; }
    if (Motor.getSelectedIndex() <= 0) { JOptionPane.showMessageDialog(this, "Selecciona una motorización válida (Eléctrico o Gasolina).", "Validación", JOptionPane.WARNING_MESSAGE); return; }

    // Leer AÑO permitiendo vacío; validar formato y rango 1950–2025
    Integer anio = null;
    try {
        JSpinner.NumberEditor edAnio = (JSpinner.NumberEditor) Año.getEditor();
        JFormattedTextField tfAnio = edAnio.getTextField();
        String txt = tfAnio.getText().trim();
        if (!txt.isEmpty()) {
            tfAnio.commitEdit();                  // parsea lo tipeado si es válido
            Object v = Año.getValue();
            if (v instanceof Number) anio = ((Number) v).intValue();
        }
    } catch (Exception ignore) {}

    if (anio == null || anio < 1950 || anio > 2025) {
        JOptionPane.showMessageDialog(this, "Indica un año válido (1950–2025).",
                "Validación", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Leer PRECIO permitiendo vacío; validar formato y rango 25,000.00–1,500,000.00
    BigDecimal precio = null;
    try {
        JSpinner.NumberEditor edPrecio = (JSpinner.NumberEditor) Precio.getEditor();
        JFormattedTextField tfPrecio = edPrecio.getTextField();
        String txt = tfPrecio.getText().trim();
        if (!txt.isEmpty()) {
            tfPrecio.commitEdit();
            Object v = Precio.getValue();
            if (v instanceof Number) {
                precio = BigDecimal.valueOf(((Number) v).doubleValue())
                                    .setScale(2, RoundingMode.HALF_UP);
            }
        }
    } catch (Exception ignore) {}

    if (precio == null ||
        precio.compareTo(new BigDecimal("25000.00")) < 0 ||
        precio.compareTo(new BigDecimal("1500000.00")) > 0) {
        JOptionPane.showMessageDialog(this,
                "Indica un precio válido (25,000.00 – 1,500,000.00).",
                "Validación", JOptionPane.WARNING_MESSAGE);
        return;
    }

    boolean esAuto = (Tipo.getSelectedIndex() == 1); // 1=Automovil, 2=Motocicleta
    Motorizacion mot = (Motor.getSelectedIndex() == 1)
            ? Motorizacion.ELECTRICO
            : Motorizacion.GASOLINA;

    EntityManagerFactory emf = null;
    EntityManager em = null;

    try {
        emf = Persistence.createEntityManagerFactory(PU_NAME);
        em  = emf.createEntityManager();

        // Seleccionamos la fábrica según motorización
        FabricaVehiculo fabrica = (mot == Motorizacion.ELECTRICO)
                ? new FabricaVehiculoElectricidad(em)
                : new FabricaVehiculoGasolina(em);

        if (esAuto) {
            AutomovilEntity a = fabrica.creaAutomovil(marca, modelo, anio, precio);
            JOptionPane.showMessageDialog(this, "Automóvil registrado. ID: " + a.getId(),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            ScooterEntity s = fabrica.creaScooter(marca, modelo, anio, precio);
            JOptionPane.showMessageDialog(this, "Motocicleta registrada. ID: " + s.getId(),
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }

        cargarTablaVehiculos();
        limpiarFormulario();

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    } finally {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }
    }//GEN-LAST:event_GuardarActionPerformed

    private void GuardarStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_GuardarStockActionPerformed

    int row = TablaVehiculos.getSelectedRow();
    if (row < 0) {
        javax.swing.JOptionPane.showMessageDialog(this, "Selecciona un vehículo en la tabla.");
        return;
    }
    // id está en la primera columna
    Object idVal = TablaVehiculos.getValueAt(row, 0);
    if (idVal == null) {
        javax.swing.JOptionPane.showMessageDialog(this, "No se pudo obtener el ID del vehículo.");
        return;
    }
    vehiculoSeleccionadoId = (idVal instanceof Number) ? ((Number) idVal).longValue()
                                                       : Long.valueOf(idVal.toString());
    setModoAjusteStock(true);

    }//GEN-LAST:event_GuardarStockActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NeevosCarros.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NeevosCarros.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NeevosCarros.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NeevosCarros.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NeevosCarros().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner Año;
    private javax.swing.JButton Guardar;
    private javax.swing.JButton GuardarStock;
    private javax.swing.JTextField Marca;
    private javax.swing.JTextField Modelo;
    private javax.swing.JComboBox<String> Motor;
    private javax.swing.JSpinner Precio;
    private javax.swing.JSpinner Stock;
    private javax.swing.JTable TablaVehiculos;
    private javax.swing.JComboBox<String> Tipo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
