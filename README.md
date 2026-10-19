# Cosecha Local

Aplicación Android que conecta a productores del campo con compradores locales. Incluye tres roles (comprador, vendedor y administrador) y funciona sin backend: todos los datos son de ejemplo y viven en memoria.

## Tecnologías

- Kotlin
- Layouts XML con ViewBinding (sin Compose)
- Material Design 3 con paleta propia
- Navigation Component (un solo `nav_graph.xml`)
- ViewModel y LiveData, con ViewModels compartidos por rol (`activityViewModels`)
- RecyclerView con `ListAdapter`
- Sin librerías externas de gráficas: las barras se dibujan con una `View` personalizada

## Requisitos

- Android Studio reciente
- JDK 11 o superior
- Dispositivo o emulador con Android 8.0 (API 26) o superior

## Cómo ejecutarla

Desde Android Studio, abre el proyecto y pulsa **Run**.

Por línea de comandos, con un emulador o dispositivo conectado:

```bash
./gradlew :app:installDebug
adb shell am start -n com.garcia.cosechalocal/.ui.splash.SplashActivity
```

## Cómo entrar

No hay autenticación real. En la pantalla de login se elige el rol de prueba (Comprador, Vendedor o Administrador) y la app muestra el menú inferior de ese rol. "Cerrar sesión" vuelve al login y oculta el menú.

## Funcionalidades por rol

**Comprador**
- Inicio y catálogo con búsqueda y filtro por categoría
- Detalle de producto, carrito con badge y checkout
- Perfil con cambio de biometría simulado

**Vendedor**
- Panel con ventas del mes, pedidos pendientes y productos activos
- CRUD de productos con validaciones y confirmación al eliminar
- Historial de pedidos con filtro por estado y cambio de estado desde un diálogo
- Perfil de la tienda

**Administrador**
- Panel con indicadores, gráfica de pedidos de 7 días y productos por aprobar
- Gestión de usuarios: buscar, filtrar por rol, crear, editar, bloquear y eliminar
- Moderación de productos: aprobar, suspender y eliminar
- Reporte de ventas por periodo (hoy, 7 días, mes) con comisión de la plataforma
- Ajustes simulados

## Estructura del proyecto

```
app/src/main/
├── java/com/garcia/cosechalocal/
│   ├── data/
│   │   ├── model/        Product, Order, User, ProductModeration, SalesReport...
│   │   └── repository/   Datos de ejemplo por rol
│   ├── ui/
│   │   ├── auth/         Login, registro y recuperación de contraseña
│   │   ├── buyer/        Pantallas del comprador
│   │   ├── seller/       Pantallas del vendedor
│   │   ├── admin/        Pantallas del administrador
│   │   ├── common/       UserRole y pantallas compartidas
│   │   ├── main/         MainActivity y menú inferior por rol
│   │   └── splash/       Pantalla de inicio
│   └── utils/            Formato de precios y montos
└── res/
    ├── layout/           Fragments e ítems de lista
    ├── navigation/       nav_graph.xml
    ├── menu/             Menús inferiores por rol
    └── values/           colors, dimens, strings, styles y themes
```

## Paleta

| Color | Hex |
|---|---|
| Verde principal | `#2E7D32` |
| Verde oscuro | `#1B4D2B` |
| Ámbar de acento | `#F9A825` |
| Fondo | `#F5F8F1` |
| Error | `#C62828` |

## Notas

- Los precios usan pesos colombianos con formato `$32.000`; los montos grandes se muestran como `$5,1 M`.
- Los cambios hechos en pantalla (crear, editar o eliminar) se pierden al cerrar la app, porque no hay persistencia.
