import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.*;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.language.*;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.Link;

import java.util.stream.Collectors;
import java.util.stream.Stream;


public class P1Tika {

  public static Map<String, Integer> sortByValue(final Map<String, Integer> wordCounts) {
  /*Introduce las entradas del Map en un Stream, lo ordena con los valores que tiene el Map
  y crea otro map con las entradas ya ordenadas y con los valores del antiguo Map.*/
    return wordCounts.entrySet()
            .stream()
            .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  public static void main(String[] args) throws Exception{

    if(args.length < 2){
      System.out.println("Por favor, introduzca opción (-d,-l,-t) y directorio.");
      System.exit(0);
    }
    //Obtenemos un Array de Strings con los nombres de todos los archivos que
    //contiene el directorio pasado como argumento
    File directorio = new File(args[1]);
    String[] archivos = directorio.list();

    //Creamos un objeto Tika
    Tika tika = new Tika();
    //Establecemos el limite de caracteres para los strings
    tika.setMaxStringLength(1000000000);

    //Representa los metadatos del documento
    Metadata metadata = new Metadata();

    System.out.println("\n");

    if(args.length>1){
      //Opción -d (Tabla con nombre, codificación, tipo e idioma del fichero)
      if(args[0].toString().equals("-d")){
        //Establecemos un formato para la tabla
        String formato = "| %-35s   |  %-9s | %-73s | %-5s |%n";
        System.out.format(formato, "NAME", "ENCODING", "TYPE", "LANG.");
        System.out.format("|----------------------------------------------------------------------------------------------------------------------------------------|%n");
        //Recorremos para cada archivo del directorio
        for(String file : archivos){
          File f = new File("./" + args[1] + "/" + file);
          //Establece el límite de caracteres en el constructor
          BodyContentHandler handler = new BodyContentHandler(1000000000);
          //Guarda la información del contexto concreto para el ContentHandler
          ParseContext parseContext = new ParseContext();
          //Creamos un objeto para parsear el documento
          AutoDetectParser parser = new AutoDetectParser();
          //Utilizamos parser para que a través del stream de datos del documento, content,
          //nos devuelva el contenido en el objeto handler y los metadatos en medatada
          FileInputStream stream = new FileInputStream(f);
          try {
            parser.parse(stream, handler, metadata, parseContext);
          } finally{
            stream.close();
          }
          //Obtenemos los metadatos del documento.
          tika.parse(f, metadata);
          //Creamos un objeto LanguageIdentifier al que le pasamos un string con el
          //contenido del documento para extraer el idioma en el que esté escrito.
          LanguageIdentifier object = new LanguageIdentifier(handler.toString());
          //Extraemos el nombre del documento, la codificación, el MIME tipo del archivo y el idioma en que esta escrito
          String nam = metadata.get(Metadata.RESOURCE_NAME_KEY);
          String encod = metadata.get(Metadata.CONTENT_ENCODING);
          String type = metadata.get(Metadata.CONTENT_TYPE);
          String lang = object.getLanguage();
          //Mostramos en el formato generado
  				System.out.format(formato, nam, encod, type, lang);
          System.out.format("|----------------------------------------------------------------------------------------------------------------------------------------|%n");
        }
        System.out.println("\n");
      }
      //Opción -l (Extraemos todos los enlaces de cada documento)
      else if(args[0].toString().equals("-l")){
        for(String file : archivos){
          File f = new File("./" + args[1] + "/" + file);
          //Definimos un handler para links
          LinkContentHandler linkHandler = new LinkContentHandler();
          //Definimos un objeto para el contexto y el parser
          ParseContext parseContext = new ParseContext();
          AutoDetectParser parser = new AutoDetectParser();
          //Utilizamos parser para que a través del stream de datos del documento, content,
          //nos devuelva el contenido en el objeto handler y los metadatos en medatada
          FileInputStream stream = new FileInputStream(f);
          try {
            parser.parse(stream, linkHandler, metadata, parseContext);
          } finally{
            stream.close();
          }
          //Guardamos los enlaces en una lista de Links
          List<Link> enlaces = linkHandler.getLinks();
          //Imprimimos aquellos enlaces que no sean vacíos
          System.out.println("ENLACES del archivo " + file + ":\n");
          if(enlaces.isEmpty())
            System.out.println("NO se han encontrado enlaces.");
          else{
            for(Link l:enlaces)
              System.out.println(l);
          }
          System.out.println("\n");
        }
      }
      //Opción -t (Generar csv con las ocurrencias de palabras de cada archivo)
      else if(args[0].toString().equals("-t")){
    	  //Si no existe, creamos un directorio llamado CSV en el directorio raiz
   	    File csv = new File("./CSV");
    	  if (!csv.exists())
          csv.mkdir();

  		  for(String file : archivos){
          File f = new File("./" + args[1] + "/" + file);
          //Establece el límite de caracteres en el constructor
          BodyContentHandler handler = new BodyContentHandler(1000000000);
          //Definimos un objeto para el contexto y el parser
          ParseContext parseContext = new ParseContext();
          AutoDetectParser parser = new AutoDetectParser();
          FileInputStream stream = new FileInputStream(f);
          try {
            parser.parse(stream, handler, metadata, parseContext);
          } finally{
            stream.close();
          }
          //Pasamos el contenido del documento del objeto handler a un string
          String str = handler.toString();
          //Crea un Stream a partir del string.
          //Mapea el elemento del stream a un array de strings con map().
          //Los elementos de este array de strings son las palabras separadas a través de split().
          //split() utiliza una expresion regular para separar las palabras.
          //flatMap() mapea el array a un stream que tiene como elementos los elementos del array.
          //collect() nos devuelve una lista de strings con los elementos del stream
          List <String> list = Stream.of(str).map(w -> w.split("[\\s\\;\\.\\,\\:\\?\\¿\\¡\\!\\(\\)]+")).flatMap(Arrays::stream)
                        .collect(Collectors.toList());
          //stream() crea una Stream cuyos elementos son los elementos de list
          //collect() nos devuelve un Map que se crea a través de Collectors.toMap()
          //Collectors.toMap() crea un Map donde los keys son los elementos del stream
          //Cada key tiene como valor 1 en el Map y las colisiones se resuelven sumando los valores
          //Asi obtenemos un Map donde cada Key es la palabra que aparece y el valor su frecuencia.
          Map <String, Integer > wordCounter = list.stream()
                                .collect(Collectors.toMap(w -> w.toLowerCase(), w -> 1, Integer::sum));
          //Ordenamos el Map segun los valores.
          wordCounter = sortByValue(wordCounter);
          Iterator it = wordCounter.keySet().iterator();
          //Creamos archivos .csv con el recuento de las palabras de cada documento en CSV/
          PrintWriter writer = new PrintWriter("./CSV/" + file + ".csv");
          writer.print("Text" + ";" + "Size\n");
          while(it.hasNext()){
            String key = (String) it.next();
            writer.print(key + ";" + wordCounter.get(key) + "\n");
          }
          writer.close();
        }
      }
      else
        System.out.println("ERROR: El parámetro debe estar en la siguiente lista [-d,-l,-t].\n");
    }
  }
}
