import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Lanzar la interfaz gráfica en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}