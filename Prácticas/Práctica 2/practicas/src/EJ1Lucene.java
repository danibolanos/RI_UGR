import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.ArrayList;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.language.*;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.Link;


import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;



import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EJ1Lucene {

  public static void statisticAnalyzer(ArrayList<Analyzer> analyzers, ArrayList<String> types, String str, String file){

    try{

      //Creamos archivos .csv con el recuento de las palabras de cada documento en CSV/
      PrintWriter writer = new PrintWriter("./Estadisticas/" + file + ".txt");

      for(int i=0; i < analyzers.size(); ++i){
        /*Como resultado del analizador estandar obtenemos un objeto TokenStream
        que nos permitirá enumerar la secuencia de tokens.*/
        TokenStream stream = analyzers.get(i).tokenStream(null, str);

        CharTermAttribute cAtt = stream.addAttribute(CharTermAttribute.class);

        List<String> list = new ArrayList<String>();

        stream.reset();
        //Añadimos todos los tokens del TokenStream a una lista
        while(stream.incrementToken()){
          list.add(cAtt.toString());
        }
        stream.end();

        //stream() crea una Stream cuyos elementos son los elementos de list
        //collect() nos devuelve un Map que se crea a través de Collectors.toMap()
        //Collectors.toMap() crea un Map donde los keys son los elementos del stream
        //Cada key tiene como valor 1 en el Map y las colisiones se resuelven sumando los valores
        //Asi obtenemos un Map donde cada Key es la palabra que aparece y el valor su frecuencia.
        Map <String, Integer > wordCounter = list.stream()
                              .collect(Collectors.toMap(w -> w, w -> 1, Integer::sum));
        //Ordenamos el Map segun los valores.
        wordCounter = sortByValue(wordCounter);
        Iterator it = wordCounter.keySet().iterator();

        //Creamos archivos .txt con el recuento de los tokens de cada documento en Estadisticas/
        writer.print("----------------------------" + types.get(i) + "----------------------------\n");
        writer.print("Numero de tokens en el archivo: " + wordCounter.size() + "\n");
        while(it.hasNext()){
          String key = (String) it.next();
            writer.print(key + ": " + wordCounter.get(key) + "\n");
        }

      }

      writer.close();

    } catch(IOException e){
      throw new RuntimeException(e);
    }
  }

  public static Map<String, Integer> sortByValue(final Map<String, Integer> wordCounts) {
  /*Introduce las entradas del Map en un Stream, lo ordena con los valores que tiene el Map
  y crea otro map con las entradas ya ordenadas y con los valores del antiguo Map.*/
    return wordCounts.entrySet()
            .stream()
            .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  public static void main(String[] args) throws Exception{

    if(args.length < 1){
      System.out.println("Por favor, introduzca el directorio.");
      System.exit(0);
    }
    //Obtenemos un Array de Strings con los nombres de todos los archivos que
    //contiene el directorio pasado como argumento
    File directorio = new File(args[0]);
    String[] archivos = directorio.list();

    //Creamos un objeto Tika
    Tika tika = new Tika();
    //Establecemos el limite de caracteres para los strings
    tika.setMaxStringLength(1000000000);

    //Representa los metadatos del documento
    Metadata metadata = new Metadata();

    System.out.println("\n");

    if(true){

    	  //Si no existe, creamos un directorio llamado CSV en el directorio raiz
   	    File csv = new File("./Estadisticas");
    	  if (!csv.exists())
          csv.mkdir();

  		  for(String file : archivos){
          File f = new File("./" + args[0] + "/" + file);
          //Establece el límite de caracteres en el constructor
          BodyContentHandler handler = new BodyContentHandler(1000000000);
          //Definimos un objeto para el contexto y el parser
          ParseContext parseContext = new ParseContext();
          AutoDetectParser parser = new AutoDetectParser();
          FileInputStream stream = new FileInputStream(f);
          try {
            parser.parse(stream, handler, metadata, parseContext);
		 		System.out.println("Archivo Parseado y Analizado: " + file);
          } finally{
            stream.close();
          }
          //Pasamos el contenido del documento del objeto handler a un string
          String str = handler.toString();

          //Guardamos los analizadores que vayamos a utilizar en un ArrayList
          ArrayList<Analyzer> analyzers = new ArrayList<Analyzer>();
          ArrayList<String> types = new ArrayList<String>();

          analyzers.add(new WhitespaceAnalyzer());
          analyzers.add(new SimpleAnalyzer());
          analyzers.add(new StandardAnalyzer());
          analyzers.add(new SpanishAnalyzer());

          types.add("WhitespaceAnalyzer");
          types.add("SimpleAnalyzer");
          types.add("StandardAnalyzer");
          types.add("SpanishAnalyzer");

          /*Para cada archivo le pasamos los analizadores guardados en el ArrayList
            y calculamos las frecuencias a los tokens.*/
          statisticAnalyzer(analyzers, types,  str, file);



        }

    }
  }
}
