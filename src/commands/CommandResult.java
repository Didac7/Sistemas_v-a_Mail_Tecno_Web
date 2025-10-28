package commands;

/**
 * Clase que encapsula el resultado de la ejecución de un comando
 */
public class CommandResult {
    
    private boolean exito;
    private String mensaje;
    private String datos;
    private int filasAfectadas;
    
    public CommandResult(boolean exito, String mensaje, String datos) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.datos = datos;
        this.filasAfectadas = 0;
    }
    
    public CommandResult(boolean exito, String mensaje) {
        this(exito, mensaje, "");
    }
    
    public static CommandResult exito(String mensaje, String datos) {
        return new CommandResult(true, mensaje, datos);
    }
    
    public static CommandResult exito(String mensaje) {
        return new CommandResult(true, mensaje, "");
    }
    
    public static CommandResult error(String mensaje) {
        return new CommandResult(false, mensaje, "");
    }
    
    public boolean isExito() {
        return exito;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public String getDatos() {
        return datos;
    }
    
    public int getFilasAfectadas() {
        return filasAfectadas;
    }
    
    public void setFilasAfectadas(int filasAfectadas) {
        this.filasAfectadas = filasAfectadas;
    }
}
