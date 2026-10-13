public class BloqueMemoria {
    private int id;
    private int tamañoKB;
    private boolean ocupado;
    private String pidProcesoAsignado; // PID del proceso en ejecución
    private int fragmentacionInterna;

    public BloqueMemoria(int id, int tamañoKB) {
        this.id = id;
        this.tamañoKB = tamañoKB;
        this.ocupado = false;
        this.pidProcesoAsignado = "Libre";
        this.fragmentacionInterna = 0;
    }

    public void asignarProceso(String pid, int tamañoProceso) {
        this.ocupado = true;
        this.pidProcesoAsignado = pid;
        this.fragmentacionInterna = this.tamañoKB - tamañoProceso; // Fórmula de Fragmentación Interna
    }

    public void liberar() {
        this.ocupado = false;
        this.pidProcesoAsignado = "Libre";
        this.fragmentacionInterna = 0;
    }

    // Getters y Setters
    public int getId() { return id; }
    public int getTamañoKB() { return tamañoKB; }
    public boolean isOcupado() { return ocupado; }
    public String getPidProcesoAsignado() { return pidProcesoAsignado; }
    public int getFragmentacionInterna() { return fragmentacionInterna; }
}