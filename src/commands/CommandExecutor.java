package commands;

import database.DatabaseOperations;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Ejecutor de comandos del sistema
 * Procesa todos los casos de uso (CU1-CU8)
 */
public class CommandExecutor {
    
    public CommandResult ejecutar(CommandParser parser) {
        String comando = parser.getComando();
        
        try {
            switch (comando) {
                // CU1 - Gestión de Usuarios
                case "LISTUSU": return listarUsuarios(parser);
                case "INSUSU": return insertarUsuario(parser);
                case "MODUSU": return modificarUsuario(parser);
                case "ELIMUSU": return eliminarUsuario(parser);
                
                // CU2 - Gestión de Vehículos
                case "LISTVEH": return listarVehiculos(parser);
                case "INSVEH": return insertarVehiculo(parser);
                case "MODVEH": return modificarVehiculo(parser);
                case "UPDGPS": return actualizarGPS(parser);
                
                // CU3 - Gestión de Destinos
                case "LISTDES": return listarDestinos(parser);
                case "INSDES": return insertarDestino(parser);
                case "MODDES": return modificarDestino(parser);
                
                // CU4 - Gestión de Rutas
                case "LISTRUT": return CommandExecutorParte2.listarRutas(parser);
                case "INSRUT": return CommandExecutorParte2.insertarRuta(parser);
                case "MODRUT": return CommandExecutorParte2.modificarRuta(parser);
                
                // CU5 - Gestión de Paquetes
                case "LISTPAQ": return CommandExecutorParte2.listarPaquetes(parser);
                case "INSPAQ": return CommandExecutorParte2.insertarPaquete(parser);
                case "MODPAQ": return CommandExecutorParte2.modificarPaquete(parser);
                case "ENTPAQ": return CommandExecutorParte2.entregarPaquete(parser);
                
                // CU6 - Gestión de Seguimiento
                case "LISTSEG": return CommandExecutorParte2.listarSeguimiento(parser);
                case "INSSEG": return CommandExecutorParte2.insertarSeguimiento(parser);
                
                // CU7 - Gestión de Pagos
                case "LISTPAG": return CommandExecutorParte2.listarPagos(parser);
                case "INSPAG": return CommandExecutorParte2.insertarPago(parser);
                case "REGPAG": return CommandExecutorParte2.registrarPago(parser);
                
                // CU8 - Reportes y Estadísticas
                case "REPDIA": return CommandExecutorParte2.reporteDiario(parser);
                case "REPPAQ": return CommandExecutorParte2.reportePaquetes(parser);
                case "REPCLI": return CommandExecutorParte2.reporteCliente(parser);
                case "REPVEH": return CommandExecutorParte2.reporteVehiculos(parser);
                case "REPRUT": return CommandExecutorParte2.reporteRutas(parser);
                
                default:
                    return CommandResult.error("Comando '" + comando + "' no reconocido.\n" +
                            "Comandos disponibles: LISTUSU, INSUSU, LISTVEH, LISTPAQ, etc.");
            }
        } catch (SQLException e) {
            return CommandResult.error("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            return CommandResult.error("Error inesperado: " + e.getMessage());
        }
    }
    
    // ============================================
    // CU1 - GESTIÓN DE USUARIOS
    // ============================================
    
    private CommandResult listarUsuarios(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.isEmpty()) {
            return CommandResult.error("Falta parámetro. Use: LISTUSU[\"*\"] o LISTUSU[\"CLIENTE\"]");
        }
        
        String filtro = params.get(0);
        String sql;
        List<Map<String, Object>> resultados;
        
        if ("*".equals(filtro)) {
            sql = "SELECT * FROM usuarios ORDER BY id_usuario";
            resultados = DatabaseOperations.executeQuery(sql);
        } else {
            sql = "SELECT * FROM usuarios WHERE tipo_usuario = ? ORDER BY id_usuario";
            resultados = DatabaseOperations.executeQuery(sql, filtro);
        }
        
        String datos = DatabaseOperations.formatResultsAsText(resultados);
        return CommandResult.exito("Usuarios listados correctamente", datos);
    }
    
    private CommandResult insertarUsuario(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 6) {
            return CommandResult.error("Parámetros insuficientes. Se requieren: " +
                    "ci, nombre, apellido, tipo_usuario, telefono, email [, direccion]");
        }
        
        String ci = params.get(0);
        String nombre = params.get(1);
        String apellido = params.get(2);
        String tipoUsuario = params.get(3);
        String telefono = params.get(4);
        String email = params.get(5);
        String direccion = params.size() > 6 ? params.get(6) : null;
        
        // Validar tipo de usuario
        if (!tipoUsuario.matches("PROPIETARIO|CHOFER|SECRETARIA|CLIENTE")) {
            return CommandResult.error("tipo_usuario inválido. Use: PROPIETARIO, CHOFER, SECRETARIA o CLIENTE");
        }
        
        String sql = "INSERT INTO usuarios (ci, nombre, apellido, tipo_usuario, telefono, email, direccion) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        int id = DatabaseOperations.executeInsertWithGeneratedKey(sql, 
                ci, nombre, apellido, tipoUsuario, telefono, email, direccion);
        
        return CommandResult.exito("Usuario insertado exitosamente con ID: " + id,
                String.format("CI: %s\nNombre: %s %s\nEmail: %s\nTipo: %s", 
                    ci, nombre, apellido, email, tipoUsuario));
    }
    
    private CommandResult modificarUsuario(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 2) {
            return CommandResult.error("Parámetros insuficientes. Use: MODUSU[\"id\",\"campo\",\"valor\",...]");
        }
        
        String id = params.get(0);
        
        // Construir UPDATE dinámico
        StringBuilder sql = new StringBuilder("UPDATE usuarios SET ");
        List<Object> valores = new java.util.ArrayList<>();
        
        for (int i = 1; i < params.size(); i += 2) {
            if (i + 1 < params.size()) {
                if (i > 1) sql.append(", ");
                sql.append(params.get(i)).append(" = ?");
                valores.add(params.get(i + 1));
            }
        }
        
        sql.append(" WHERE id_usuario = ?");
        valores.add(id);
        
        int filas = DatabaseOperations.executeUpdate(sql.toString(), valores.toArray());
        
        if (filas > 0) {
            return CommandResult.exito("Usuario modificado exitosamente", 
                    "Filas afectadas: " + filas);
        } else {
            return CommandResult.error("No se encontró el usuario con ID: " + id);
        }
    }
    
    private CommandResult eliminarUsuario(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.isEmpty()) {
            return CommandResult.error("Falta parámetro. Use: ELIMUSU[\"id\"]");
        }
        
        String id = params.get(0);
        String sql = "UPDATE usuarios SET estado = 'INACTIVO' WHERE id_usuario = ?";
        
        int filas = DatabaseOperations.executeUpdate(sql, id);
        
        if (filas > 0) {
            return CommandResult.exito("Usuario eliminado (inactivado) exitosamente",
                    "ID Usuario: " + id);
        } else {
            return CommandResult.error("No se encontró el usuario con ID: " + id);
        }
    }
    
    // ============================================
    // CU2 - GESTIÓN DE VEHÍCULOS
    // ============================================
    
    private CommandResult listarVehiculos(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.isEmpty()) {
            return CommandResult.error("Falta parámetro. Use: LISTVEH[\"*\"] o LISTVEH[\"DISPONIBLE\"]");
        }
        
        String filtro = params.get(0);
        String sql;
        List<Map<String, Object>> resultados;
        
        if ("*".equals(filtro)) {
            sql = "SELECT v.*, u.nombre || ' ' || u.apellido as chofer " +
                  "FROM vehiculos v LEFT JOIN usuarios u ON v.id_chofer = u.id_usuario " +
                  "ORDER BY v.id_vehiculo";
            resultados = DatabaseOperations.executeQuery(sql);
        } else {
            sql = "SELECT v.*, u.nombre || ' ' || u.apellido as chofer " +
                  "FROM vehiculos v LEFT JOIN usuarios u ON v.id_chofer = u.id_usuario " +
                  "WHERE v.estado = ? ORDER BY v.id_vehiculo";
            resultados = DatabaseOperations.executeQuery(sql, filtro);
        }
        
        String datos = DatabaseOperations.formatResultsAsText(resultados);
        return CommandResult.exito("Vehículos listados correctamente", datos);
    }
    
    private CommandResult insertarVehiculo(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 6) {
            return CommandResult.error("Parámetros insuficientes. Se requieren: " +
                    "placa, marca, modelo, año, capacidad_carga, tipo_vehiculo [, id_chofer, foto_url, gps_activo]");
        }
        
        String placa = params.get(0);
        String marca = params.get(1);
        String modelo = params.get(2);
        String anio = params.get(3);
        String capacidad = params.get(4);
        String tipo = params.get(5);
        String idChofer = params.size() > 6 ? params.get(6) : null;
        String fotoUrl = params.size() > 7 ? params.get(7) : null;
        String gpsActivo = params.size() > 8 ? params.get(8) : "TRUE";
        
        String sql = "INSERT INTO vehiculos (placa, marca, modelo, año, capacidad_carga, " +
                     "tipo_vehiculo, id_chofer, foto_url, gps_activo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        int id = DatabaseOperations.executeInsertWithGeneratedKey(sql, 
                placa, marca, modelo, anio, capacidad, tipo, idChofer, fotoUrl, gpsActivo);
        
        return CommandResult.exito("Vehículo insertado exitosamente con ID: " + id,
                String.format("Placa: %s\nMarca: %s %s\nTipo: %s", placa, marca, modelo, tipo));
    }
    
    private CommandResult modificarVehiculo(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 2) {
            return CommandResult.error("Parámetros insuficientes. Use: MODVEH[\"id\",\"estado\"]");
        }
        
        String id = params.get(0);
        String estado = params.get(1);
        
        String sql = "UPDATE vehiculos SET estado = ? WHERE id_vehiculo = ?";
        int filas = DatabaseOperations.executeUpdate(sql, estado, id);
        
        if (filas > 0) {
            return CommandResult.exito("Vehículo modificado exitosamente",
                    "ID: " + id + ", Nuevo estado: " + estado);
        } else {
            return CommandResult.error("No se encontró el vehículo con ID: " + id);
        }
    }
    
    private CommandResult actualizarGPS(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 3) {
            return CommandResult.error("Parámetros insuficientes. Use: UPDGPS[\"id\",\"latitud\",\"longitud\"]");
        }
        
        String id = params.get(0);
        String latitud = params.get(1);
        String longitud = params.get(2);
        
        String sql = "UPDATE vehiculos SET latitud_actual = ?, longitud_actual = ?, " +
                     "ultima_actualizacion_gps = CURRENT_TIMESTAMP WHERE id_vehiculo = ?";
        
        int filas = DatabaseOperations.executeUpdate(sql, latitud, longitud, id);
        
        if (filas > 0) {
            return CommandResult.exito("GPS actualizado exitosamente",
                    String.format("Vehículo ID: %s\nLatitud: %s\nLongitud: %s", id, latitud, longitud));
        } else {
            return CommandResult.error("No se encontró el vehículo con ID: " + id);
        }
    }
    
    // ============================================
    // CU3 - GESTIÓN DE DESTINOS
    // ============================================
    
    private CommandResult listarDestinos(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.isEmpty()) {
            return CommandResult.error("Falta parámetro. Use: LISTDES[\"*\"] o LISTDES[\"ciudad\"]");
        }
        
        String filtro = params.get(0);
        String sql;
        List<Map<String, Object>> resultados;
        
        if ("*".equals(filtro)) {
            sql = "SELECT * FROM destinos ORDER BY id_destino";
            resultados = DatabaseOperations.executeQuery(sql);
        } else {
            sql = "SELECT * FROM destinos WHERE ciudad = ? ORDER BY id_destino";
            resultados = DatabaseOperations.executeQuery(sql, filtro);
        }
        
        String datos = DatabaseOperations.formatResultsAsText(resultados);
        return CommandResult.exito("Destinos listados correctamente", datos);
    }
    
    private CommandResult insertarDestino(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 7) {
            return CommandResult.error("Parámetros insuficientes. Se requieren: " +
                    "nombre_destino, direccion, ciudad, departamento, pais, latitud, longitud [, descripcion]");
        }
        
        String nombre = params.get(0);
        String direccion = params.get(1);
        String ciudad = params.get(2);
        String departamento = params.get(3);
        String pais = params.get(4);
        String latitud = params.get(5);
        String longitud = params.get(6);
        String descripcion = params.size() > 7 ? params.get(7) : null;
        
        String sql = "INSERT INTO destinos (nombre_destino, direccion, ciudad, departamento, " +
                     "pais, latitud, longitud, descripcion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        int id = DatabaseOperations.executeInsertWithGeneratedKey(sql,
                nombre, direccion, ciudad, departamento, pais, latitud, longitud, descripcion);
        
        return CommandResult.exito("Destino insertado exitosamente con ID: " + id,
                String.format("Nombre: %s\nCiudad: %s, %s", nombre, ciudad, departamento));
    }
    
    private CommandResult modificarDestino(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 3) {
            return CommandResult.error("Parámetros insuficientes. Use: MODDES[\"id\",\"campo\",\"valor\"]");
        }
        
        String id = params.get(0);
        String campo = params.get(1);
        String valor = params.get(2);
        
        String sql = "UPDATE destinos SET " + campo + " = ? WHERE id_destino = ?";
        int filas = DatabaseOperations.executeUpdate(sql, valor, id);
        
        if (filas > 0) {
            return CommandResult.exito("Destino modificado exitosamente",
                    "ID: " + id + ", Campo: " + campo);
        } else {
            return CommandResult.error("No se encontró el destino con ID: " + id);
        }
    }
}
