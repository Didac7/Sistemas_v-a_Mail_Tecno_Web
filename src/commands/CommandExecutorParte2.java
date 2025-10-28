package commands;

import database.DatabaseOperations;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Parte 2 del CommandExecutor - CU4 a CU8
 * Continuación de las funciones de comandos
 */
public class CommandExecutorParte2 {
    
    // ============================================
    // CU4 - GESTIÓN DE RUTAS
    // ============================================
    
    public static CommandResult listarRutas(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.isEmpty()) {
            return CommandResult.error("Falta parámetro. Use: LISTRUT[\"*\"] o LISTRUT[\"ACTIVA\"]");
        }
        
        String filtro = params.get(0);
        String sql;
        List<Map<String, Object>> resultados;
        
        if ("*".equals(filtro)) {
            sql = "SELECT r.*, o.nombre_destino as origen, d.nombre_destino as destino " +
                  "FROM rutas r " +
                  "JOIN destinos o ON r.id_origen = o.id_destino " +
                  "JOIN destinos d ON r.id_destino = d.id_destino " +
                  "ORDER BY r.id_ruta";
            resultados = DatabaseOperations.executeQuery(sql);
        } else {
            sql = "SELECT r.*, o.nombre_destino as origen, d.nombre_destino as destino " +
                  "FROM rutas r " +
                  "JOIN destinos o ON r.id_origen = o.id_destino " +
                  "JOIN destinos d ON r.id_destino = d.id_destino " +
                  "WHERE r.estado = ? ORDER BY r.id_ruta";
            resultados = DatabaseOperations.executeQuery(sql, filtro);
        }
        
        String datos = DatabaseOperations.formatResultsAsText(resultados);
        return CommandResult.exito("Rutas listadas correctamente", datos);
    }
    
    public static CommandResult insertarRuta(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 6) {
            return CommandResult.error("Parámetros insuficientes. Se requieren: " +
                    "nombre_ruta, id_origen, id_destino, distancia_km, tiempo_estimado_horas, costo_base [, descripcion]");
        }
        
        String nombreRuta = params.get(0);
        String idOrigen = params.get(1);
        String idDestino = params.get(2);
        String distancia = params.get(3);
        String tiempo = params.get(4);
        String costo = params.get(5);
        String descripcion = params.size() > 6 ? params.get(6) : null;
        
        String sql = "INSERT INTO rutas (nombre_ruta, id_origen, id_destino, distancia_km, " +
                     "tiempo_estimado_horas, costo_base, descripcion_ruta) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        int id = DatabaseOperations.executeInsertWithGeneratedKey(sql,
                nombreRuta, idOrigen, idDestino, distancia, tiempo, costo, descripcion);
        
        return CommandResult.exito("Ruta insertada exitosamente con ID: " + id,
                String.format("Ruta: %s\nDistancia: %s km\nCosto: Bs. %s", nombreRuta, distancia, costo));
    }
    
    public static CommandResult modificarRuta(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 3) {
            return CommandResult.error("Parámetros insuficientes. Use: MODRUT[\"id\",\"distancia_km\",\"tiempo_est\"]");
        }
        
        String id = params.get(0);
        String distancia = params.get(1);
        String tiempo = params.get(2);
        
        String sql = "UPDATE rutas SET distancia_km = ?, tiempo_estimado_horas = ? WHERE id_ruta = ?";
        int filas = DatabaseOperations.executeUpdate(sql, distancia, tiempo, id);
        
        if (filas > 0) {
            return CommandResult.exito("Ruta modificada exitosamente",
                    String.format("ID: %s\nDistancia: %s km\nTiempo: %s hrs", id, distancia, tiempo));
        } else {
            return CommandResult.error("No se encontró la ruta con ID: " + id);
        }
    }
    
    // ============================================
    // CU5 - GESTIÓN DE PAQUETES
    // ============================================
    
    public static CommandResult listarPaquetes(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.isEmpty()) {
            return CommandResult.error("Falta parámetro. Use: LISTPAQ[\"*\"], LISTPAQ[\"EN_TRANSITO\"] o LISTPAQ[\"codigo\"]");
        }
        
        String filtro = params.get(0);
        String sql;
        List<Map<String, Object>> resultados;
        
        if ("*".equals(filtro)) {
            sql = "SELECT p.*, c.nombre || ' ' || c.apellido as cliente, v.placa " +
                  "FROM paquetes p " +
                  "JOIN usuarios c ON p.id_cliente = c.id_usuario " +
                  "LEFT JOIN vehiculos v ON p.id_vehiculo = v.id_vehiculo " +
                  "ORDER BY p.id_paquete DESC";
            resultados = DatabaseOperations.executeQuery(sql);
        } else if (filtro.startsWith("TRV-") || filtro.length() > 20) {
            // Buscar por código
            sql = "SELECT p.*, c.nombre || ' ' || c.apellido as cliente, v.placa " +
                  "FROM paquetes p " +
                  "JOIN usuarios c ON p.id_cliente = c.id_usuario " +
                  "LEFT JOIN vehiculos v ON p.id_vehiculo = v.id_vehiculo " +
                  "WHERE p.codigo_seguimiento = ?";
            resultados = DatabaseOperations.executeQuery(sql, filtro);
        } else {
            // Buscar por estado
            sql = "SELECT p.*, c.nombre || ' ' || c.apellido as cliente, v.placa " +
                  "FROM paquetes p " +
                  "JOIN usuarios c ON p.id_cliente = c.id_usuario " +
                  "LEFT JOIN vehiculos v ON p.id_vehiculo = v.id_vehiculo " +
                  "WHERE p.estado_paquete = ? ORDER BY p.id_paquete DESC";
            resultados = DatabaseOperations.executeQuery(sql, filtro);
        }
        
        String datos = DatabaseOperations.formatResultsAsText(resultados);
        return CommandResult.exito("Paquetes listados correctamente", datos);
    }
    
    public static CommandResult insertarPaquete(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 8) {
            return CommandResult.error("Parámetros insuficientes. Se requieren: " +
                    "codigo_seguimiento, id_cliente, id_ruta, id_vehiculo, descripcion, peso_kg, volumen_m3, valor_declarado [, tipo, prioridad]");
        }
        
        String codigo = params.get(0);
        String idCliente = params.get(1);
        String idRuta = params.get(2);
        String idVehiculo = params.get(3);
        String descripcion = params.get(4);
        String peso = params.get(5);
        String volumen = params.get(6);
        String valor = params.get(7);
        String tipo = params.size() > 8 ? params.get(8) : "GENERAL";
        String prioridad = params.size() > 9 ? params.get(9) : "NORMAL";
        
        String sql = "INSERT INTO paquetes (codigo_seguimiento, id_cliente, id_ruta, id_vehiculo, " +
                     "descripcion_contenido, peso_kg, volumen_m3, valor_declarado, tipo_paquete, prioridad) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        int id = DatabaseOperations.executeInsertWithGeneratedKey(sql,
                codigo, idCliente, idRuta, idVehiculo, descripcion, peso, volumen, valor, tipo, prioridad);
        
        return CommandResult.exito("Paquete registrado exitosamente con ID: " + id,
                String.format("Código: %s\nDescripción: %s\nPeso: %s kg\nValor: Bs. %s", 
                    codigo, descripcion, peso, valor));
    }
    
    public static CommandResult modificarPaquete(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 2) {
            return CommandResult.error("Parámetros insuficientes. Use: MODPAQ[\"id\",\"estado\"]");
        }
        
        String id = params.get(0);
        String estado = params.get(1);
        
        String sql = "UPDATE paquetes SET estado_paquete = ?, fecha_envio = CURRENT_TIMESTAMP WHERE id_paquete = ?";
        int filas = DatabaseOperations.executeUpdate(sql, estado, id);
        
        if (filas > 0) {
            return CommandResult.exito("Paquete modificado exitosamente",
                    "ID: " + id + ", Nuevo estado: " + estado);
        } else {
            return CommandResult.error("No se encontró el paquete con ID: " + id);
        }
    }
    
    public static CommandResult entregarPaquete(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.isEmpty()) {
            return CommandResult.error("Falta parámetro. Use: ENTPAQ[\"id\"]");
        }
        
        String id = params.get(0);
        
        String sql = "UPDATE paquetes SET estado_paquete = 'ENTREGADO', " +
                     "fecha_entrega_real = CURRENT_TIMESTAMP WHERE id_paquete = ?";
        int filas = DatabaseOperations.executeUpdate(sql, id);
        
        if (filas > 0) {
            return CommandResult.exito("Paquete entregado exitosamente",
                    "ID Paquete: " + id + "\nFecha de entrega: " + java.time.LocalDateTime.now());
        } else {
            return CommandResult.error("No se encontró el paquete con ID: " + id);
        }
    }
    
    // ============================================
    // CU6 - GESTIÓN DE SEGUIMIENTO
    // ============================================
    
    public static CommandResult listarSeguimiento(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.isEmpty()) {
            return CommandResult.error("Falta parámetro. Use: LISTSEG[\"id_paquete\"] o LISTSEG[\"codigo\"]");
        }
        
        String filtro = params.get(0);
        String sql;
        List<Map<String, Object>> resultados;
        
        if (filtro.startsWith("TRV-")) {
            // Buscar por código
            sql = "SELECT s.*, p.codigo_seguimiento, v.placa " +
                  "FROM seguimiento s " +
                  "JOIN paquetes p ON s.id_paquete = p.id_paquete " +
                  "LEFT JOIN vehiculos v ON s.id_vehiculo = v.id_vehiculo " +
                  "WHERE p.codigo_seguimiento = ? ORDER BY s.fecha_hora DESC";
            resultados = DatabaseOperations.executeQuery(sql, filtro);
        } else {
            // Buscar por ID
            sql = "SELECT s.*, p.codigo_seguimiento, v.placa " +
                  "FROM seguimiento s " +
                  "JOIN paquetes p ON s.id_paquete = p.id_paquete " +
                  "LEFT JOIN vehiculos v ON s.id_vehiculo = v.id_vehiculo " +
                  "WHERE s.id_paquete = ? ORDER BY s.fecha_hora DESC";
            resultados = DatabaseOperations.executeQuery(sql, filtro);
        }
        
        String datos = DatabaseOperations.formatResultsAsText(resultados);
        return CommandResult.exito("Seguimiento listado correctamente", datos);
    }
    
    public static CommandResult insertarSeguimiento(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 6) {
            return CommandResult.error("Parámetros insuficientes. Se requieren: " +
                    "id_paquete, id_vehiculo, estado, ubicacion, latitud, longitud [, descripcion, registrado_por]");
        }
        
        String idPaquete = params.get(0);
        String idVehiculo = params.get(1);
        String estado = params.get(2);
        String ubicacion = params.get(3);
        String latitud = params.get(4);
        String longitud = params.get(5);
        String descripcion = params.size() > 6 ? params.get(6) : null;
        String registradoPor = params.size() > 7 ? params.get(7) : null;
        
        String sql = "INSERT INTO seguimiento (id_paquete, id_vehiculo, estado_seguimiento, " +
                     "ubicacion_actual, latitud, longitud, descripcion, registrado_por) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        int id = DatabaseOperations.executeInsertWithGeneratedKey(sql,
                idPaquete, idVehiculo, estado, ubicacion, latitud, longitud, descripcion, registradoPor);
        
        return CommandResult.exito("Seguimiento registrado exitosamente con ID: " + id,
                String.format("Estado: %s\nUbicación: %s\nCoordenadas: %s, %s", 
                    estado, ubicacion, latitud, longitud));
    }
    
    // ============================================
    // CU7 - GESTIÓN DE PAGOS
    // ============================================
    
    public static CommandResult listarPagos(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.isEmpty()) {
            return CommandResult.error("Falta parámetro. Use: LISTPAG[\"*\"], LISTPAG[\"PENDIENTE\"] o LISTPAG[\"id_cliente\"]");
        }
        
        String filtro = params.get(0);
        String sql;
        List<Map<String, Object>> resultados;
        
        if ("*".equals(filtro)) {
            sql = "SELECT pg.*, p.codigo_seguimiento, c.nombre || ' ' || c.apellido as cliente " +
                  "FROM pagos pg " +
                  "JOIN paquetes p ON pg.id_paquete = p.id_paquete " +
                  "JOIN usuarios c ON pg.id_cliente = c.id_usuario " +
                  "ORDER BY pg.id_pago DESC";
            resultados = DatabaseOperations.executeQuery(sql);
        } else if (filtro.matches("\\d+")) {
            // ID cliente
            sql = "SELECT pg.*, p.codigo_seguimiento, c.nombre || ' ' || c.apellido as cliente " +
                  "FROM pagos pg " +
                  "JOIN paquetes p ON pg.id_paquete = p.id_paquete " +
                  "JOIN usuarios c ON pg.id_cliente = c.id_usuario " +
                  "WHERE pg.id_cliente = ? ORDER BY pg.id_pago DESC";
            resultados = DatabaseOperations.executeQuery(sql, filtro);
        } else {
            // Estado
            sql = "SELECT pg.*, p.codigo_seguimiento, c.nombre || ' ' || c.apellido as cliente " +
                  "FROM pagos pg " +
                  "JOIN paquetes p ON pg.id_paquete = p.id_paquete " +
                  "JOIN usuarios c ON pg.id_cliente = c.id_usuario " +
                  "WHERE pg.estado_pago = ? ORDER BY pg.id_pago DESC";
            resultados = DatabaseOperations.executeQuery(sql, filtro);
        }
        
        String datos = DatabaseOperations.formatResultsAsText(resultados);
        return CommandResult.exito("Pagos listados correctamente", datos);
    }
    
    public static CommandResult insertarPago(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 4) {
            return CommandResult.error("Parámetros insuficientes. Se requieren: " +
                    "id_paquete, id_cliente, monto_total, tipo_pago [, fecha_vencimiento, metodo_pago, num_transaccion]");
        }
        
        String idPaquete = params.get(0);
        String idCliente = params.get(1);
        String montoTotal = params.get(2);
        String tipoPago = params.get(3);
        String fechaVenc = params.size() > 4 ? params.get(4) : null;
        String metodoPago = params.size() > 5 ? params.get(5) : null;
        String numTrans = params.size() > 6 ? params.get(6) : null;
        
        String sql = "INSERT INTO pagos (id_paquete, id_cliente, monto_total, tipo_pago, " +
                     "fecha_vencimiento, metodo_pago, numero_transaccion) " +
                     "VALUES (?, ?, ?, ?, ?::date, ?, ?)";
        
        int id = DatabaseOperations.executeInsertWithGeneratedKey(sql,
                idPaquete, idCliente, montoTotal, tipoPago, fechaVenc, metodoPago, numTrans);
        
        return CommandResult.exito("Pago registrado exitosamente con ID: " + id,
                String.format("Monto: Bs. %s\nTipo: %s\nEstado: PENDIENTE", montoTotal, tipoPago));
    }
    
    public static CommandResult registrarPago(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.size() < 3) {
            return CommandResult.error("Parámetros insuficientes. Use: REGPAG[\"id_pago\",\"monto\",\"metodo\"]");
        }
        
        String idPago = params.get(0);
        String monto = params.get(1);
        String metodo = params.get(2);
        
        String sql = "UPDATE pagos SET monto_pagado = monto_pagado + ?, " +
                     "estado_pago = CASE WHEN (monto_pagado + ?) >= monto_total THEN 'PAGADO' ELSE 'PARCIAL' END, " +
                     "fecha_pago = CURRENT_TIMESTAMP, metodo_pago = ? WHERE id_pago = ?";
        
        int filas = DatabaseOperations.executeUpdate(sql, monto, monto, metodo, idPago);
        
        if (filas > 0) {
            return CommandResult.exito("Pago registrado exitosamente",
                    String.format("ID Pago: %s\nMonto pagado: Bs. %s\nMétodo: %s", idPago, monto, metodo));
        } else {
            return CommandResult.error("No se encontró el pago con ID: " + idPago);
        }
    }
    
    // ============================================
    // CU8 - REPORTES Y ESTADÍSTICAS
    // ============================================
    
    public static CommandResult reporteDiario(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        String fecha = params.isEmpty() ? java.time.LocalDate.now().toString() : params.get(0);
        
        String sql = "SELECT " +
                     "COUNT(*) as total_paquetes, " +
                     "SUM(peso_kg) as peso_total, " +
                     "COUNT(CASE WHEN estado_paquete='ENTREGADO' THEN 1 END) as entregados, " +
                     "COUNT(CASE WHEN estado_paquete='EN_TRANSITO' THEN 1 END) as en_transito, " +
                     "COUNT(CASE WHEN estado_paquete='REGISTRADO' THEN 1 END) as registrados " +
                     "FROM paquetes WHERE DATE(fecha_registro) = ?::date";
        
        List<Map<String, Object>> resultados = DatabaseOperations.executeQuery(sql, fecha);
        String datos = DatabaseOperations.formatResultsAsText(resultados);
        
        return CommandResult.exito("Reporte diario generado", datos);
    }
    
    public static CommandResult reportePaquetes(CommandParser parser) throws SQLException {
        String sql = "SELECT estado_paquete, COUNT(*) as cantidad, " +
                     "SUM(valor_declarado) as valor_total, AVG(peso_kg) as peso_promedio " +
                     "FROM paquetes GROUP BY estado_paquete ORDER BY cantidad DESC";
        
        List<Map<String, Object>> resultados = DatabaseOperations.executeQuery(sql);
        String datos = DatabaseOperations.formatResultsAsText(resultados);
        
        return CommandResult.exito("Reporte de paquetes generado", datos);
    }
    
    public static CommandResult reporteCliente(CommandParser parser) throws SQLException {
        List<String> params = parser.getParametros();
        
        if (params.isEmpty()) {
            return CommandResult.error("Falta parámetro. Use: REPCLI[\"id_cliente\"]");
        }
        
        String idCliente = params.get(0);
        
        String sql = "SELECT u.nombre, u.apellido, COUNT(p.id_paquete) as total_paquetes, " +
                     "SUM(pg.monto_total) as total_facturado, SUM(pg.monto_pagado) as total_pagado " +
                     "FROM usuarios u " +
                     "LEFT JOIN paquetes p ON u.id_usuario = p.id_cliente " +
                     "LEFT JOIN pagos pg ON p.id_paquete = pg.id_paquete " +
                     "WHERE u.id_usuario = ? " +
                     "GROUP BY u.id_usuario, u.nombre, u.apellido";
        
        List<Map<String, Object>> resultados = DatabaseOperations.executeQuery(sql, idCliente);
        String datos = DatabaseOperations.formatResultsAsText(resultados);
        
        return CommandResult.exito("Reporte de cliente generado", datos);
    }
    
    public static CommandResult reporteVehiculos(CommandParser parser) throws SQLException {
        String sql = "SELECT v.placa, v.marca, v.modelo, u.nombre || ' ' || u.apellido as chofer, " +
                     "COUNT(p.id_paquete) as paquetes_transportados, v.estado " +
                     "FROM vehiculos v " +
                     "LEFT JOIN usuarios u ON v.id_chofer = u.id_usuario " +
                     "LEFT JOIN paquetes p ON v.id_vehiculo = p.id_vehiculo " +
                     "GROUP BY v.id_vehiculo, v.placa, v.marca, v.modelo, u.nombre, u.apellido, v.estado " +
                     "ORDER BY paquetes_transportados DESC";
        
        List<Map<String, Object>> resultados = DatabaseOperations.executeQuery(sql);
        String datos = DatabaseOperations.formatResultsAsText(resultados);
        
        return CommandResult.exito("Reporte de vehículos generado", datos);
    }
    
    public static CommandResult reporteRutas(CommandParser parser) throws SQLException {
        String sql = "SELECT r.nombre_ruta, o.ciudad as origen, d.ciudad as destino, " +
                     "COUNT(p.id_paquete) as total_paquetes, r.distancia_km, r.costo_base " +
                     "FROM rutas r " +
                     "JOIN destinos o ON r.id_origen = o.id_destino " +
                     "JOIN destinos d ON r.id_destino = d.id_destino " +
                     "LEFT JOIN paquetes p ON r.id_ruta = p.id_ruta " +
                     "GROUP BY r.id_ruta, r.nombre_ruta, o.ciudad, d.ciudad, r.distancia_km, r.costo_base " +
                     "ORDER BY total_paquetes DESC";
        
        List<Map<String, Object>> resultados = DatabaseOperations.executeQuery(sql);
        String datos = DatabaseOperations.formatResultsAsText(resultados);
        
        return CommandResult.exito("Reporte de rutas generado", datos);
    }
}
