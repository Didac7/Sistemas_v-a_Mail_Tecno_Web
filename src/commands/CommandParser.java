package commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser para analizar comandos desde el asunto del email
 * Formato: COMANDO["param1","param2","param3",...]
 */
public class CommandParser {
    
    private String comandoOriginal;
    private String comando;
    private List<String> parametros;
    private boolean valid;
    private String error;
    
    public CommandParser(String asunto) {
        this.comandoOriginal = asunto;
        this.parametros = new ArrayList<>();
        this.valid = false;
        parsear(asunto);
    }
    
    /**
     * Parsea el comando del asunto
     */
    private void parsear(String asunto) {
        if (asunto == null || asunto.trim().isEmpty()) {
            error = "El asunto está vacío";
            return;
        }
        
        // Patrón: COMANDO["param1","param2",...]
        Pattern pattern = Pattern.compile("^([A-Z]+)\\[(.*)\\]$");
        Matcher matcher = pattern.matcher(asunto.trim());
        
        if (!matcher.matches()) {
            error = "Formato de comando inválido. Use: COMANDO[\"param1\",\"param2\",...]";
            return;
        }
        
        comando = matcher.group(1);
        String parametrosStr = matcher.group(2);
        
        // Parsear parámetros
        if (!parametrosStr.isEmpty()) {
            // Patrón para extraer strings entre comillas
            Pattern paramPattern = Pattern.compile("\"([^\"]*)\"");
            Matcher paramMatcher = paramPattern.matcher(parametrosStr);
            
            while (paramMatcher.find()) {
                parametros.add(paramMatcher.group(1));
            }
        }
        
        valid = true;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String getComando() {
        return comando;
    }
    
    public List<String> getParametros() {
        return parametros;
    }
    
    public String getError() {
        return error;
    }
    
    public String getComandoOriginal() {
        return comandoOriginal;
    }
    
    /**
     * Obtiene un parámetro por índice
     */
    public String getParametro(int index) {
        if (index >= 0 && index < parametros.size()) {
            return parametros.get(index);
        }
        return null;
    }
    
    /**
     * Obtiene el número de parámetros
     */
    public int getNumeroParametros() {
        return parametros.size();
    }
    
    @Override
    public String toString() {
        return "CommandParser{" +
                "comando='" + comando + '\'' +
                ", parametros=" + parametros +
                ", valid=" + valid +
                '}';
    }
}
