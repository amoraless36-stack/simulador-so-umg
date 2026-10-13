public class Proceso {
    private String pid;
    private int tiempoLlegada;
    private int tiempoRafaga;
    private int prioridad;
    private int tamañoMemoriaMB;

    // Estado del proceso para Módulo PCB y Ciclo de Vida (Nuevo, Listo, Ejecucion, Bloqueado, Terminado)
    private String estado;

    // Tiempos calculados
    private int tiempoInicio;
    private int tiempoFinalizacion;
    private int tiempoRetorno;
    private int tiempoEspera;
    private int tiempoRespuesta;

    // Auxiliares para algoritmos (Round Robin, Gantt, etc.)
    private int tiempoRestante;
    private boolean yaInicio;

    // Constructor principal
    public Proceso(String pid, int tiempoLlegada, int tiempoRafaga, int prioridad, int tamañoMemoriaMB) {
        this.pid = pid;
        this.tiempoLlegada = tiempoLlegada;
        this.tiempoRafaga = tiempoRafaga;
        this.prioridad = prioridad;
        this.tamañoMemoriaMB = tamañoMemoriaMB;
        
        this.estado = "Nuevo";
        this.tiempoRestante = tiempoRafaga;
        this.yaInicio = false;
    }

    // --- MÉTODOS DE CÁLCULO ---
    public void calcularTiemposFinales() {
        this.tiempoRetorno = this.tiempoFinalizacion - this.tiempoLlegada;
        this.tiempoEspera = this.tiempoRetorno - this.tiempoRafaga;
    }

    // --- ESTADO DEL PROCESO (RESUELVE LOS ERRORES EN InspectorPCB) ---
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // --- GETTERS Y SETTERS PRINCIPALES ---
    public String getPid() { return pid; }
    public void setPid(String pid) { this.pid = pid; }

    public int getTiempoLlegada() { return tiempoLlegada; }
    public void setTiempoLlegada(int tiempoLlegada) { this.tiempoLlegada = tiempoLlegada; }

    public int getTiempoRafaga() { return tiempoRafaga; }
    public void setTiempoRafaga(int tiempoRafaga) { this.tiempoRafaga = tiempoRafaga; }

    public int getPrioridad() { return prioridad; }
    public void setPrioridad(int prioridad) { this.prioridad = prioridad; }

    public int getTamañoMemoriaMB() { return tamañoMemoriaMB; }
    public void setTamañoMemoriaMB(int tamañoMemoriaMB) { this.tamañoMemoriaMB = tamañoMemoriaMB; }

    public int getTiempoInicio() { return tiempoInicio; }
    public void setTiempoInicio(int tiempoInicio) { this.tiempoInicio = tiempoInicio; }

    public int getTiempoFinalizacion() { return tiempoFinalizacion; }
    public void setTiempoFinalizacion(int tiempoFinalizacion) { this.tiempoFinalizacion = tiempoFinalizacion; }

    public int getTiempoRetorno() { return tiempoRetorno; }
    public void setTiempoRetorno(int tiempoRetorno) { this.tiempoRetorno = tiempoRetorno; }

    public int getTiempoEspera() { return tiempoEspera; }
    public void setTiempoEspera(int tiempoEspera) { this.tiempoEspera = tiempoEspera; }

    public int getTiempoRespuesta() { return tiempoRespuesta; }
    public void setTiempoRespuesta(int tiempoRespuesta) { this.tiempoRespuesta = tiempoRespuesta; }

    public int getTiempoRestante() { return tiempoRestante; }
    public void setTiempoRestante(int tiempoRestante) { this.tiempoRestante = tiempoRestante; }

    public boolean isYaInicio() { return yaInicio; }
    public void setYaInicio(boolean yaInicio) { this.yaInicio = yaInicio; }
}