# ORDERS APPLICATION

## Introducción

La aplicación Orders permite gestionar ordenes de venta y sus respectivos productos a través de una API.
La misma fue desarrollada usando Java 8 y Spring Boot, y MongoDB como método de persistencia.


## Supuestos y Consideraciones del  Problema

### Consideraciones de la letra

 - El SKU de los productos es ***case sensitve***, SKU-1 es diferente a sku-1.
 - Cuando se crea una orden esta se crea sin productos.
 - Una orden tiene una lista de productos con sus respectiva cantidades. Se puede agregar el mismo producto varias veces.
 - Las ordenes se crean con una moneda por defecto pero esta puede ser modificada para evitar problemas a futuro.
 - Los productos pueden estar en cualquier moneda y el calculo del total de la orden se realiza utilizando conversiones.
 - El agregar un producto no recalcula toda la orden, sino que simplemente agrega el valor del mismo (convertido si fuera necesario).
 - El actualizar o borrar un producto recalcula toda la orden.
 - Se asume que el modificar un producto en una orden es modificar la cantidad únicamente, ya que el producto se puede modificar individualmente y esto se ve reflejado en todas las ordenes.

### Consideraciones técnicas

 - Se decide utilizar un cache para la llamadas a la API de fixer, para poder calcular rápidamente. La duración del cache es configurable, se decide utilizar un día entero.
 - La API de fixer en su versión gratuita solo permite el calculo en base a EUR, por lo cual todos los cálculos están basados en esto (Se pasa el valor a EUR, y luego a la moneda seleccionada).
 - Para hacer mas ágil la actualización y borrado de un producto y no sacrificar la disponibilidad. Se decidió utilizar un evento asincrónico, que se encarga de actualizar las ordenes correspondientes.

## Instalación

Clonar el repositorio utilizando el siguiente comando:

    git clone https://github.com/janteloo/orders-app

Acceder a la raíz 

    cd orders-app

La aplicación se debe construir dependiendo de la forma que se pretenda utilizar. Se puede instalar usando Docker (recomendada) y correr dentro de un contenedor o simplemente ejecutar el jar generado.


### Docker
Primero se debe construir la aplicación y generar la imagen de Docker. Para este caso es necesario tener instalado Docker.

    mvn package dockerfile:build
Esto genera la imagen de la aplicacion.

Crear una red para interconectar la imagen de orders con la de mongodb

    docker network create orders_network

La primera vez que se levante mongo es necesario especificar la red a la cual se va a conectar (esto se encarga de descargar la imagen de mongo sino existe)

    docker run -d --name mongocontainer --network=orders_network -v ~/mongo-data:/data/db mongo

Por ultimo levantar la aplicación por primera vez especificando el puerto y la red

    docker run -p 8080:8080 --name orderscontainer --network=orders_network orders-app

Una vez creados para poder volver a levantarlos se utiliza

    docker start -ai mongocontainer
    docker start -ai orderscontainer

### Jar

En este caso es necesario tener instalado Mongodb.

Generar el Jar

    mvn clean package

Levantar mongodb

    mongod
Correr la aplicación

    mvn spring-boot:run

Para ambos casos se recomienda utilizar la flag para no correr los tests, ya que los test de integración toman un tiempo considerable en correr

    mvn -Dmaven.test.skip=true clean package
    mvn -Dmaven.test.skip=true package dockerfile:build.

## API
Endpoint base

    http://localhost:8080/api/order/

### Productos
#### Crear Producto
    POST
    http://localhost:8080/api/product/
    Body:
    {   
		"sku": "PU-7500",
		"name": "Test1",
		"price": 3456,
		"currency": "USD"
	}

#### Obtener Producto
    GET
    http://localhost:8080/api/product/{sku}

#### Actualizar Producto
    PUT
    http://localhost:8080/api/product/{sku}
    Body
    {
	    "name": "Test number 2",
	    "price": 800,
	    "currency": "UYU"
    }
Actualiza el producto, y las ordenes que lo contengan
#### Borrar Producto
    DELETE
    http://localhost:8080/api/product/{sku}
Borra el producto, y lo borra de las ordenes que lo contengan

### Ordenes

#### Crear Orden

    POST
    http://localhost:8080/api/order/
Crea una orden vacía

#### Agregar Producto a Orden
	POST
    http://localhost:8080/api/order/{orderId}/product/{sku}

#### Borrar Producto de una Orden

    DELETE
    http://localhost:8080/api/order/{orderId}/product/{sku}
Borra el producto de la orden, y actualiza el total

#### Obtener Orden
    GET
    http://localhost:8080/api/order/{orderId}
#### Actualizar Cantidad de Productos en una Orden

    UPDATE
    http://localhost:8080/api/order/{orderId}/product/{sku}
    Body
    {
		"count": 5
	}

Actualiza la cantidad de productos que se encuentran en una orden, y actualiza el total

#### Obtener Monedas
    GET
    http://localhost:8080/api/order/currencies

Devuelve todas las monedas aceptadas por el sistema

#### Obtener Orden en otra Moneda

    GET
    http://localhost:8080/api/order/{orderId}/product/{sku}

Devuelve una orden con el valor correspondiente en otra moneda. **No actualiza la orden sino que devuelve una representación**

#### Recalcular Orden en otra Moneda

    PUT
    http://localhost:8080/api/order/{orderId}/product/{sku}
    
Devuelve una orden con el valor correspondiente en otra moneda. Recalcula el total y actualiza la moneda de la misma.
