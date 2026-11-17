# Modelo de datos de Cosecha Local

Firestore guarda usuarios, productos, carritos y órdenes. SQLite/Room guarda las
imágenes de producto en la tabla `product_images`.

## users/{uid}

El ID del documento es el UID de Firebase Authentication.

- `name`: string
- `email`: string
- `role`: `BUYER`, `SELLER` o `ADMIN`
- `active`: boolean
- `createdAt`: timestamp
- `updatedAt`: timestamp

El registro público solo crea `BUYER` o `SELLER`. La primera cuenta `ADMIN` se
crea manualmente en Firebase Console usando el UID de una cuenta ya creada en
Authentication.

### Gestión de usuarios por el administrador

- **Crear:** la app provisiona la cuenta de Authentication con una instancia
  secundaria de `FirebaseAuth` y una contraseña temporal aleatoria, y luego
  escribe el perfil. El usuario recibe un correo de restablecimiento para
  definir su propia contraseña.
- **Editar:** nombre, rol y `active`. El correo no se puede cambiar desde el
  cliente porque la credencial de Authentication seguiría usando el anterior.
- **Desactivar:** `active = false`. Acción reversible; bloquea el inicio de
  sesión y todas las operaciones por las reglas de seguridad.
- **Eliminar:** borra el documento `users/{uid}`. Sin perfil, el inicio de
  sesión falla y las reglas niegan toda operación. La credencial de
  Authentication queda huérfana (el correo no puede reutilizarse) hasta que
  un backend con Admin SDK la elimine. El historial de órdenes se preserva
  porque las órdenes guardan copias de nombres y montos.

## products/{productId}

- `sellerId`: UID del vendedor
- `sellerName`: string
- `name`: string
- `price`: number
- `stock`: number
- `lastOrderId`: string opcional; última orden aprobada que descontó stock
  (lo usan las reglas para validar el descuento del comprador)
- `category`: string
- `description`: string
- `hasLocalImage`: boolean
- `status`: `POR_APROBAR`, `APROBADO` o `SUSPENDIDO`
- `createdAt`: timestamp
- `updatedAt`: timestamp

Los vendedores crean productos como `POR_APROBAR`. Solo el administrador cambia
el estado. `product_images.productId` en SQLite debe coincidir con `productId`.

## carts/{buyerUid}

El ID del documento es el UID del comprador.

- `items`: lista de mapas
  - `productId`: string
  - `sellerId`: string
  - `sellerName`: string (solo para mostrar el carrito)
  - `name`: string
  - `unitPrice`: number
  - `quantity`: number
- `shippingFee`: number
- `checkoutId`: string (UUID estable hasta registrar la orden; ID de orden para reintentos)
- `updatedAt`: timestamp

Cada comprador solo puede leer y modificar su propio documento de carrito.
La transacción de checkout valida los productos y sus precios actuales, crea
`orders/{checkoutId}` y vacía el carrito con un nuevo `checkoutId` en el mismo
commit. El límite actual es de cinco productos diferentes por pedido.

## orders/{orderId}

- `buyerId`: UID del comprador
- `buyerName`: string
- `items`: lista de mapas
  - `productId`: string
  - `sellerId`: string
  - `name`: string
  - `unitPrice`: number
  - `quantity`: number
  - `subtotal`: number
- `sellerIds`: lista de UIDs para consultas del vendedor
- `deliveryAddress`: string
- `location`: mapa opcional
  - `latitude`: number
  - `longitude`: number
- `subtotal`: number
- `shippingFee`: number
- `total`: number
- `paymentStatus`: `PENDIENTE_PAGO`, `APROBADO`, `RECHAZADO` o `CANCELADO`
- `deliveryStatus`: `PENDIENTE`, `PREPARANDO`, `ENVIADO` o `ENTREGADO`
- `paymentProvider`: string opcional (`SANDBOX_SIMULADO` en la fase 7, sin backend;
  el cliente escribe el resultado, no es verificación real)
- `paymentReference`: string opcional
- `createdAt`: timestamp
- `updatedAt`: timestamp

Una orden puede contener productos de varios vendedores. `sellerIds` permite que
cada vendedor consulte solo las órdenes que incluyen sus productos. El vendedor
solo ve sus renglones en la interfaz. Mientras no exista verificación de pago,
el vendedor no puede cambiar `deliveryStatus`.

**Pendiente para producción:** la transacción del cliente comprueba disponibilidad
pero no reserva/decrementa stock. Varias órdenes pendientes pueden solicitar la
misma unidad; un backend de confianza debe reservar stock de manera atómica,
expirar reservas y restituirlas al cancelar antes de habilitar pagos. Además, las
reglas de lectura dan acceso a un vendedor a la orden completa (incluidos los
ítems de otros vendedores); se necesitan vistas por vendedor redactadas por el
backend para aislar esos datos. Publicar las reglas locales actualizadas en
Firebase Console antes de probar checkout.

## product_images (SQLite)

- `productId`: string, clave primaria
- `imageData`: BLOB comprimido, preferiblemente JPEG
- `mimeType`: string
- `updatedAt`: número de milisegundos

La foto se muestra únicamente en el dispositivo donde fue cargada. Esta es una
limitación académica para evitar la facturación requerida por Firebase Storage.
