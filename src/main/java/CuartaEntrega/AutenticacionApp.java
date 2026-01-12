package CuartaEntrega;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sistema de Autenticación Básica con Javalin.
 * Implementa Registro, Login y Protección de Rutas.
 */
public class AutenticacionApp {

    // Almacenamiento de usuarios (Username -> Usuario)
    private static final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();
    
    // Almacenamiento de tokens activos (Token -> Username)
    private static final Map<String, String> tokensValidos = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.requestLogger.http((ctx, ms) -> {
                System.out.println(ctx.method() + " " + ctx.path() + " - " + ctx.status());
            });
        }).start(7070);

        // --- INTERFAZ DE PRUEBAS ---
        app.get("/", AutenticacionApp::servirInterfazPruebas);

        // --- RUTAS PÚBLICAS ---
        app.post("/auth/registrar", AuthController::registrar);
        app.post("/auth/login", AuthController::login);

        // --- RUTAS PROTEGIDAS ---
        // El handler 'before' verifica el token antes de acceder a /perfil
        app.before("/perfil", AuthController::verificarAutenticacion);
        app.get("/perfil", AuthController::obtenerPerfil);

        // Manejo de excepciones
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        });
    }

    // ==========================================
    // MODELO
    // ==========================================
    static class Usuario {
        private String username;
        private String password; // En producción, nunca usar texto plano
        private String email;
        private String fechaRegistro;

        public Usuario() {}
        public Usuario(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.fechaRegistro = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getEmail() { return email; }
        public String getFechaRegistro() { return fechaRegistro; }
    }

    // ==========================================
    // CONTROLADOR DE AUTENTICACIÓN
    // ==========================================
    static class AuthController {

        /**
         * Registra un nuevo usuario
         */
        public static void registrar(Context ctx) {
            Usuario nuevo = ctx.bodyAsClass(Usuario.class);
            
            if (nuevo.getUsername() == null || nuevo.getPassword() == null) {
                throw new IllegalArgumentException("Usuario y contraseña son requeridos");
            }
            if (usuarios.containsKey(nuevo.getUsername())) {
                throw new IllegalArgumentException("El nombre de usuario ya existe");
            }

            usuarios.put(nuevo.getUsername(), new Usuario(nuevo.getUsername(), nuevo.getPassword(), nuevo.getEmail()));
            ctx.status(HttpStatus.CREATED).json(Map.of(
                "mensaje", "Usuario registrado exitosamente",
                "username", nuevo.getUsername()
            ));
        }

        /**
         * Realiza login y genera token simple (username_timestamp)
         */
        public static void login(Context ctx) {
            Map<String, String> credenciales = ctx.bodyAsClass(Map.class);
            String user = credenciales.get("username");
            String pass = credenciales.get("password");

            Usuario usuario = usuarios.get(user);
            if (usuario != null && usuario.getPassword().equals(pass)) {
                // Generar token simple
                String token = user + "_" + System.currentTimeMillis();
                tokensValidos.put(token, user);
                
                ctx.json(Map.of(
                    "token", token,
                    "username", user
                ));
            } else {
                ctx.status(HttpStatus.UNAUTHORIZED).json(Map.of("error", "Credenciales inválidas"));
            }
        }

        /**
         * Handler que verifica autenticación antes de ejecutar el endpoint
         */
        public static void verificarAutenticacion(Context ctx) {
            String token = ctx.header("Authorization");
            
            String username = validarToken(token);
            if (username == null) {
                // Si el token no es válido, lanzamos 401 y Javalin detiene la ejecución
                ctx.status(401).json(Map.of("error", "No autorizado. Token requerido o inválido"));
                throw new UnauthorizedResponse(); 
            }
            
            // Guardamos el usuario en el contexto para usarlo en el endpoint
            ctx.attribute("currentUser", username);
        }

        /**
         * Valida token de autenticación
         */
        public static String validarToken(String token) {
            if (token == null) return null;
            return tokensValidos.get(token); // Retorna el username si el token existe
        }

        /**
         * Obtiene perfil del usuario autenticado
         */
        public static void obtenerPerfil(Context ctx) {
            String username = ctx.attribute("currentUser");
            Usuario u = usuarios.get(username);
            
            ctx.json(Map.of(
                "username", u.getUsername(),
                "email", u.getEmail() != null ? u.getEmail() : "No provisto",
                "fechaRegistro", u.getFechaRegistro()
            ));
        }
    }

    private static void servirInterfazPruebas(Context ctx) {
        ctx.html("<html><head><meta charset='UTF-8'><title>Auth Test</title>" +
            "<style>body{font-family:sans-serif;max-width:500px;margin:50px auto;background:#f0f2f5;padding:20px}" +
            ".card{background:white;padding:20px;border-radius:8px;box-shadow:0 2px 4px rgba(0,0,0,0.1);margin-bottom:20px}" +
            "input{display:block;width:100%;margin:10px 0;padding:8px;box-sizing:border-box}" +
            "button{width:100%;padding:10px;background:#1877f2;color:white;border:none;border-radius:5px;cursor:pointer}" +
            "pre{background:#eee;padding:10px;overflow-x:auto;font-size:12px}</style></head>" +
            "<body>" +
            "<h2>🔐 Sistema de Autenticación</h2>" +
            "<div class='card'><h3>Registro / Login</h3>" +
            "<input id='u' placeholder='Usuario'> <input id='p' type='password' placeholder='Contraseña'>" +
            "<button onclick='auth(\"/auth/registrar\")'>Registrar</button><br><br>" +
            "<button style='background:#42b72a' onclick='auth(\"/auth/login\")'>Login</button></div>" +
            "<div class='card'><h3>Perfil Protegido</h3>" +
            "<button style='background:#666' onclick='verPerfil()'>Obtener mi Perfil</button>" +
            "<p id='tokenStatus' style='font-size:11px;color:gray'>No has iniciado sesión</p></div>" +
            "<pre id='res'>Resultado de la API aparecerá aquí...</pre>" +
            "<script>" +
            "let miToken = '';" +
            "async function auth(path){" +
            "  const body = {username: document.getElementById('u').value, password: document.getElementById('p').value};" +
            "  const r = await fetch(path, {method:'POST', body: JSON.stringify(body)});" +
            "  const data = await r.json();" +
            "  if(data.token) { miToken = data.token; document.getElementById('tokenStatus').innerText = 'Token: ' + miToken; }" +
            "  document.getElementById('res').innerText = JSON.stringify(data, null, 2);" +
            "}" +
            "async function verPerfil(){" +
            "  const r = await fetch('/perfil', {headers: {'Authorization': miToken}});" +
            "  const data = await r.json();" +
            "  document.getElementById('res').innerText = JSON.stringify(data, null, 2);" +
            "}" +
            "</script></body></html>");
    }
}