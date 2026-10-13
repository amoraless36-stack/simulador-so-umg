import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorConcurrencia {
    private int contadorCompartido;
    private final ReentrantLock mutex = new ReentrantLock();
    private final Semaphore semaforoBinario;
    private final Semaphore semaforoContador;

    public MonitorConcurrencia() {
        this.contadorCompartido = 0;
        this.semaforoBinario = new Semaphore(1);   // S = 1 (Mutex/Binario)
        this.semaforoContador = new Semaphore(3);  // S = n (Pool de 3 recursos)
    }

    public int getContadorCompartido() {
        return contadorCompartido;
    }

    // 1. Demostración de Condición de Carrera (SIN Sincronización)
    public void ejecutarSinSincronizacion(int numHilos, int incrementosPorHilo) {
        this.contadorCompartido = 0;
        Thread[] hilos = new Thread[numHilos];

        System.out.println("\n>>> MÓDULO 4: CONCURRENCIA - ESCENARIO SIN SINCRONIZACIÓN <<<");
        int valorEsperado = numHilos * incrementosPorHilo;

        for (int i = 0; i < numHilos; i++) {
            hilos[i] = new Thread(() -> {
                for (int j = 0; j < incrementosPorHilo; j++) {
                    int temp = contadorCompartido;
                    try { Thread.sleep(1); } catch (InterruptedException e) {}
                    contadorCompartido = temp + 1;
                }
            });
            hilos[i].start();
        }

        esperarHilos(hilos);

        System.out.println("Valor Esperado (Teórico) : " + valorEsperado);
        System.out.println("Valor Real Obtenido       : " + contadorCompartido);
        if (contadorCompartido != valorEsperado) {
            System.out.println("[RESULTADO]: ¡SE EVIDENCIÓ UNA CONDICIÓN DE CARRERA (Inconsistencia de datos)!");
        }
    }

    // 2. Demostración CON Sincronización (Exclusión Mutua con ReentrantLock)
    public void ejecutarConSincronizacion(int numHilos, int incrementosPorHilo) {
        this.contadorCompartido = 0;
        Thread[] hilos = new Thread[numHilos];

        System.out.println("\n>>> MÓDULO 4: CONCURRENCIA - ESCENARIO CON SINCRONIZACIÓN (MUTEX) <<<");
        int valorEsperado = numHilos * incrementosPorHilo;

        for (int i = 0; i < numHilos; i++) {
            hilos[i] = new Thread(() -> {
                for (int j = 0; j < incrementosPorHilo; j++) {
                    mutex.lock();
                    try {
                        contadorCompartido++;
                    } finally {
                        mutex.unlock();
                    }
                }
            });
            hilos[i].start();
        }

        esperarHilos(hilos);

        System.out.println("Valor Esperado (Teórico) : " + valorEsperado);
        System.out.println("Valor Real Obtenido       : " + contadorCompartido);
        System.out.println("[RESULTADO]: Exclusión mutua garantizada. Datos 100% consistentes.");
    }

    // 3. Uso explícito de Semáforo Binario (wait/signal)
    public void ejecutarConSemaforoBinario(int numHilos, int incrementosPorHilo) {
        this.contadorCompartido = 0;
        Thread[] hilos = new Thread[numHilos];

        System.out.println("\n>>> MÓDULO 4: CONCURRENCIA - ESCENARIO CON SEMÁFORO BINARIO (S=1) <<<");
        int valorEsperado = numHilos * incrementosPorHilo;

        for (int i = 0; i < numHilos; i++) {
            hilos[i] = new Thread(() -> {
                for (int j = 0; j < incrementosPorHilo; j++) {
                    try {
                        semaforoBinario.acquire(); // wait(mutex) - P()
                        contadorCompartido++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        semaforoBinario.release(); // signal(mutex) - V()
                    }
                }
            });
            hilos[i].start();
        }

        esperarHilos(hilos);

        System.out.println("Valor Esperado (Teórico) : " + valorEsperado);
        System.out.println("Valor Real Obtenido       : " + contadorCompartido);
        System.out.println("[RESULTADO]: Semáforo binario protegió la sección crítica con éxito.");
    }

    // 4. Simulación de Semáforo Contador paso a paso
    public void simularSemaforoContador() {
        System.out.println("\n>>> DEMOSTRACIÓN DE SEMÁFORO CONTADOR (Permisos Iniciales S = 3) <<<");
        System.out.printf("%-10s | %-20s | %-12s | %-20s\n", 
                "Paso", "Operación", "Valor S", "Estado");
        System.out.println("---------------------------------------------------------------------");

        for (int i = 1; i <= 5; i++) {
            boolean adquirido = semaforoContador.tryAcquire();
            int valorS = semaforoContador.availablePermits();
            String estado = adquirido ? "Acceso Concedido" : "BLOQUEADO (Sin Permisos)";
            System.out.printf("Paso %-5d | %-20s | S = %-8d | %-20s\n", 
                    i, "Hilo " + i + " pide recurso", valorS, estado);
        }

        System.out.println("\nLiberando 2 permisos...");
        semaforoContador.release(2);
        System.out.println("Nuevo valor del Semáforo S = " + semaforoContador.availablePermits());
    }

    private void esperarHilos(Thread[] hilos) {
        for (Thread h : hilos) {
            try {
                h.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}