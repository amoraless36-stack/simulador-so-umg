import java.util.*;

public class PlanificadorCPU {

    // Clase auxiliar para registrar qué proceso se ejecutó en cada unidad de tiempo (para el Gantt)
    public static class BloqueGantt {
        public int tiempoInicio;
        public int tiempoFin;
        public String pid;

        public BloqueGantt(int tiempoInicio, int tiempoFin, String pid) {
            this.tiempoInicio = tiempoInicio;
            this.tiempoFin = tiempoFin;
            this.pid = pid;
        }
    }

    // 1. Algoritmo FCFS / FIFO
    public static List<BloqueGantt> simularFCFS(List<Proceso> procesos) {
        // Ordenar directamente la lista recibida por tiempo de llegada
        procesos.sort(Comparator.comparingInt(Proceso::getTiempoLlegada));
        
        List<BloqueGantt> diagramagantt = new ArrayList<>();
        int tiempoActual = 0;

        for (Proceso p : procesos) {
            if (tiempoActual < p.getTiempoLlegada()) {
                diagramagantt.add(new BloqueGantt(tiempoActual, p.getTiempoLlegada(), "IDLE"));
                tiempoActual = p.getTiempoLlegada();
            }

            p.setTiempoInicio(tiempoActual);
            p.setTiempoRespuesta(tiempoActual - p.getTiempoLlegada());
            
            tiempoActual += p.getTiempoRafaga();
            p.setTiempoFinalizacion(tiempoActual);
            p.calcularTiemposFinales();

            diagramagantt.add(new BloqueGantt(p.getTiempoInicio(), p.getTiempoFinalizacion(), p.getPid()));
        }
        imprimirReporte(procesos);
        return diagramagantt;
    }

    // 2. Algoritmo SJF (Shortest Job First - No Expropiativo)
    public static List<BloqueGantt> simularSJF(List<Proceso> procesos) {
        List<BloqueGantt> diagramagantt = new ArrayList<>();
        int tiempoActual = 0;
        int completados = 0;
        int n = procesos.size();
        boolean[] visitado = new boolean[n];

        while (completados < n) {
            int idx = -1;
            int minRafaga = Integer.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                Proceso p = procesos.get(i);
                if (p.getTiempoLlegada() <= tiempoActual && !visitado[i]) {
                    if (p.getTiempoRafaga() < minRafaga) {
                        minRafaga = p.getTiempoRafaga();
                        idx = i;
                    }
                }
            }

            if (idx == -1) {
                // Verificar si falta algún proceso por llegar
                int proximaLlegada = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (!visitado[i] && procesos.get(i).getTiempoLlegada() < proximaLlegada) {
                        proximaLlegada = procesos.get(i).getTiempoLlegada();
                    }
                }
                if (proximaLlegada != Integer.MAX_VALUE) {
                    diagramagantt.add(new BloqueGantt(tiempoActual, proximaLlegada, "IDLE"));
                    tiempoActual = proximaLlegada;
                } else {
                    tiempoActual++;
                }
            } else {
                Proceso p = procesos.get(idx);
                p.setTiempoInicio(tiempoActual);
                p.setTiempoRespuesta(tiempoActual - p.getTiempoLlegada());
                
                tiempoActual += p.getTiempoRafaga();
                p.setTiempoFinalizacion(tiempoActual);
                p.calcularTiemposFinales();

                diagramagantt.add(new BloqueGantt(p.getTiempoInicio(), p.getTiempoFinalizacion(), p.getPid()));
                visitado[idx] = true;
                completados++;
            }
        }
        imprimirReporte(procesos);
        return diagramagantt;
    }

    // 3. Algoritmo Round Robin (RR)
    public static List<BloqueGantt> simularRoundRobin(List<Proceso> procesos, int quantum) {
        List<BloqueGantt> diagramagantt = new ArrayList<>();
        Queue<Proceso> colaListos = new LinkedList<>();
        
        // Reiniciar variables temporales para Round Robin
        for (Proceso p : procesos) {
            p.setTiempoRestante(p.getTiempoRafaga());
            p.setYaInicio(false);
        }

        procesos.sort(Comparator.comparingInt(Proceso::getTiempoLlegada));
        int tiempoActual = 0;
        int completados = 0;
        int n = procesos.size();
        int i = 0;

        // Agregar los primeros en llegar
        while (i < n && procesos.get(i).getTiempoLlegada() <= tiempoActual) {
            colaListos.add(procesos.get(i));
            i++;
        }

        while (completados < n) {
            if (colaListos.isEmpty()) {
                if (i < n) {
                    int proximaLlegada = procesos.get(i).getTiempoLlegada();
                    diagramagantt.add(new BloqueGantt(tiempoActual, proximaLlegada, "IDLE"));
                    tiempoActual = proximaLlegada;
                    while (i < n && procesos.get(i).getTiempoLlegada() <= tiempoActual) {
                        colaListos.add(procesos.get(i));
                        i++;
                    }
                } else {
                    tiempoActual++;
                }
                continue;
            }

            Proceso p = colaListos.poll();

            if (!p.isYaInicio()) {
                p.setTiempoInicio(tiempoActual);
                p.setTiempoRespuesta(tiempoActual - p.getTiempoLlegada());
                p.setYaInicio(true);
            }

            int tiempoEjecutado = Math.min(quantum, p.getTiempoRestante());
            diagramagantt.add(new BloqueGantt(tiempoActual, tiempoActual + tiempoEjecutado, p.getPid()));
            
            tiempoActual += tiempoEjecutado;
            p.setTiempoRestante(p.getTiempoRestante() - tiempoEjecutado);

            // Agregar procesos que llegaron durante este intervalo de tiempo
            while (i < n && procesos.get(i).getTiempoLlegada() <= tiempoActual) {
                colaListos.add(procesos.get(i));
                i++;
            }

            if (p.getTiempoRestante() > 0) {
                colaListos.add(p);
            } else {
                p.setTiempoFinalizacion(tiempoActual);
                p.calcularTiemposFinales();
                completados++;
            }
        }
        imprimirReporte(procesos);
        return diagramagantt;
    }

    // Genera la tabla formateada y calcula los promedios solicitados
    private static void imprimirReporte(List<Proceso> procesos) {
        System.out.println("\n---------------------------------------------------------------------------------------");
        System.out.printf("%-6s | %-5s | %-5s | %-8s | %-8s | %-8s | %-8s\n", 
                "PID", "T_ll", "T_r", "T_Fin", "T_Ret", "T_Esp", "T_Resp");
        System.out.println("---------------------------------------------------------------------------------------");
        
        double sumaRet = 0, sumaEsp = 0, sumaResp = 0;
        for (Proceso p : procesos) {
            System.out.printf("%-6s | %-5d | %-5d | %-8d | %-8d | %-8d | %-8d\n",
                    p.getPid(), p.getTiempoLlegada(), p.getTiempoRafaga(),
                    p.getTiempoFinalizacion(), p.getTiempoRetorno(), 
                    p.getTiempoEspera(), p.getTiempoRespuesta());
            
            sumaRet += p.getTiempoRetorno();
            sumaEsp += p.getTiempoEspera();
            sumaResp += p.getTiempoRespuesta();
        }
        int total = procesos.isEmpty() ? 1 : procesos.size();
        System.out.println("---------------------------------------------------------------------------------------");
        System.out.printf("PROMEDIOS: Retorno = %.2f | Espera = %.2f | Respuesta = %.2f\n", 
                (sumaRet / total), (sumaEsp / total), (sumaResp / total));
        System.out.println("---------------------------------------------------------------------------------------\n");
    }
}