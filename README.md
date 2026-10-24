# EcoConsumo
Aplicación móvil para la gestión de inventarios y alertas de caducidad - UPN

## Requisitos
- Android Studio (incluye el JDK 17 y el SDK de Android)
- Dispositivo o emulador con Android 8.0 (API 26) o superior

La base de datos es **SQLite local** (`EcoConsumo.db`), creada automáticamente por `DBHelper`.
No requiere instalar ningún servidor de base de datos.

## Cómo ejecutar
1. Clonar el repositorio y abrirlo con *File > Open* en Android Studio.
2. Esperar la sincronización de Gradle.
3. Ejecutar la configuración `app` en un emulador o celular.

## Historias de usuario implementadas
| HU | Funcionalidad | Dónde |
|----|---------------|-------|
| HU0001 | Registro de productos | `ProductoFormActivity` |
| HU0002 | Fechas de vencimiento con validación | `ProductoFormActivity` |
| HU0003 | Notificaciones de productos por vencer (3 días) | `VencimientoWorker`, `NotificacionHelper` |
| HU0004 | Consulta de productos y stock, filtro por categoría | `DespensaFragment` |
| HU0005 | Edición y eliminación con confirmación | `DetalleProductoActivity` |
| HU0006 | Lista de compras por stock bajo o agotado | `ComprasFragment`, `CompraDAO` |
| HU0007 | Registro de usuarios (contraseña con hash PBKDF2) | `RegistroActivity` |
| HU0008 | Inicio de sesión | `LoginActivity`, `SessionManager` |
| HU0009 | Filtros Todos / Por caducar / En stock | `DespensaFragment` |
| HU0010 | Búsqueda en tiempo real | `DespensaFragment` |
| HU0011 | Categorías predefinidas y personalizadas | `CategoriaDAO`, `ProductoFormActivity` |
| HU0012 | Cierre de sesión | `CuentaFragment` |

## Estructura de la base de datos
- `usuario`: cuentas registradas
- `categoria`: categorías predefinidas (`id_usuario` NULL) y personalizadas
- `producto`: víveres con cantidad, stock mínimo y fechas (formato `yyyy-MM-dd`)
- `lista_compra`: sugerencias de compra generadas por stock bajo
