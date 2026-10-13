import java.util.ArrayList;
import java.util.List;

public class GestorMemoria {
    private List<BloqueMemoria> particiones;

    public GestorMemoria(int[] tamañosParticiones) {
        this.particiones = new ArrayList<>();
        for (int i = 0; i < tamañosParticiones.length; i++) {
            this.particiones.add(new BloqueMemoria(i + 1, tamañosParticiones[i]));
        }
    }

    public List<BloqueMemoria> getParticiones() {
        return particiones;
    }

    // 1. First Fit (Primer Ajuste)
    public boolean asignarFirstFit(Proceso p) {
        for (BloqueMemoria bloque : particiones) {
            if (!bloque.isOcupado() && bloque.getTamañoKB() >= p.getTamañoMemoriaMB()) {
                bloque.asignarProceso(p.getPid(), p.getTamañoMemoriaMB());
                return true;
            }
        }
        return false; // No cupo en ninguna partición
    }

    // 2. Best Fit (Mejor Ajuste)
    public boolean asignarBestFit(Proceso p) {
        int mejorIdx = -1;
        int menorEspacioSobrante = Integer.MAX_VALUE;

        for (int i = 0; i < particiones.size(); i++) {
            BloqueMemoria b = particiones.get(i);
            if (!b.isOcupado() && b.getTamañoKB() >= p.getTamañoMemoriaMB()) {
                int sobrante = b.getTamañoKB() - p.getTamañoMemoriaMB();
                if (sobrante < menorEspacioSobrante) {
                    menorEspacioSobrante = sobrante;
                    mejorIdx = i;
                }
            }
        }

        if (mejorIdx != -1) {
            particiones.get(mejorIdx).asignarProceso(p.getPid(), p.getTamañoMemoriaMB());
            return true;
        }
        return false;
    }

    // 3. Worst Fit (Peor Ajuste)
    public boolean asignarWorstFit(Proceso p) {
        int peorIdx = -1;
        int mayorEspacioSobrante = -1;

        for (int i = 0; i < particiones.size(); i++) {
            BloqueMemoria b = particiones.get(i);
            if (!b.isOcupado() && b.getTamañoKB() >= p.getTamañoMemoriaMB()) {
                int sobrante = b.getTamañoKB() - p.getTamañoMemoriaMB();
                if (sobrante > mayorEspacioSobrante) {
                    mayorEspacioSobrante = sobrante;
                    peorIdx = i;
                }
            }
        }

        if (peorIdx != -1) {
            particiones.get(peorIdx).asignarProceso(p.getPid(), p.getTamañoMemoriaMB());
            return true;
        }
        return false;
    }

    // Liberar memoria cuando un proceso termina
    public void liberarProceso(String pid) {
        for (BloqueMemoria b : particiones) {
            if (b.isOcupado() && b.getPidProcesoAsignado().equals(pid)) {
                b.liberar();
            }
        }
    }

    // Reporte visual en consola
    public void mostrarEstadoMemoria() {
        System.out.println("\n================ MAPA DE MEMORIA RAM ================");
        System.out.printf("%-10s | %-10s | %-10s | %-10s | %-15s\n", 
                "Partición", "Tamaño KB", "Estado", "Proceso", "Frag. Interna");
        System.out.println("-------------------------------------------------------------------");
        
        int fragInternaTotal = 0;
        for (BloqueMemoria b : particiones) {
            System.out.printf("Bloque %-4d | %-10d | %-10s | %-10s | %-15d\n",
                    b.getId(), b.getTamañoKB(), 
                    (b.isOcupado() ? "OCUPADO" : "LIBRE"), 
                    b.getPidProcesoAsignado(), 
                    b.getFragmentacionInterna());
            
            fragInternaTotal += b.getFragmentacionInterna();
        }
        System.out.println("-------------------------------------------------------------------");
        System.out.println("FRAGMENTACIÓN INTERNA TOTAL ACUMULADA: " + fragInternaTotal + " KB");
        System.out.println("===================================================================\n");
    }

    public void reiniciarMemoria() {
        for (BloqueMemoria b : particiones) {
            b.liberar();
        }
    }
}