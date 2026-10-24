# EcoConsumo
Aplicación móvil para la gestión de inventarios del hogar, alertas de caducidad y reducción del desperdicio de alimentos - UPN

## Requisitos
- Android Studio (incluye el JDK 17 y el SDK de Android)
- Dispositivo o emulador con Android 8.0 (API 26) o superior

La base de datos es **SQLite local** (`EcoConsumo.db`) gestionada con **Room**.
No requiere instalar ningún servidor de base de datos.

## Cómo ejecutar
1. Clonar el repositorio y abrirlo con *File > Open* en Android Studio.
2. Esperar la sincronización de Gradle.
3. Ejecutar la configuración `app` en un emulador o celular.
4. Pruebas unitarias: clic derecho en `app/src/test` > *Run Tests*, o `./gradlew test`.

## Arquitectura (MVVM)
```
View (Activity / Fragment)  ──observa──▶  ViewModel (LiveData)
                                              │
                                              ▼
                                         Repository  ──▶  Tareas (hilo secundario)
                                              │
                                              ▼
                                     Room DAO  ──▶  SQLite (EcoConsumo.db)
```
| Paquete | Contenido |
|---------|-----------|
| `data.entity` | Entidades Room: `Usuario`, `Hogar`, `MiembroHogar`, `Categoria`, `Producto`, `ItemCompra`, `Movimiento` |
| `data.dao` | Consultas SQL (Room valida cada consulta al compilar) |
| `data.model` | Resultados de consultas agrupadas y joins |
| `data.repository` | Lógica de negocio y transacciones |
| `ui.*` | Pantallas y ViewModels por módulo: `auth`, `despensa`, `compras`, `estadisticas`, `cuenta` |
| `notificaciones` | Canal de notificaciones y `VencimientoWorker` (WorkManager) |
| `util` | Sesión, preferencias, validación, seguridad, fechas y exportación CSV |

## Historias de usuario implementadas
| HU | Funcionalidad | Dónde |
|----|---------------|-------|
| HU0001 | Registro de productos | `ProductoFormActivity`, `ProductoFormViewModel` |
| HU0002 | Fechas de vencimiento con validación | `ProductoValidator` (con pruebas unitarias) |
| HU0003 | Notificaciones de productos por vencer, a la hora configurada | `VencimientoWorker`, `NotificacionHelper` |
| HU0004 | Consulta de productos y stock, filtro por categoría | `DespensaFragment`, `DespensaViewModel` |
| HU0005 | Edición y eliminación con confirmación | `DetalleProductoActivity` |
| HU0006 | Lista de compras por stock bajo, artículos manuales | `ComprasFragment`, `CompraRepository` |
| HU0007 | Registro de usuarios (contraseña con hash PBKDF2) | `RegistroActivity`, `UsuarioRepository` |
| HU0008 | Inicio de sesión | `LoginActivity`, `SessionManager` |
| HU0009 | Filtros Todos / Por caducar / En stock | `DespensaFragment` |
| HU0010 | Búsqueda en tiempo real | `DespensaFragment` |
| HU0011 | Categorías predefinidas y personalizadas | `CategoriaRepository` |
| HU0012 | Cierre de sesión | `CuentaFragment` |

## Funcionalidades adicionales
- **Historial y estadísticas:** cada compra, consumo y desecho (con motivo) se registra en `movimiento`.
  La pestaña Estadísticas muestra unidades compradas, consumidas y desechadas, % de aprovechamiento,
  un gráfico mensual (vista propia `BarChartView`), desperdicio por categoría y por motivo,
  los productos más desechados y una recomendación. El historial completo se puede filtrar por tipo.
- **Despensa compartida:** cada usuario pertenece a un hogar (rol administrador o miembro).
  Otro miembro de la familia se une con el código de invitación de 6 caracteres. El administrador
  puede renombrar el hogar, generar un código nuevo y quitar miembros.
- **Ajustes:** activar/desactivar avisos, días de anticipación para "por caducar", hora del aviso
  diario, tema claro/oscuro y exportación del inventario y el historial a CSV.
- **Perfil:** editar el nombre y cambiar la contraseña.

## Modelo de datos
- `usuario`: cuentas registradas
- `hogar`: despensas compartidas, con código de invitación único
- `miembro_hogar`: hogar y rol de cada usuario (un hogar por usuario)
- `categoria`: categorías predefinidas (`id_hogar` NULL) y personalizadas por hogar
- `producto`: víveres con cantidad, stock mínimo y fechas (`yyyy-MM-dd`)
- `lista_compra`: artículos por comprar (sugeridos por stock bajo o agregados a mano)
- `movimiento`: historial de compras, consumos y desechos; base de las estadísticas
