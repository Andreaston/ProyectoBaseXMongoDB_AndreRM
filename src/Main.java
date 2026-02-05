

import com.mongodb.client.*;
import org.basex.examples.api.BaseXClient;
import org.bson.BsonNull;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException {

        //Abrir BaseX
        //BaseXClient session = new BaseXClient("localhost", 1984, "admin", "123abc")
        BaseXClient sesionBase = new BaseXClient("localhost",1984,"admin","123abc");

        sesionBase.execute("OPEN productos");

        //Abrir MongoDB

        String url = "mongodb://localhost:27017";
        MongoClient mongoClient = MongoClients.create(url);
        MongoDatabase databaseMon = mongoClient.getDatabase("tienda");


        Scanner leer = new Scanner(System.in);
        int opcion = 0;

        //Guardar id cliente seleccionado
        ObjectId clienteId = null;
        //String clienteId = null;

        do {
            System.out.println("1. Modificar valor XML por ID\n2. Eliminar producto\n3. Obtener todos los productos por orden alfabético\n4. Listar productos por disponibilidad\n5. Mostrar producto más caro por categoría" +
                    "\n6. Mostrar nombre y fabricante de productos con subcanedas a buscar\n7. Mostrar cantidad total de productos en cada categoría y calcular el porcentaje que representa del stock" +
                    "\n8. Crear un nuevo cliente \n9. Identificarse con cliente \n10. Borrar cliente \n11. Modificar valor de cliente \n12. Introducir producto al carrito \n13. Mostrar carrito del cliente" +
                    "\n14. Mostrar pedidos cliente \n15. Realizar pedido del carrito \n16. Sumar todos los carritos de cada cliente \n17. Suma de los pedidos de cada cliente \n18. SALIR");

                opcion = leer.nextInt();

                switch (opcion){
                    case 1:

                        //replace node /productos/producto[id = "2"]/precio with <precio>20.01</precio>

                        leer.nextLine();
                        System.out.println("Dime el ID del producto a modificar");
                        String id = leer.nextLine();
                        System.out.println("Dime el nuevo precio del producto con ID["+id+"]");
                        String precio = leer.nextLine();

                        String query = "declare variable $id external;" +
                                "declare variable $precio external;" +
                                "replace node /productos/producto[id = $id]/precio with <precio>{$precio}</precio>";

                        var sentencia = sesionBase.query(query);

                        sentencia.bind("id", id);
                        sentencia.bind("precio",precio);

                        sentencia.execute();
                        sentencia.close();

                        break;
                    case 2:

                        //delete node /productos/producto[id = 9]

                        leer.nextLine();
                        System.out.println("Dime el id del producto a eliminar");
                        String idDelete = leer.nextLine();

                        String eliminar = "declare variable $id external; delete node /productos/producto[id = $id]";

                        var queryDelete = sesionBase.query(eliminar);

                        queryDelete.bind("id", idDelete);

                        queryDelete.execute();
                        queryDelete.close();


                        break;
                    case 3:

                        //for $p in //producto order by $p/nombre return $p/(id | nombre | precio | disponibilidad | categoria )

                        BaseXClient.Query productosOrden = sesionBase.query("for $p in //producto order by $p/nombre return $p/(id | nombre | precio | disponibilidad | categoria )");

                        while (productosOrden.more()){
                            System.out.println(productosOrden.next());
                        }

                        break;
                    case 4:

                        // //producto[disponibilidad > 11]/(id | nombre | precio | disponibilidad | categoria ) En java no funciona bien

                        leer.nextLine();
                        System.out.println("Dime una cantidad de stock a buscar");
                        String n = leer.nextLine();
                        /*
                        String buscar = "declare variable $cantidad external;" +
                                "//producto[disponibilidad > $cantidad]/(id | nombre | precio | disponibilidad | categoria )";


                        var queryBuscar = sesionBase.query(buscar);

                        queryBuscar.bind("cantidad", n);

                        while (queryBuscar.more()){
                            System.out.println(queryBuscar.next());
                        }

                         */

                        //for $p in //productos let $disponibilidad := $p/disponibilidad where $p/disponibilidad > $disponibilidad return $p/(id|nombre|precio|disponibilidad|categoria)

                       /* String buscar = "declare variable $cantidad external" +
                                "for $p in //productos let $disponibilidad := $p/disponibilidad where $cantidad > $disponibilidad return $p/(id | nombre | precio | disponibilidad | categoria)";

                        */

                        String buscar = "declare variable $cantidad external;" +
                                "for $p in //producto let $disponibilidad := xs:integer(normalize-space($p/disponibilidad)) where xs:integer($cantidad) > $disponibilidad return $p/(id | nombre | precio | disponibilidad | categoria)";

                        var queryBuscar = sesionBase.query(buscar);

                        queryBuscar.bind("cantidad", n);

                        while (queryBuscar.more()){
                            System.out.println(queryBuscar.next());
                        }

                        queryBuscar.close();

                        break;
                    case 5:

                        //for $cat in distinct-values(//producto/categoria) let $productosCat := //producto[categoria = $cat] let $maxPrecio := max($productosCat/precio) return $productosCat[precio = $maxPrecio]/(nombre | precio | categoria)

                        BaseXClient.Query queryCaro = sesionBase.query("for $cat in distinct-values(//producto/categoria) let $productosCat := //producto[categoria = $cat] let $maxPrecio := max($productosCat/precio) return $productosCat[precio = $maxPrecio]/(nombre | precio | categoria)");

                        while (queryCaro.more()){
                            System.out.println(queryCaro.next());
                        }

                        break;
                    case 6:

                        // /productos/producto[contains(descripcion,"Impresora")]/(nombre | fabricante)

                        leer.nextLine();
                        System.out.println("Dime una subcadena a buscar");
                        String subcadena = leer.nextLine();

                        String querySub = "declare variable $sub external; /productos/producto[contains(descripcion, $sub)]/(nombre | fabricante)";

                        var queryBusSub = sesionBase.query(querySub);
                        queryBusSub.bind("sub",subcadena);

                        if (queryBusSub == null){
                            System.out.println("No se encontró la palabra ["+subcadena+"]");
                        }else {

                            while (queryBusSub.more()){
                                System.out.println(queryBusSub.next());
                            }

                        }


                        break;
                    case 7:

                        //for $p in distinct-values(//producto/categoria) let $lista := //producto[categoria = $p] let $suma := sum($lista/disponibilidad) let $sumaT := sum(//producto/disponibilidad)let $porce := ($suma div $sumaT) * 100 return <categoria> <nombre>{$p}</nombre> <stock>{$suma}</stock> <media>{$porce}</media> </categoria>

                        BaseXClient.Query media = sesionBase.query("for $p in distinct-values(//producto/categoria) let $lista := //producto[categoria = $p] let $suma := sum($lista/disponibilidad) let $sumaT := sum(//producto/disponibilidad)let $porce := ($suma div $sumaT) * 100 return <categoria> <nombre>{$p}</nombre> <stock>{$suma}</stock> <media>{$porce}</media></categoria>");

                        while (media.more()){
                            System.out.println(media.next());
                        }


                        break;
                    case 8:
                        leer.nextLine();
                        System.out.println("Dime el nombre del nuevo cliente");
                        String nombre = leer.nextLine();
                        System.out.println("Dime el correo de " + nombre);
                        String email = leer.nextLine();
                        System.out.println("Dime la direccion de " + nombre);
                        String direccion = leer.nextLine();

                        MongoCollection<Document> cliente = databaseMon.getCollection("pedidos");
                        //new Document("email", "carlos.martinez@example.com")
                        Document clienteMail = cliente.find(new Document("email",email)).first();

                        if (clienteMail != null){
                            System.out.println("Ese mail ya está registrado con otro usuario");
                        }else {
                            Document crearCliente = new Document("nombre", nombre).append("email", email).append("direccion",direccion);
                            cliente.insertOne(crearCliente);
                            System.out.println("Cliente creado correctamente");
                        }

                        break;
                    case 9:

                        leer.nextLine();
                        System.out.println("Dime el email de cliente registrado para entrar en la sesión");
                        String emailCliente = leer.nextLine();

                        MongoCollection<Document> pedidos = databaseMon.getCollection("pedidos");
                        Document clienteExist = pedidos.find(new Document("email",emailCliente)).first();

                        if (clienteExist == null){
                            System.out.println("El cliente no existe");
                        } else {
                            //List<Bson> pillarId = Arrays.asList(new Document("$match",new Document("email", new Document("$eq", clienteExist))), new Document("$project", new Document("_id", 1L)));
                            //clienteId = (String) pillarId;
                            clienteId = clienteExist.getObjectId("_id");
                            System.out.println("Registro completado");
                        }


                        break;
                    case 10:

                        //new Document("email", "carlos.martinez@example.com")
                        leer.nextLine();
                        System.out.println("Dime el email del cliente a eliminar");
                        String clientEliminar = leer.nextLine();

                        MongoCollection<Document> collection = databaseMon.getCollection("pedidos");
                        Document ce = collection.find(new Document("email", clientEliminar)).first();

                        if (ce.isEmpty()){
                            System.out.println("El cliente no existe");
                        } else {
                            collection.deleteOne(ce);
                        }

                        break;
                    case 11:

                        leer.nextLine();
                        System.out.println("Dime el nombre para actualizar");
                        String nombreAct =leer.nextLine();
                        System.out.println("Dime el email para actualizar");
                        String emailAct = leer.nextLine();
                        System.out.println("Dime la dirección para actualizar");
                        String direcAct = leer.nextLine();
                        //Hay que meter filtro para que avise si no estás dentro de la cuenta

                        if (clienteId == null){
                            System.out.println("No hay usuario registrado");
                        } else {
                            MongoCollection<Document> colecionActualizar = databaseMon.getCollection("pedidos");
                            Document cambios = new Document().append("nombre", nombreAct).append("email",emailAct).append("direccion",direcAct);
                            colecionActualizar.updateOne(new Document("_id",clienteId), new Document("$set",cambios));
                            System.out.println("Actualización del cliente satisfactoria");
                        }

                        break;
                    case 12:
                        // string-join(//producto[id = 3]/(nombre/text() | disponibilidad/text()| precio/text()), ',')
                        // //producto[nombre = "Laptop HP Pavilion"]/disponibilidad/text()
                        String confirmacion = null;
                        //Comproebo que el cliente esté registrado
                        if (clienteId == null){
                            System.out.println("Primero entra como usuario");
                        } else {

                            do {
                                //Pido info del producto
                                System.out.println("Introduce el ID del producto");
                                String idProducto = leer.nextLine();
                                leer.nextLine(); //Si no lo pongo la primera vez que entra en el bucle salta la siguiente sentencia, si lo pongo al repetir el bucle pide introducir algo
                                System.out.println("Introduce la cantidad");
                                int cantidad = leer.nextInt();

                                //Traer el producto con una query, despúes lo separo en diferentes partes para guardar en varaibles
                                String queryCarrito = "declare variable $id external; string-join(//producto[id = $id]/(nombre/text() | disponibilidad/text()| precio/text()), ',') ";
                                //String queryCarrito = "declare variable $id external; string-join((//producto[id = $id]/nombre/text(),//producto[id = $id]/precio/text(),//producto[id = $id]/disponibilidad/text()), ',')";
                                var productoCarrito = sesionBase.query(queryCarrito);
                                productoCarrito.bind("id",idProducto);
                                productoCarrito.execute();

                                String producto = "";

                                while (productoCarrito.more()){
                                    producto = productoCarrito.next();
                                    System.out.println(producto);
                                }

                                System.out.println("PRODUCTO RAW -> [" + producto + "]");
                                System.out.println("LONGITUD -> " + producto.length());

                                String[] partes = producto.split(",");
                                System.out.println("PARTES -> " + partes.length);

                                String nomProd = partes[0];
                                double precioPod = Double.parseDouble(partes[1]);
                                //String precioPod = partes[1];
                                int stock = Integer.parseInt(partes[2]);
                                /*
                                //Comprobar que hay suficiente cantidad

                                String cantidadProd = "declare variable $id external; //producto[id = id]/disponibilidad/text()";

                                var cantidadProducto = sesionBase.query(cantidadProd);
                                int c = Integer.parseInt(String.valueOf(cantidadProducto));

                                 */

                                if (cantidad > stock){
                                    System.out.println("No hay sufuente stock");
                                    break;
                                }else {
                                    //Ahora actualizo el carrito del cliente
                                    MongoCollection<Document> pedido = databaseMon.getCollection("pedidos");
                                    Document pedidoNuevo = new Document().append("producto_id",idProducto).append("nombre", nomProd).append("cantidad", stock).append("precio_unitario", precioPod);
                                    pedido.updateOne(new Document("_id",clienteId), new Document("$push", new Document("carrito",pedidoNuevo)));
                                    System.out.println("Introducido producto ["+idProducto+"] en el carrito");
                                }



                                leer.nextLine();
                                System.out.println("¿Desea introducir más productos?(s/n)");
                                confirmacion = leer.nextLine();
                            }while (confirmacion.equals("s"));

                        }

                        break;
                    case 13:
                        /*
                          Arrays.asList(new Document("$unwind",
    new Document("path", "$carrito")),
    new Document("$match",
    new Document("_id",
    new ObjectId("65f000000000000000000001"))),
    new Document("$project",
    new Document("id_producto", "$carrito.producto_id")
            .append("producto", "$carrito.nombre")
            .append("cantidad", "$carrito.cantidad")
            .append("precio_unitario", "$carrito.precio_unitario")),
    new Document("$count", "Productos en Carrito"))
                         */

                        /*
                        * Arrays.asList(new Document("$unwind",
    new Document("path", "$carrito")
            .append("includeArrayIndex", "string")),
    new Document("$match",
    new Document("_id",
    new ObjectId("65f000000000000000000001"))),
    new Document("$project",
    new Document("producto_id", "$carrito.producto_id")
            .append("producto", "$carrito.nombre")
            .append("cantidad", "$carrito.cantidad")
            .append("precio", "$carrito.precio_unitario")),
    new Document("$group",
    new Document("_id",
    new BsonNull())
            .append("cantidad",
    new Document("$sum", "$cantidad"))))
                        * */

                        if (clienteId == null){
                            System.out.println("Es necesario registrarse primero como usuario (Op.9)");
                        } else {
                            List<Bson> carritousuario = Arrays.asList(new Document("$unwind",
                                            new Document("path", "$carrito")
                                                    .append("includeArrayIndex", "string")),
                                    new Document("$match",
                                            new Document("_id",clienteId)),
                                    new Document("$project",
                                            new Document("producto_id", "$carrito.producto_id")
                                                    .append("producto", "$carrito.nombre")
                                                    .append("cantidad", "$carrito.cantidad")
                                                    .append("precio", "$carrito.precio_unitario"))) ;

                            MongoCollection<Document> colec = databaseMon.getCollection("pedidos");

                            AggregateIterable<Document> resultado = colec.aggregate(carritousuario);

                            for (Document doc: resultado ) {
                                System.out.println(doc.toJson());
                            }
                        }

                        break;
                    case 14:

                        /*
                        * Arrays.asList(new Document("$unwind",
    new Document("path", "$pedidos")
            .append("includeArrayIndex", "string")),
    new Document("$match",
    new Document("_id",
    new ObjectId("65f000000000000000000003"))),
    new Document("$project",
    new Document("pedido_id", "$pedidos.pedido_id")
            .append("fecha", "$pedidos.fecha_pedido")
            .append("producto", "$pedidos.productos.nombre")
            .append("precio", "$pedidos.productos.precio_unitario")))
                        * */

                        if (clienteId == null){
                            System.out.println("Conectate como usuario (Op.9)");
                        }else {
                            List<Document> pedidosLista = Arrays.asList(new Document("$unwind",
                                            new Document("path", "$pedidos")
                                                    .append("includeArrayIndex", "string")),
                                    new Document("$match",
                                            new Document("_id",clienteId)),
                                    new Document("$project",
                                            new Document("pedido_id", "$pedidos.pedido_id")
                                                    .append("fecha", "$pedidos.fecha_pedido")
                                                    .append("producto", "$pedidos.productos.nombre")
                                                    .append("precio", "$pedidos.productos.precio_unitario")));

                            MongoCollection<Document> cole = databaseMon.getCollection("pedidos");
                            AggregateIterable<Document> agregacion = cole.aggregate(pedidosLista);
                            for (Document doc : agregacion){
                                System.out.println(doc.toJson());
                            }
                        }


                        break;
                    case 15:

                        /*
                        * Arrays.asList(new Document("$unwind",
    new Document("path", "$carrito")
            .append("includeArrayIndex", "string")),
    new Document("$match",
    new Document("_id",
    new ObjectId("65f000000000000000000001"))),
    new Document("$project",
    new Document("idProducto", "$carrito.producto_id")
            .append("nombre", "$carrito.nombre")
            .append("cantidad", "$carrito.cantidad")
            .append("precio", "$carrito.precio_unitario")))
                        * */

                        if (clienteId == null){
                            System.out.println("Registrate antes de realizar el pedido (Op.9)");
                        }else {
                            List<Document> carritoPedidos =  Arrays.asList(new Document("$unwind",
                                            new Document("path", "$carrito")
                                                    .append("includeArrayIndex", "string")),
                                    new Document("$match",
                                            new Document("_id",clienteId)),
                                    new Document("$project",
                                            new Document("idProducto", "$carrito.producto_id")
                                                    .append("nombre", "$carrito.nombre")
                                                    .append("cantidad", "$carrito.cantidad")
                                                    .append("precio", "$carrito.precio_unitario")
                                    )
                            );

                            MongoCollection<Document> coleccionPedidos = databaseMon.getCollection("pedidos");

                            List<Document> arrayCarrito = new ArrayList<>();

                            for (Document document : coleccionPedidos.aggregate(carritoPedidos)){
                                arrayCarrito.add(document);
                            }

                            int totalPedido = 0;

                            for (Document docPe : arrayCarrito){
                                int cantidad = docPe.getInteger("cantidad");
                                double precioP = docPe.getDouble("precio");
                                totalPedido = (int) (cantidad * precioP);
                            }

                            Document nuevoPedido = new Document("pedido_id",new ObjectId()).append("productos", arrayCarrito).append("total", totalPedido).append("fecha_pedido", new Date());

                            coleccionPedidos.updateOne(new Document("_id",clienteId), new Document("$push",new Document("pedidos",nuevoPedido)));
                        }

                        break;
                    case 16:

                         /*
                        * Arrays.asList(new Document("$unwind",
    new Document("path", "$carrito")
            .append("includeArrayIndex", "string")),
    new Document("$group",
    new Document("_id", "$email")
            .append("sumaCarrito",
    new Document("$sum", "$carrito.precio_unitario"))),
    new Document("$sort",
    new Document("orden", 1L)))
                        * */

                        List<Document> ordenCarritos = Arrays.asList(new Document("$unwind",
                                        new Document("path", "$carrito")
                                                .append("includeArrayIndex", "string")),
                                new Document("$group",
                                        new Document("_id", "$email")
                                                .append("sumaCarrito",
                                                        new Document("$sum", "$carrito.precio_unitario"))),
                                new Document("$sort",
                                        new Document("orden", 1L)
                                )
                        );

                        MongoCollection<Document> col = databaseMon.getCollection("pedidos");
                        AggregateIterable<Document> agregacionCarrito = col.aggregate(ordenCarritos);
                        for (Document document : agregacionCarrito){
                            System.out.println(document.toJson());
                        }

                        break;
                    case 17:

                        /*
                        * Arrays.asList(new Document("$unwind",
    new Document("path", "$pedidos")
            .append("includeArrayIndex", "string")),
    new Document("$group",
    new Document("_id", "$email")
            .append("sumaPedidos",
    new Document("$sum", "$pedidos.total"))))
                        * */

                        List<Document> listaPedidos = Arrays.asList(new Document("$unwind",
                                        new Document("path", "$pedidos")
                                                .append("includeArrayIndex", "string")),
                                new Document("$group",
                                        new Document("_id", "$email")
                                                .append("sumaPedidos",
                                                        new Document("$sum", "$pedidos.total"))
                                )
                        );

                        MongoCollection<Document> colPed = databaseMon.getCollection("pedidos");
                        AggregateIterable<Document> agrePed = colPed.aggregate(listaPedidos);
                        for (Document document : agrePed){
                            System.out.println(document.toJson());
                        }

                        break;
                    case 18:System.out.println("Chao :)");
                        break;
                    default: System.out.println("No entendí ["+opcion+"]");
                        break;
                }


        }while (opcion != 18);

    }
}