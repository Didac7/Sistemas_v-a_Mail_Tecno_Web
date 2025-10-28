package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase para realizar operaciones en la base de datos
 */
public class DatabaseOperations {
    
    /**
     * Ejecuta una consulta SELECT y retorna los resultados
     */
    public static List<Map<String, Object>> executeQuery(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> resultados = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Asignar parámetros
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            // Ejecutar query
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                // Procesar resultados
                while (rs.next()) {
                    Map<String, Object> fila = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        fila.put(columnName, value);
                    }
                    resultados.add(fila);
                }
            }
        }
        
        return resultados;
    }
    
    /**
     * Ejecuta una sentencia INSERT, UPDATE o DELETE
     * Retorna el número de filas afectadas
     */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Asignar parámetros
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            // Ejecutar update
            return stmt.executeUpdate();
        }
    }
    
    /**
     * Ejecuta un INSERT y retorna el ID generado
     */
    public static int executeInsertWithGeneratedKey(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Asignar parámetros
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            // Ejecutar insert
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Insert falló, no se afectaron filas.");
            }
            
            // Obtener ID generado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Insert falló, no se obtuvo ID.");
                }
            }
        }
    }
    
    /**
     * Verifica si existe un registro
     */
    public static boolean exists(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> result = executeQuery(sql, params);
        return !result.isEmpty();
    }
    
    /**
     * Cuenta registros
     */
    public static int count(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> result = executeQuery(sql, params);
        if (!result.isEmpty() && result.get(0).containsKey("count")) {
            return ((Number) result.get(0).get("count")).intValue();
        }
        return 0;
    }
    
    /**
     * Formatea resultados como texto para email
     */
    public static String formatResultsAsText(List<Map<String, Object>> resultados) {
        if (resultados.isEmpty()) {
            return "No se encontraron resultados.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Total de registros: ").append(resultados.size()).append("\n\n");
        
        for (int i = 0; i < resultados.size(); i++) {
            sb.append("─────────────────────────────────────\n");
            sb.append("Registro #").append(i + 1).append(":\n");
            sb.append("─────────────────────────────────────\n");
            
            Map<String, Object> fila = resultados.get(i);
            for (Map.Entry<String, Object> entry : fila.entrySet()) {
                sb.append(String.format("  %-25s: %s\n", 
                    entry.getKey(), 
                    entry.getValue() != null ? entry.getValue().toString() : "NULL"));
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
