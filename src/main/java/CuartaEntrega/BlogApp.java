package CuartaEntrega;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Aplicación de Blog con Posts y Comentarios usando Javalin.
 * Demuestra relaciones entre entidades y estructuras JSON complejas.
 */
public class BlogApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.requestLogger.http((ctx, ms) -> {
                System.out.println(ctx.method() + " " + ctx.path() + " - " + ctx.status());
            });
        }).start(7070);

        // --- Datos de prueba ---
        inicializarDatos();

        // --- Ruta de Bienvenida / Interfaz ---
        app.get("/", BlogApp::servirInterfaz);

        // --- Endpoints de Posts ---
        app.get("/posts", PostController::obtenerTodos);
        app.get("/posts/{id}", PostController::obtenerUno);
        app.post("/posts", PostController::crear);
        app.put("/posts/{id}", PostController::actualizar);
        app.delete("/posts/{id}", PostController::eliminar);

        // --- Endpoints de Comentarios ---
        app.get("/posts/{id}/comentarios", CommentController::obtenerComentarios);
        app.post("/posts/{id}/comentarios", CommentController::añadirComentario);

        // --- Manejo de Errores ---
        app.exception(NoSuchElementException.class, (e, ctx) -> {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        });
    }

    private static void inicializarDatos() {
        PostService.crear(new Post("Bienvenido al Blog", "Este es el primer post de prueba.", "Admin"));
    }

    // ==========================================
    // MODELOS
    // ==========================================
    
    static class Comentario {
        public Long id;
        public String autor;
        public String contenido;
        public String fecha;

        public Comentario() {}
        public Comentario(Long id, String autor, String contenido) {
            this.id = id;
            this.autor = autor;
            this.contenido = contenido;
            this.fecha = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    static class Post {
        public Long id;
        public String titulo;
        public String contenido;
        public String autor;
        public String fechaPublicacion;
        public List<Comentario> comentarios = new ArrayList<>();

        public Post() {}
        public Post(String titulo, String contenido, String autor) {
            this.titulo = titulo;
            this.contenido = contenido;
            this.autor = autor;
            this.fechaPublicacion = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    // ==========================================
    // SERVICIOS (Lógica de Almacenamiento)
    // ==========================================

    static class PostService {
        private static final Map<Long, Post> posts = new ConcurrentHashMap<>();
        private static final AtomicLong postIds = new AtomicLong(1);
        private static final AtomicLong commentIds = new AtomicLong(1);

        public static List<Post> obtenerTodos() { return new ArrayList<>(posts.values()); }

        public static Post obtenerPorId(Long id) {
            if (!posts.containsKey(id)) throw new NoSuchElementException("Post no encontrado");
            return posts.get(id);
        }

        public static Post crear(Post p) {
            p.id = postIds.getAndIncrement();
            p.fechaPublicacion = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            p.comentarios = new ArrayList<>();
            posts.put(p.id, p);
            return p;
        }

        public static Post actualizar(Long id, Post nuevosDatos) {
            Post p = obtenerPorId(id);
            p.titulo = nuevosDatos.titulo;
            p.contenido = nuevosDatos.contenido;
            return p;
        }

        public static void eliminar(Long id) {
            if (posts.remove(id) == null) throw new NoSuchElementException("Post no encontrado");
        }

        public static Comentario agregarComentario(Long postId, Comentario c) {
            Post p = obtenerPorId(postId);
            c.id = commentIds.getAndIncrement();
            c.fecha = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            p.comentarios.add(c);
            return c;
        }
    }

    // ==========================================
    // CONTROLADORES
    // ==========================================

    static class PostController {
        public static void obtenerTodos(Context ctx) { ctx.json(PostService.obtenerTodos()); }
        public static void obtenerUno(Context ctx) {
            ctx.json(PostService.obtenerPorId(ctx.pathParamAsClass("id", Long.class).get()));
        }
        public static void crear(Context ctx) {
            ctx.status(201).json(PostService.crear(ctx.bodyAsClass(Post.class)));
        }
        public static void actualizar(Context ctx) {
            Long id = ctx.pathParamAsClass("id", Long.class).get();
            ctx.json(PostService.actualizar(id, ctx.bodyAsClass(Post.class)));
        }
        public static void eliminar(Context ctx) {
            PostService.eliminar(ctx.pathParamAsClass("id", Long.class).get());
            ctx.status(204);
        }
    }

    static class CommentController {
        public static void obtenerComentarios(Context ctx) {
            Long id = ctx.pathParamAsClass("id", Long.class).get();
            ctx.json(PostService.obtenerPorId(id).comentarios);
        }
        public static void añadirComentario(Context ctx) {
            Long id = ctx.pathParamAsClass("id", Long.class).get();
            Comentario c = ctx.bodyAsClass(Comentario.class);
            ctx.status(201).json(PostService.agregarComentario(id, c));
        }
    }

    private static void servirInterfaz(Context ctx) {
        ctx.html("<html><head><meta charset='UTF-8'><title>Blog API</title>" +
            "<style>body{font-family:sans-serif;max-width:700px;margin:30px auto;padding:20px;background:#f9f9f9}" +
            ".post{background:white;padding:20px;border-radius:8px;margin-bottom:20px;box-shadow:0 2px 5px rgba(0,0,0,0.1)}" +
            ".comment{margin-left:30px;padding:10px;border-left:3px solid #ddd;font-size:0.9em;background:#fafafa;margin-top:10px}" +
            "input, textarea{width:100%;margin-bottom:10px;padding:8px} button{padding:8px 15px;cursor:pointer}</style></head>" +
            "<body><h1>📝 Mi Blog API</h1>" +
            "<div style='background:#eee;padding:15px;margin-bottom:20px'><h3>Nuevo Post</h3>" +
            "<input id='t' placeholder='Título'> <textarea id='c' placeholder='Contenido'></textarea> <input id='a' placeholder='Autor'>" +
            "<button onclick='crearPost()'>Publicar</button></div>" +
            "<div id='feed'></div>" +
            "<script>" +
            "async function cargar(){" +
            "  const r = await fetch('/posts'); const posts = await r.json();" +
            "  document.getElementById('feed').innerHTML = posts.map(p => `<div class='post'>" +
            "    <h2>${p.titulo}</h2><p>${p.contenido}</p><small>Por ${p.autor} - ${p.fechaPublicacion}</small>" +
            "    <hr><h4>Comentarios</h4>" +
            "    <div id='coms-${p.id}'>${p.comentarios.map(c => `<div class='comment'><b>${c.autor}:</b> ${c.contenido}</div>`).join('')}</div>" +
            "    <div style='margin-top:10px'><input id='ca-${p.id}' placeholder='Tu nombre' style='width:30%'> " +
            "    <input id='cc-${p.id}' placeholder='Escribe un comentario...' style='width:50%'> " +
            "    <button onclick='comentar(${p.id})'>Enviar</button></div>" +
            "  </div>`).join('');" +
            "}" +
            "async function crearPost(){" +
            "  const body = {titulo:document.getElementById('t').value, contenido:document.getElementById('c').value, autor:document.getElementById('a').value};" +
            "  await fetch('/posts', {method:'POST', body:JSON.stringify(body), headers:{'Content-Type':'application/json'}});" +
            "  cargar();" +
            "}" +
            "async function comentar(id){" +
            "  const body = {autor:document.getElementById('ca-'+id).value, contenido:document.getElementById('cc-'+id).value};" +
            "  await fetch('/posts/'+id+'/comentarios', {method:'POST', body:JSON.stringify(body), headers:{'Content-Type':'application/json'}});" +
            "  cargar();" +
            "}" +
            "cargar();" +
            "</script></body></html>");
    }
}