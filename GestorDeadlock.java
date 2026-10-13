import java.util.Arrays;

public class GestorDeadlock {
    private int numProcesos;
    private int numRecursos;
    
    private int[][] asignacion; // Matriz Allocation
    private int[][] maximo;     // Matriz Max
    private int[][] necesidad;  // Matriz Need (Max - Allocation)
    private int[] disponibles;  // Vector Available

    public GestorDeadlock(int numProcesos, int numRecursos, int[][] asignacion, int[][] maximo, int[] disponibles) {
        this.numProcesos = numProcesos;
        this.numRecursos = numRecursos;
        this.asignacion = asignacion;
        this.maximo = maximo;
        this.disponibles = disponibles.clone();
        this.necesidad = new int[numProcesos][numRecursos];

        // Cálculo de la Matriz de Necesidad: Need[i][j] = Max[i][j] - Allocation[i][j]
        calcularMatrizNecesidad();
    }

    private void calcularMatrizNecesidad() {
        for (int i = 0; i < numProcesos; i++) {
            for (int j = 0; j < numRecursos; j++) {
                necesidad[i][j] = maximo[i][j] - asignacion[i][j];
            }
        }
    }

    // Algoritmo del Banquero para evaluar si el sistema está en Estado Seguro
    public boolean evaluarEstadoSeguro() {
        int[] trabajo = disponibles.clone(); // Vector Work
        boolean[] finalizado = new boolean[numProcesos]; // Vector Finish
        int[] secuenciaSegura = new int[numProcesos];
        int contador = 0;

        System.out.println("\n>>> EVALUANDO ALGORITMO DEL BANQUERO <<<");
        System.out.println("Vector Disponibles Inicial (Available): " + Arrays.toString(trabajo));

        while (contador < numProcesos) {
            boolean encontrado = false;

            for (int p = 0; p < numProcesos; p++) {
                if (!finalizado[p]) {
                    // Verificar si Necesidad <= Trabajo para todos los recursos
                    boolean sePuedeEjecutar = true;
                    for (int r = 0; r < numRecursos; r++) {
                        if (necesidad[p][r] > trabajo[r]) {
                            sePuedeEjecutar = false;
                            break;
                        }
                    }

                    // Si se puede ejecutar, el proceso simula terminar y libera sus recursos
                    if (sePuedeEjecutar) {
                        for (int r = 0; r < numRecursos; r++) {
                            trabajo[r] += asignacion[p][r];
                        }
                        secuenciaSegura[contador++] = p;
                        finalizado[p] = true;
                        encontrado = true;
                        System.out.println("Proceso P" + p + " se ejecutó y liberó recursos. Nuevo Disponible: " + Arrays.toString(trabajo));
                    }
                }
            }

            // Si en una ronda ningún proceso pudo ejecutarse, hay riesgo de Interbloqueo / Estado Inseguro
            if (!encontrado) {
                System.out.println("\n[ALERTA DE DEADLOCK] El sistema está en un ESTADO INSEGURO / INTERBLOQUEADO.");
                imprimirProcesosBloqueados(finalizado);
                return false;
            }
        }

        System.out.println("\n[SISTEMA SEGURO] No hay interbloqueos.");
        System.out.print("Secuencia Segura de Ejecución: < ");
        for (int i = 0; i < numProcesos; i++) {
            System.out.print("P" + secuenciaSegura[i] + (i < numProcesos - 1 ? ", " : " "));
        }
        System.out.println(">");
        return true;
    }

    private void imprimirProcesosBloqueados(boolean[] finalizado) {
        System.out.print("Procesos bloqueados en la cadena de espera: ");
        for (int i = 0; i < numProcesos; i++) {
            if (!finalizado[i]) {
                System.out.print("P" + i + " ");
            }
        }
        System.out.println();
    }

    public void mostrarMatrices() {
        System.out.println("\n--- MATRIZ DE NECESIDAD (NEED) ---");
        for (int i = 0; i < numProcesos; i++) {
            System.out.print("P" + i + ": ");
            System.out.println(Arrays.toString(necesidad[i]));
        }
    }
}