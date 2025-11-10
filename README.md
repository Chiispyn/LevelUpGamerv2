# Proyecto Final: LevelUp Gamer

## Introducción

**LevelUp Gamer** es una aplicación de Android nativa que simula una tienda de e-commerce para una tienda de videojuegos y artículos gamer. Construida 100% con Jetpack Compose, la app sigue patrones de arquitectura modernos para ofrecer una experiencia de usuario fluida, un código mantenible y una base escalable, incluyendo un completo panel de administración.

---

## Proceso de Desarrollo: De la Idea a la Realidad

El desarrollo de esta app fue un viaje de refactorización y mejora continua, demostrando una evolución desde una prueba de concepto simple a una aplicación robusta.

1.  **Migración a una Base de Datos Real:** El proyecto comenzó con datos "hardcodeados" (escritos directamente en el código). El primer gran paso fue eliminar esta estructura y diseñar una arquitectura de persistencia sólida con **Room**, definiendo todas las entidades, DAOs y relaciones necesarias.

2.  **Limpieza y Reorganización:** A mitad de camino, el proyecto sufrió de desorden, con archivos duplicados y errores de compilación. Realizamos una auditoría completa, eliminamos el código obsoleto y reorganizamos la estructura de archivos para que fuera limpia y mantenible.

3.  **Implementación de Funcionalidades Clave:** Sobre la base de datos sólida, se implementaron las funcionalidades más importantes, como el sistema de recompensas dinámicas (con stock y tipos), la suspensión de usuarios y los filtros en la tienda.

4.  **Mejora de la Experiencia de Usuario (UX):** Finalmente, añadimos pequeños pero importantes detalles, como deshabilitar botones si una acción no es posible, mostrar mensajes de `Toast` para dar feedback, y asegurar que la navegación sea fluida y sin errores.

---

## Funcionalidades Implementadas

La aplicación se divide en dos grandes áreas: la tienda para clientes y un panel de administración.

### Funcionalidades para Clientes

*   **Catálogo de Productos:** Muestra los productos en una grilla con un buscador para filtrar por nombre y filtros dinámicos por categoría.
*   **Detalle de Producto:** Muestra información detallada, incluyendo descripción, precio y reviews de otros usuarios.
*   **Sistema de Reviews:** Los usuarios pueden ver y escribir sus propias reseñas y calificaciones.
*   **Carrito de Compras:** Funcionalidad completa para añadir, quitar y modificar la cantidad de productos.
*   **Proceso de Checkout:** Un flujo de compra que permite seleccionar dirección de envío, método de pago y aplicar recompensas.
*   **Sistema de Usuarios:** Registro y Login de usuarios.
*   **Perfil de Usuario:** Pantalla donde el usuario puede ver sus datos, puntos acumulados, nivel de lealtad y un historial de sus compras.
*   **Sistema de Puntos y Recompensas:** Los usuarios ganan puntos por sus compras y pueden canjearlos en una "Tienda de Recompensas" por premios dinámicos (descuentos, envío gratis, etc.).
*   **Comunidad y Eventos:** Una sección para ver los próximos eventos de la tienda, donde los usuarios pueden inscribirse para ganar puntos.

### Funcionalidades para Administradores

*   **Panel de Administración:** Un área separada y protegida para la gestión de la tienda.
*   **Gestión de Productos (CRUD):** Crear, ver, editar y eliminar productos.
*   **Gestión de Eventos (CRUD):** Crear, ver, editar y eliminar eventos comunitarios.
*   **Gestión de Recompensas (CRUD):** Creación de recompensas dinámicas, definiendo tipo, valor, costo en puntos y stock.
*   **Gestión de Pedidos:** Ver el listado de todas las órdenes y cambiar su estado (ej: de `PROCESANDO` a `ENVIADO`).
*   **Gestión de Usuarios:** Ver el listado de usuarios y la capacidad de suspender o reactivar sus cuentas.

---

## Funcionalidades No Implementadas (Alcance Futuro)

Para mantener el enfoque en la arquitectura local y la experiencia de usuario, las siguientes características no fueron implementadas:

*   **Conectividad con Backend:** La aplicación opera 100% local. No hay conexión con una API REST real.
*   **Pasarela de Pagos:** El proceso de "Checkout" es una simulación y no integra una pasarela de pagos real.
*   **Notificaciones Push:** No se implementó un sistema de notificaciones.
*   **Pruebas Unitarias y de Integración:** Aunque la arquitectura facilita las pruebas, no se desarrollaron casos de prueba exhaustivos.

---

## Arquitectura y Tecnologías Utilizadas

*   **UI:** Construida enteramente con **Jetpack Compose**.
*   **Arquitectura:** **MVVM (Model-View-ViewModel)**. Los ViewModels exponen el estado vía `StateFlow`, y los Composables reaccionan con `collectAsState()`.
*   **Navegación:** **Jetpack Navigation para Compose**. Se implementó una estructura de navegación anidada para separar los flujos de la app (Login, App Principal, Panel de Admin).
*   **Persistencia de Datos (Room):** Se usa como única fuente de verdad, siguiendo el patrón **Entity -> DAO -> Repository**.
    *   **Relaciones:** Se utiliza `@Relation` y `@Embedded` (ej. en `OrderWithItems`) para consultar datos relacionados de forma eficiente.
    *   **Tipos Complejos:** Se implementó un `TypeConverter` para que Room pueda guardar tipos complejos como Enums (`RewardType`).
*   **Inyección de Dependencias (Manual):** Una `ViewModelFactory` personalizada inyecta los repositorios necesarios, asegurando que todos los ViewModels trabajen con la misma instancia de `AppDatabase` creada en la clase `MyApp`.
*   **Asincronía:** **Corrutinas de Kotlin y Flows** para todas las operaciones de base de datos y tareas en segundo plano.

---

## Cómo Ejecutar el Proyecto

1.  Clonar el repositorio desde GitHub.
2.  Abrir el proyecto con una versión reciente de Android Studio.
3.  Esperar a que Gradle sincronice todas las dependencias.
4.  Ejecutar la aplicación en un emulador o en un dispositivo físico con Android.
