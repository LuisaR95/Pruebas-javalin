// Configuración
const API_BASE_URL = 'http://localhost:7070/api';

// Elementos del DOM
const elementos = {
    form: null,
    productoId: null,
    nombre: null,
    precio: null,
    btnGuardar: null,
    btnLimpiar: null,
    tablaProductos: null,
    mensajeDiv: null,
    errorDiv: null
};

// Estado de la aplicación
let modoEdicion = false;

/**
 * Inicializa la aplicación
 */
function inicializar() {
    // Cachear elementos del DOM
    elementos.form = document.getElementById('formProducto');
    elementos.productoId = document.getElementById('producto-id');
    elementos.nombre = document.getElementById('nombre');
    elementos.precio = document.getElementById('precio');
    elementos.btnGuardar = document.getElementById('btnGuardar');
    elementos.btnLimpiar = document.getElementById('btnLimpiar');
    elementos.tablaProductos = document.querySelector('#tablaProductos tbody');
    elementos.mensajeDiv = document.getElementById('mensaje');
    elementos.errorDiv = document.getElementById('error');

    // Configurar event listeners
    elementos.form.addEventListener('submit', manejarSubmit);
    elementos.btnLimpiar.addEventListener('click', limpiarFormulario);

    // Cargar productos iniciales
    cargarProductos();
}

/**
 * Carga todos los productos desde la API
 */
async function cargarProductos() {
    try {
        const response = await fetch(${API_BASE_URL}/productos);

        if (!response.ok) {
            throw new Error('Error al cargar productos');
        }

        const productos = await response.json();
        renderizarTabla(productos);
        ocultarError();
        ocultarMensaje();
    } catch (error) {
        mostrarError(Error al cargar: ${error.message});
        console.error('Error:', error);
    }
}

/**
 * Renderiza la tabla de productos
 * @param {Array} productos - Lista de productos
 */
function renderizarTabla(productos) {
    if (!productos || productos.length === 0) {
        elementos.tablaProductos.innerHTML =
            '<tr><td colspan="4"><div class="empty-state">No hay productos disponibles</div></td></tr>';
        return;
    }

    elementos.tablaProductos.innerHTML = productos.map(producto => `
        <tr>
            <td>${escaparHTML(producto.id)}</td>
            <td>${escaparHTML(producto.nombre)}</td>
            <td>${parseFloat(producto.precio).toFixed(2)} EUR</td>
            <td class="actions">
                <div class="btn-group">
                    <button class="btn btn-edit" onclick="editarProducto(${producto.id})">Editar</button>
                    <button class="btn btn-delete" onclick="borrarProducto(${producto.id})">Borrar</button>
                </div>
            </td>
        </tr>
    `).join('');
}

/**
 * Maneja el envío del formulario
 * @param {Event} e - Evento de submit
 */
async function manejarSubmit(e) {
    e.preventDefault();
    ocultarError();
    ocultarMensaje();

    const nombre = elementos.nombre.value.trim();
    const precio = parseFloat(elementos.precio.value);
    const id = elementos.productoId.value;

    // Validaciones
    if (!nombre) {
        mostrarError('El nombre es obligatorio');
        return;
    }

    if (isNaN(precio) || precio < 0) {
        mostrarError('El precio debe ser un número mayor o igual a 0');
        return;
    }

    // Preparar datos
    const producto = { nombre, precio };
    const metodo = id ? 'PUT' : 'POST';
    const url = id ? ${API_BASE_URL}/productos/${id} : ${API_BASE_URL}/productos;

    // Deshabilitar botones durante la operación
    deshabilitarBotones(true);

    try {
        const response = await fetch(url, {
            method: metodo,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(producto)
        });

        if (!response.ok) {
            throw new Error('Error en la operación');
        }

        mostrarMensaje(id ? 'Producto actualizado correctamente' : 'Producto creado correctamente');
        limpiarFormulario();
        await cargarProductos();
    } catch (error) {
        mostrarError(Error: ${error.message});
        console.error('Error:', error);
    } finally {
        deshabilitarBotones(false);
    }
}

/**
 * Carga un producto para editarlo
 * @param {number} id - ID del producto
 */
async function editarProducto(id) {
    ocultarError();

    try {
        const response = await fetch(${API_BASE_URL}/productos/${id});

        if (!response.ok) {
            throw new Error('Producto no encontrado');
        }

        const producto = await response.json();

        elementos.productoId.value = producto.id;
        elementos.nombre.value = producto.nombre;
        elementos.precio.value = producto.precio;
        elementos.btnGuardar.textContent = 'Actualizar';

        modoEdicion = true;

        // Scroll al formulario
        elementos.form.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) {
        mostrarError(Error: ${error.message});
        console.error('Error:', error);
    }
}

/**
 * Borra un producto
 * @param {number} id - ID del producto
 */
async function borrarProducto(id) {
    if (!confirm('¿Está seguro de que desea eliminar este producto?')) {
        return;
    }

    try {
        const response = await fetch(${API_BASE_URL}/productos/${id}, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Error al eliminar el producto');
        }

        mostrarMensaje('Producto eliminado correctamente');
        await cargarProductos();
    } catch (error) {
        mostrarError(Error: ${error.message});
        console.error('Error:', error);
    }
}

/**
 * Limpia el formulario y resetea el estado
 */
function limpiarFormulario() {
    elementos.form.reset();
    elementos.productoId.value = '';
    elementos.btnGuardar.textContent = 'Guardar';
    modoEdicion = false;
    ocultarError();
}

/**
 * Muestra un mensaje de error
 * @param {string} mensaje - Mensaje a mostrar
 */
function mostrarError(mensaje) {
    if (mensaje) {
        elementos.errorDiv.textContent = mensaje;
        elementos.errorDiv.classList.add('show');
    }
}

/**
 * Oculta el mensaje de error
 */
function ocultarError() {
    elementos.errorDiv.classList.remove('show');
}

/**
 * Muestra un mensaje de éxito
 * @param {string} mensaje - Mensaje a mostrar
 */
function mostrarMensaje(mensaje) {
    if (mensaje) {
        elementos.mensajeDiv.textContent = mensaje;
        elementos.mensajeDiv.classList.add('show');
        setTimeout(() => elementos.mensajeDiv.classList.remove('show'), 3000);
    }
}

/**
 * Oculta el mensaje de éxito
 */
function ocultarMensaje() {
    elementos.mensajeDiv.classList.remove('show');
}

/**
 * Escapa caracteres HTML para prevenir XSS
 * @param {string} texto - Texto a escapar
 * @returns {string} - Texto escapado
 */
function escaparHTML(texto) {
    const div = document.createElement('div');
    div.textContent = texto;
    return div.innerHTML;
}

/**
 * Habilita o deshabilita los botones del formulario
 * @param {boolean} deshabilitar - true para deshabilitar, false para habilitar
 */
function deshabilitarBotones(deshabilitar) {
    elementos.btnGuardar.disabled = deshabilitar;
    elementos.btnLimpiar.disabled = deshabilitar;
    elementos.nombre.disabled = deshabilitar;
    elementos.precio.disabled = deshabilitar;
}

// Exponer funciones globales para onclick en HTML
window.editarProducto = editarProducto;
window.borrarProducto = borrarProducto;

// Inicializar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', inicializar);
