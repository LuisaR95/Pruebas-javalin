package CuartaEntrega;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * API de Ventas y Estadísticas en Tiempo Real usando Javalin.
 * Procesa datos en memoria y realiza agregaciones dinámicas.
 */
public class VentaApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.requestLogger.http((ctx, ms) -> {
                System.out.println(ctx.method() + " " + ctx.path() + " - " + ctx.status());
            });
        }).start(7070);

        // --- Datos iniciales ---
        seedData();

        // --- Interfaz Web ---
        app.get("/", VentaApp::servirInterfaz);

        // --- Endpoints de Ventas ---
        app.get("/ventas", VentaController::obtenerTodas);
        app.get("/ventas/{id}", VentaController::obtenerUna);
        app.post("/ventas", VentaController::crear);

        // --- Endpoint de Estadísticas ---
        app.get("/estadisticas", VentaController::obtenerEstadisticas);

        // --- Manejo de Errores ---
        app.exception(NoSuchElementException.class, (e, ctx) -> {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        });
        
        System.out.println("Servidor de Estadísticas activo en http://localhost:7070");
    }

    private static void seedData() {
        VentaService.registrar(new Venta("Laptop", 1, 1200.00));
        VentaService.registrar(new Venta("Mouse", 5, 25.50));
        VentaService.registrar(new Venta("Teclado", 2, 45.00));
    }

    // ==========================================
    // MODELOS
    // ==========================================
    static class Venta {
        public Long id;
        public String producto;
        public int cantidad;
        public double precioUnitario;
        public double total;
        public String fecha; // YYYY-MM-DD para filtrado fácil

        public Venta() {}
        public Venta(String producto, int cantidad, double precioUnitario) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.total = cantidad * precioUnitario;
            this.fecha = LocalDate.now().toString();
        }
    }

    static class Estadisticas {
        public double totalVentas;
        public long numeroTransacciones;
        public String productoMasVendido;
        public double ventaPromedio;

        public Estadisticas(double totalVentas, long numeroTransacciones, String productoMasVendido, double ventaPromedio) {
            this.totalVentas = totalVentas;
            this.numeroTransacciones = numeroTransacciones;
            this.productoMasVendido = productoMasVendido;
            this.ventaPromedio = ventaPromedio;
        }
    }

    // ==========================================
    // SERVICIO (Lógica de Agregación)
    // ==========================================
    static class VentaService {
        private static final Map<Long, Venta> ventas = new ConcurrentHashMap<>();
        private static final AtomicLong idGenerator = new AtomicLong(1);

        public static Venta registrar(Venta v) {
            v.id = idGenerator.getAndIncrement();
            v.total = v.cantidad * v.precioUnitario;
            if (v.fecha == null) v.fecha = LocalDate.now().toString();
            ventas.put(v.id, v);
            return v;
        }

        public static List<Venta> obtenerTodas() {
            return new ArrayList<>(ventas.values());
        }

        public static Estadisticas calcularEstadisticas(String inicio, String fin) {
            List<Venta> filtradas = ventas.values().stream()
                .filter(v -> (inicio == null || v.fecha.compareTo(inicio) >= 0))
                .filter(v -> (fin == null || v.fecha.compareTo(fin) <= 0))
                .collect(Collectors.toList());

            if (filtradas.isEmpty()) {
                return new Estadisticas(0.0, 0, "N/A", 0.0);
            }

            double total = filtradas.stream().mapToDouble(v -> v.total).sum();
            long count = filtradas.size();
            double promedio = total / count;

            // Calcular producto más vendido (por cantidad acumulada)
            String topProduct = filtradas.stream()
                .collect(Collectors.groupingBy(v -> v.producto, Collectors.summingInt(v -> v.cantidad)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

            return new Estadisticas(total, count, topProduct, promedio);
        }
    }

    // ==========================================
    // CONTROLADORES
    // ==========================================
    static class VentaController {
        public static void obtenerTodas(Context ctx) {
            String p = ctx.queryParam("producto");
            if (p != null) {
                ctx.json(VentaService.obtenerTodas().stream()
                    .filter(v -> v.producto.equalsIgnoreCase(p))
                    .collect(Collectors.toList()));
            } else {
                ctx.json(VentaService.obtenerTodas());
            }
        }

        public static void obtenerUna(Context ctx) {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Venta v = VentaService.ventas.get(id);
            if (v == null) throw new NoSuchElementException("Venta no encontrada");
            ctx.json(v);
        }

        public static void crear(Context ctx) {
            Venta nueva = ctx.bodyAsClass(Venta.class);
            ctx.status(201).json(VentaService.registrar(nueva));
        }

        public static void obtenerEstadisticas(Context ctx) {
            String inicio = ctx.queryParam("fecha_inicio");
            String fin = ctx.queryParam("fecha_fin");
            ctx.json(VentaService.calcularEstadisticas(inicio, fin));
        }
    }

    private static void servirInterfaz(Context ctx) {
        ctx.html("<html><head><meta charset='UTF-8'><title>Ventas & Stats</title>" +
            "<style>body{font-family:sans-serif;max-width:900px;margin:20px auto;background:#f4f7f6;padding:20px}" +
            ".grid{display:grid;grid-template-columns: repeat(4, 1fr);gap:15px;margin-bottom:30px}" +
            ".stat-card{background:white;padding:20px;border-radius:10px;box-shadow:0 2px 5px rgba(0,0,0,0.05);text-align:center}" +
            ".stat-card h3{margin:0;color:#666;font-size:0.9em} .stat-card p{margin:10px 0 0;font-size:1.5em;font-weight:bold;color:#2c3e50}" +
            ".form-container{background:#fff;padding:20px;border-radius:10px;margin-bottom:20px} " +
            "table{width:100%;background:white;border-collapse:collapse} th,td{padding:12px;text-align:left;border-bottom:1px solid #eee}" +
            "input{padding:8px;margin-right:10px} button{padding:8px 20px;background:#3498db;color:white;border:none;border-radius:5px;cursor:pointer}</style></head>" +
            "<body>" +
            "<h1>📊 Dashboard de Estadísticas de Ventas</h1>" +
            "<div class='grid'>" +
            "  <div class='stat-card'><h3>Total Ventas</h3><p id='total'>$0.00</p></div>" +
            "  <div class='stat-card'><h3>Transacciones</h3><p id='count'>0</p></div>" +
            "  <div class='stat-card'><h3>Más Vendido</h3><p id='top'>-</p></div>" +
            "  <div class='stat-card'><h3>Promedio</h3><p id='avg'>$0.00</p></div>" +
            "</div>" +
            "<div class='form-container'>" +
            "  <h3>Registrar Nueva Venta</h3>" +
            "  <input id='prod' placeholder='Producto'> <input id='cant' type='number' placeholder='Cant' style='width:60px'> " +
            "  <input id='prec' type='number' step='0.01' placeholder='Precio Unit.'> " +
            "  <button onclick='vender()'>Registrar</button>" +
            "</div>" +
            "<table><thead><tr><th>ID</th><th>Producto</th><th>Cant.</th><th>Total</th><th>Fecha</th></tr></thead>" +
            "<tbody id='tabla'></tbody></table>" +
            "<script>" +
            "async function actualizar(){" +
            "  const rS = await fetch('/estadisticas'); const s = await rS.json();" +
            "  document.getElementById('total').innerText = '$' + s.totalVentas.toFixed(2);" +
            "  document.getElementById('count').innerText = s.numeroTransacciones;" +
            "  document.getElementById('top').innerText = s.productoMasVendido;" +
            "  document.getElementById('avg').innerText = '$' + s.ventaPromedio.toFixed(2);" +
            "  const rV = await fetch('/ventas'); const ventas = await rV.json();" +
            "  document.getElementById('tabla').innerHTML = ventas.map(v => `<tr>" +
            "    <td>${v.id}</td><td>${v.producto}</td><td>${v.cantidad}</td><td>$${v.total.toFixed(2)}</td><td>${v.fecha}</td>" +
            "  </tr>`).reverse().join('');" +
            "}" +
            "async function vender(){" +
            "  const body = {producto:document.getElementById('prod').value, cantidad:parseInt(document.getElementById('cant').value), precioUnitario:parseFloat(document.getElementById('prec').value)};" +
            "  await fetch('/ventas', {method:'POST', body:JSON.stringify(body), headers:{'Content-Type':'application/json'}});" +
            "  actualizar();" +
            "}" +
            "actualizar();" +
            "</script></body></html>");
    }
}