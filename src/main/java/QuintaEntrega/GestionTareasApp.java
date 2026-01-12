import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Aplicación de Gestión de Tareas con Interfaz Web integrada para pruebas.
 */
public class GestionTareasApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.requestLogger.http((ctx, ms) -> {
                System.out.println(ctx.method() + " " + ctx.path() + " - " + ctx.status() + " (" + ms + "ms)");
            });
        }).start(7070);

        // --- INTERFAZ WEB PARA PRUEBAS (Carga esto en http://localhost:7070) ---
        app.get("/", ctx -> {
            ctx.html("<!DOCTYPE html>" +
                    "<html><head><meta charset='UTF-8'><title>To-Do API</title>" +
                    "<style>body{font-family:sans-serif;max-width:800px;margin:auto;padding:20px;background:#f4f4f9}" +
                    "table{width:100%;border-collapse:collapse;background:white} th,td{padding:10px;border:1px solid #ddd;text-align:left}" +
                    "button{cursor:pointer;padding:5px 10px;background:#007bff;color:white;border:none;border-radius:3px}" +
                    "button.done{background:#28a745} .form-box{background:#eee;padding:15px;margin-bottom:20px;border-radius:5px}</style></head>" +
                    "<body>" +
                    "<h1>Gestor de Tareas</h1>" +
                    "<div class='form-box'>" +
                    "<h3>Agregar Nueva Tarea</h3>" +
                    "<input id='titulo' placeholder='Título'> <input id='desc' placeholder='Descripción'> " +
                    "<button onclick='agregar()'>Agregar Tarea</button>" +
                    "</div>" +
                    "<table><thead><tr><th>ID</th><th>Título</th><th>Estado</th><th>Acciones</th></tr></thead>" +
                    "<tbody id='tabla'></tbody></table>" +
                    "<script>" +
                    "async function cargar(){" +
                    "  const r = await fetch('/tareas'); const lista = await r.json();" +
                    "  document.getElementById('tabla').innerHTML = lista.map(t => `<tr>" +
                    "    <td>${t.id}</td><td><b>${t.titulo}</b><br><small>${t.descripcion}</small></td>" +
                    "    <td>${t.completada ? '✅ Completada' : '⏳ Pendiente'}</td>" +
                    "    <td>${t.completada ? '' : `<button class='done' onclick='completar(${t.id})'>Completar</button>`}</td>" +
                    "  </tr>`).join('');" +
                    "}" +
                    "async function agregar(){" +
                    "  const t = document.getElementById('titulo').value; const d = document.getElementById('desc').value;" +
                    "  if(!t) return alert('El título es obligatorio');" +
                    "  await fetch('/tareas', {method:'POST', body: JSON.stringify({titulo:t, descripcion:d}), headers:{'Content-Type':'application/json'}});" +
                    "  cargar();" +
                    "}" +
                    "async function completar(id){" +
                    "  await fetch('/tareas/'+id+'/completar', {method:'PATCH'});" +
                    "  cargar();" +
                    "}" +
                    "cargar();" +
                    "</script></body></html>");
        });

        // --- ENDPOINTS DE LA API ---
        app.get("/tareas", TareaController::obtenerTodas);
        app.get("/tareas/{id}", TareaController::obtenerPorId);
        app.post("/tareas", TareaController::crear);
        app.put("/tareas/{id}", TareaController::actualizar);
        app.delete("/tareas/{id}", TareaController::eliminar);
        app.patch("/tareas/{id}/completar", TareaController::marcarCompletada);

        // --- MANEJO DE ERRORES ---
        app.exception(NoSuchElementException.class, (e, ctx) -> {
            ctx.status(HttpStatus.NOT_FOUND).json(Map.of("error", e.getMessage()));
        });
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage()));
        });
    }

    // ==========================================
    // MODELO
    // ==========================================
    static class Tarea {
        private Long id;
        private String titulo;
        private String descripcion;
        private boolean completada;
        private String fechaCreacion;

        public Tarea() {}

        public Tarea(Long id, String titulo, String descripcion) {
            this.id = id;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.completada = false;
            this.fechaCreacion = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public boolean isCompletada() { return completada; }
        public void setCompletada(boolean completada) { this.completada = completada; }
        public String getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    }

    // ==========================================
    // SERVICIO
    // ==========================================
    static class TareaService {
        private static final Map<Long, Tarea> tareas = new ConcurrentHashMap<>();
        private static final AtomicLong idGenerator = new AtomicLong(1);

        static {
            save(new Tarea(null, "Estudiar Javalin", "Completar ejercicios prácticos"));
            save(new Tarea(null, "Prueba de API", "Verificar que el JSON funciona"));
        }

        public static List<Tarea> findAll() {
            return new ArrayList<>(tareas.values());
        }

        public static Tarea findById(Long id) {
            if (!tareas.containsKey(id)) throw new NoSuchElementException("Tarea no encontrada");
            return tareas.get(id);
        }

        public static Tarea save(Tarea nuevaTarea) {
            if (nuevaTarea.getTitulo() == null || nuevaTarea.getTitulo().isEmpty()) {
                throw new IllegalArgumentException("El título es obligatorio");
            }
            Long id = idGenerator.getAndIncrement();
            nuevaTarea.setId(id);
            nuevaTarea.setCompletada(false);
            nuevaTarea.setFechaCreacion(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            tareas.put(id, nuevaTarea);
            return nuevaTarea;
        }

        public static Tarea update(Long id, Tarea datos) {
            Tarea t = findById(id);
            t.setTitulo(datos.getTitulo());
            t.setDescripcion(datos.getDescripcion());
            return t;
        }

        public static void delete(Long id) {
            if (!tareas.containsKey(id)) throw new NoSuchElementException("ID inexistente");
            tareas.remove(id);
        }

        public static Tarea markAsCompleted(Long id) {
            Tarea t = findById(id);
            t.setCompletada(true);
            return t;
        }
    }

    // ==========================================
    // CONTROLADOR
    // ==========================================
    static class TareaController {
        public static void obtenerTodas(Context ctx) {
            ctx.json(TareaService.findAll());
        }
        public static void obtenerPorId(Context ctx) {
            Long id = Long.parseLong(ctx.pathParam("id"));
            ctx.json(TareaService.findById(id));
        }
        public static void crear(Context ctx) {
            Tarea t = ctx.bodyAsClass(Tarea.class);
            ctx.status(HttpStatus.CREATED).json(TareaService.save(t));
        }
        public static void actualizar(Context ctx) {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Tarea t = ctx.bodyAsClass(Tarea.class);
            ctx.json(TareaService.update(id, t));
        }
        public static void eliminar(Context ctx) {
            TareaService.delete(Long.parseLong(ctx.pathParam("id")));
            ctx.status(HttpStatus.NO_CONTENT);
        }
        public static void marcarCompletada(Context ctx) {
            Long id = Long.parseLong(ctx.pathParam("id"));
            ctx.json(TareaService.markAsCompleted(id));
        }
    }
}