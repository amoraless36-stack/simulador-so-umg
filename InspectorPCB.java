public class InspectorPCB {

    public static void simularCambioDeContexto(Proceso sale, Proceso entra, int pcActual) {
        System.out.println("\n================ SIMULADOR DE CAMBIO DE CONTEXTO (CONTEXT SWITCH) ================");
        
        if (sale != null) {
            sale.setEstado("BLOQUEADO / LISTO");
            System.out.println("[GUARDANDO ESTADO]");
            System.out.println(" -> Guardando Registros del CPU de " + sale.getPid() + " en su PCB.");
            System.out.println(" -> Guardando Contador de Programa (PC = " + pcActual + ") de " + sale.getPid());
            System.out.println(" -> Transición del proceso " + sale.getPid() + ": EJECUCION -> " + sale.getEstado());
        }

        if (entra != null) {
            entra.setEstado("EJECUCION");
            System.out.println("\n[CARGANDO ESTADO]");
            System.out.println(" -> Cargando Registros de CPU desde el PCB de " + entra.getPid());
            System.out.println(" -> Restableciendo Contador de Programa (PC) para " + entra.getPid());
            System.out.println(" -> Transición del proceso " + entra.getPid() + ": LISTO -> EJECUCION");
        }
        System.out.println("==================================================================================\n");
    }

    public static void mostrarPCB(Proceso p, int pc) {
        System.out.println("\n+------------------------------------------------------+");
        System.out.println("|         INSPECTOR DE BLOQUE DE CONTROL (PCB)         |");
        System.out.println("+------------------------------------------------------+");
        System.out.printf("| PID Process ID          : %-26s |\n", p.getPid());
        System.out.printf("| Estado Actual           : %-26s |\n", p.getEstado());
        System.out.printf("| Program Counter (PC)    : 0x%04X (Instrucción %-2d) |\n", pc, pc);
        System.out.printf("| Prioridad               : %-26d |\n", p.getPrioridad());
        System.out.printf("| Memoria Asignada        : %-23d KB |\n", p.getTamañoMemoriaMB());
        System.out.printf("| Ráfaga Restante CPU     : %-23d ms |\n", p.getTiempoRestante());
        System.out.println("+------------------------------------------------------+");
    }
}