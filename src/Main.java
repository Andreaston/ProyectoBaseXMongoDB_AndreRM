import org.basex.examples.api.BaseXClient;

import java.io.IOException;
import java.util.Scanner;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException {
        //BaseXClient session = new BaseXClient("localhost", 1984, "admin", "123abc")
        BaseXClient sesion = new BaseXClient("localhost",1984,"admin","123abc");

        sesion.execute("OPEN productos");

        Scanner leer = new Scanner(System.in);
        int opcion = 0;

        do {
            System.out.println("1. Modificar valor XML por ID\n2. Eliminar producto\n3. Obtener todos los productos por orden alfabético\n4. Listar productos por disponibilidad\n5. Mostrar producto más caro por categoría" +
                    "\n6. Mostrar nombre y fabricante de productos con subcanedas a buscar\n7. Mostrar cantidad total de productos en cada categoría y calcular el porcentaje que representa del stock\n6. SALIR");

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

                        var sentencia = sesion.query(query);

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

                        var queryDelete = sesion.query(eliminar);

                        queryDelete.bind("id", idDelete);

                        queryDelete.execute();
                        queryDelete.close();


                        break;
                    case 3:

                        //for $p in //producto order by $p/nombre return $p/(id | nombre | precio | disponibilidad | categoria )

                        BaseXClient.Query productosOrden = sesion.query("for $p in //producto order by $p/nombre return $p/(id | nombre | precio | disponibilidad | categoria )");

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


                        var queryBuscar = sesion.query(buscar);

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

                        var queryBuscar = sesion.query(buscar);

                        queryBuscar.bind("cantidad", n);

                        while (queryBuscar.more()){
                            System.out.println(queryBuscar.next());
                        }

                        queryBuscar.close();

                        break;
                    case 5:

                        //for $cat in distinct-values(//producto/categoria) let $productosCat := //producto[categoria = $cat] let $maxPrecio := max($productosCat/precio) return $productosCat[precio = $maxPrecio]/(nombre | precio | categoria)

                        BaseXClient.Query queryCaro = sesion.query("for $cat in distinct-values(//producto/categoria) let $productosCat := //producto[categoria = $cat] let $maxPrecio := max($productosCat/precio) return $productosCat[precio = $maxPrecio]/(nombre | precio | categoria)");

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

                        var queryBusSub = sesion.query(querySub);
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

                        BaseXClient.Query media = sesion.query("for $p in distinct-values(//producto/categoria) let $lista := //producto[categoria = $p] let $suma := sum($lista/disponibilidad) let $sumaT := sum(//producto/disponibilidad)let $porce := ($suma div $sumaT) * 100 return <categoria> <nombre>{$p}</nombre> <stock>{$suma}</stock> <media>{$porce}</media></categoria>");

                        while (media.more()){
                            System.out.println(media.next());
                        }


                        break;
                }


        }while (opcion != 8);

    }
}