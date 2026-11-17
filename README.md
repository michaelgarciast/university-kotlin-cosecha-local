# Cosecha Local

Aplicación Android que conecta a productores del campo con compradores locales.
Tres roles (comprador, vendedor y administrador) con autenticación real, datos
persistentes en la nube y compras verificables en sandbox.

## Tecnologías

- Kotlin
- Layouts XML con ViewBinding (sin Compose)
- Material Design 3 con paleta propia
- Navigation Component (un solo `nav_graph.xml`)
- ViewModel y LiveData, con ViewModels compartidos por rol (`activityViewModels`)
- RecyclerView con `ListAdapter`
- Firebase Authentication (correo/contraseña)
- Cloud Firestore (perfiles, productos, carritos y órdenes)
- Room (fotos de producto como `BLOB` local, tabla `product_images`)
- Google Play Services Location (`FusedLocationProvider`) y `Geocoder` del sistema
- AndroidX Biometric + Android Keystore (ingreso con huella)
- Sin librerías externas de gráficas: las barras se dibujan con una `View` personalizada

## Requisitos

- Android Studio reciente
- JDK 11 o superior
- Dispositivo o emulador con Android 8.0 (API 26) o superior
- Un proyecto propio de Firebase (capa gratuita, sin facturación)

## Configuración de Firebase

El archivo `google-services.json` no está en el repositorio. Cada persona debe
crear su propio proyecto Firebase:

1. En [Firebase Console](https://console.firebase.google.com), crea un proyecto
   nuevo (no requiere plan de facturación).
2. Registra una app Android con el paquete `com.garcia.cosechalocal` y descarga
   el `google-services.json`. Colócalo en `app/google-services.json`.
3. En **Authentication → Método de acceso**, habilita **Correo/contraseña**.
4. En **Firestore Database**, crea la base `(default)` y en la pestaña **Reglas**
   pega el contenido de [`firestore.rules`](firestore.rules) de este repositorio
   y pulsa **Publicar**. Sin estas reglas la app no puede leer ni escribir datos.
5. Crea la primera cuenta administradora (ver la siguiente sección).

## Cuentas de prueba

- **Comprador y vendedor:** se registran desde la pantalla de registro de la app
  (el rol se elige en el formulario).
- **Administrador:** no se puede registrar desde la app. Se provisiona así:
  1. Registra una cuenta cualquiera (o créala en Authentication de la consola).
  2. En Firestore, edita el documento `users/{uid}` de esa cuenta y cambia
     `role` a `ADMIN`.
  3. Vuelve a iniciar sesión: el menú de administrador aparece.

Sugerencia de cuentas: un comprador, dos vendedores (para comprobar el
aislamiento de productos y pedidos) y un administrador.

## Cómo ejecutarla

Desde Android Studio, abre el proyecto y pulsa **Run**.

Por línea de comandos, con un emulador o dispositivo conectado:

```bash
./gradlew :app:installDebug
adb shell am start -n com.garcia.cosechalocal/.ui.splash.SplashActivity
```

## Funcionalidades por rol

**Comprador**
- Inicio y catálogo con búsqueda y filtro por categoría (solo productos aprobados)
- Detalle de producto, carrito persistente en Firestore con badge
- Checkout con dirección, ubicación GPS opcional (botón "Usar ubicación") y
  geocodificación de la dirección escrita
- Pago sandbox: aprobar, rechazar o dejar pendiente; reintento desde Pedidos
- Historial de pedidos con estados de pago y entrega en tiempo real
- Perfil con interruptor "Ingresar con huella" (biometría fuerte + Keystore)

**Vendedor**
- Panel con ventas del mes, pedidos pendientes y productos activos
- CRUD de productos con foto (galería o cámara), guardada localmente en Room;
  los productos nuevos quedan `POR_APROBAR` hasta que el administrador los apruebe
- Pedidos que incluyen sus productos, con avance del estado de entrega
- Perfil de la tienda e interruptor de huella

**Administrador**
- Panel con indicadores, gráfica de pedidos de 7 días y productos por aprobar
- Gestión de usuarios: buscar, filtrar por rol, crear (con correo de
  restablecimiento para que el usuario defina su contraseña), editar,
  activar/desactivar y eliminar; no puede desactivarse ni cambiarse el rol a sí mismo
- Moderación de productos: aprobar, suspender y eliminar
- Reporte de ventas por periodo (hoy, 7 días, mes) con comisión de la plataforma

## Pasarela de pagos (sandbox)

Proveedor `SANDBOX_SIMULADO`: un diálogo permite simular tarjeta aprobada,
rechazada o pago pendiente. No se ingresan datos de tarjeta ni hay credenciales.
El pago se aplica sobre la orden guardada en Firestore en una transacción
idempotente: al aprobar se descuenta el stock atómicamente (las reglas exigen
que la orden sea del comprador y que la resta sea exactamente la cantidad
pedida) y se guarda la referencia `SBX-XXXXXXXX`. Una orden `APROBADO` es final;
una `RECHAZADO` se puede reintentar o cancelar.

**Limitación:** al no haber backend, el cliente escribe el resultado del pago.
Para producción hay que mover la aprobación y el stock a un entorno de confianza
(Cloud Functions + webhook de un proveedor real como ePayco o Mercado Pago).

## Arquitectura

```
app/src/main/
├── java/com/garcia/cosechalocal/
│   ├── data/
│   │   ├── local/        Room: CosechaLocalDatabase, ProductImageDao/Entity
│   │   ├── model/        Product, Order, CartItem, UserProfile, SalesReport...
│   │   └── repository/   AuthRepository, UserRepository, ProductRepository,
│   │                     CartRepository, OrderRepository, AdminRepository,
│   │                     ProductImageRepository (Room)
│   ├── ui/
│   │   ├── auth/         Login, registro y recuperación (Firebase Auth)
│   │   ├── buyer/        Catálogo, carrito, checkout, pedidos, perfil,
│   │   │               SandboxPayment
│   │   ├── seller/       Panel, productos, pedidos, perfil de tienda
│   │   ├── admin/        Panel, usuarios, moderación, reporte de ventas
│   │   ├── common/       UserRole y utilidades de UI
│   │   ├── main/         MainActivity y menú inferior por rol
│   │   └── splash/       SplashActivity: restaura sesión o bloqueo biométrico
│   └── utils/            BiometricLock (Keystore + BiometricPrompt),
│                         ErrorMessages, FormatUtils, ProductImageStore
└── res/
    ├── layout/           Fragments e ítems de lista
    ├── navigation/       nav_graph.xml
    ├── menu/             Menús inferiores por rol
    └── values/           colors, dimens, strings, styles y themes
```

## Modelo de datos

Colecciones de Firestore, detalladas en [FIREBASE_DATA_MODEL.md](FIREBASE_DATA_MODEL.md):

- `users/{uid}` — nombre, email, `role` (`BUYER`/`SELLER`/`ADMIN`), `active`.
- `products/{id}` — `sellerId`, nombre, precio, stock, categoría, `hasLocalImage`,
  `status` (`POR_APROBAR`/`APROBADO`/`SUSPENDIDO`), `lastOrderId`.
- `carts/{uid}` — ítems, `shippingFee` y `checkoutId` (idempotencia del checkout).
- `orders/{id}` — `buyerId`, ítems con precio al momento de compra, `sellerIds`,
  dirección, `location` opcional, totales, `paymentStatus`, `deliveryStatus`,
  `paymentProvider`, `paymentReference`.
- `product_images` (SQLite/Room) — `productId`, `imageData` (BLOB), `mimeType`.

Las reglas de seguridad (`firestore.rules`) impiden que un usuario cambie su
propio rol o estado, que un vendedor toque productos ajenos y que un comprador
lea pedidos o carritos de otros.

## Seguridad y privacidad

- `google-services.json`, `local.properties`, keystores y secretos están
  excluidos del repositorio (ver `.gitignore`); nunca se han subido credenciales.
- No hay secretos en el código: el proveedor de pagos es simulado.
- Copia de seguridad y transferencia entre dispositivos desactivadas
  (`allowBackup="false"`), para que sesión, caché de Firestore y fotos no se
  exporten.
- El ingreso con huella usa una clave AES en Android Keystore invalidada si
  cambian las huellas inscritas; cerrar sesión o bloquear la cuenta borra la
  ruta biométrica.

## Limitaciones conocidas

- Las fotos de producto son locales del dispositivo (Room); no se comparten
  entre dispositivos ni usuarios. Es una decisión académica para evitar la
  facturación de Firebase Storage.
- El pago es simulado en el cliente (ver sección de pagos).
- Crear la orden no reserva stock: varias órdenes pendientes pueden pedir la
  misma unidad; el descuento real ocurre al aprobar el pago y falla si ya no hay.
- Un vendedor puede leer la orden completa (incluidos ítems de otros
  vendedores); la interfaz solo muestra sus renglones. El aislamiento total
  requiere un backend.
- Eliminar un usuario borra su perfil (bloquea el acceso) pero deja la
  credencial de Authentication huérfana: borrarla requiere Admin SDK.

## Paleta

| Color | Hex |
|---|---|
| Verde principal | `#2E7D32` |
| Verde oscuro | `#1B4D2B` |
| Ámbar de acento | `#F9A825` |
| Fondo | `#F5F8F1` |
| Error | `#C62828` |

## Notas

- Los precios usan pesos colombianos con formato `$32.000`; los montos grandes
  se muestran como `$5,1 M`.
- Costo de envío fijo de `$7.000` por pedido (validado por las reglas).
- Máximo 5 productos diferentes por pedido (validado por las reglas).
