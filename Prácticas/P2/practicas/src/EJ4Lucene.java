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

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;


import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EJ4Lucene {



  public static void tokenizeString(Analyzer analyzer, String string, PrintWriter writer){
    try{
      /*Como resultado del analizador estandar obtenemos un objeto TokenStream
      que nos permitirá enumerar la secuencia de tokens.*/
      TokenStream stream = analyzer.tokenStream(null, string);
      /*Creamos un TokenStream a partir del anterior que elimina las palabras de
      tamaño menor que 4, y las que no las reduce a sus ultimas 4 letras.*/
      stream = new ultimas4Letras(stream);
      //Obtenemos estos atributos de stream donde cAtt es el token y offsetAtt su posición
      OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
      CharTermAttribute cAtt = stream.addAttribute(CharTermAttribute.class);

      /*Vamos obteniendo todos los tokens e imprimiendolos junto con sus posiciones.*/
      stream.reset();
      while(stream.incrementToken()){
        writer.print(cAtt.toString() + " : [" + (offsetAtt.endOffset()-4) + "," + offsetAtt.endOffset() + "]\n");
      }
      stream.end();
    } catch(IOException e){
      throw new RuntimeException(e);
    }
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


    	  //Si no existe, creamos un directorio llamado CSV en el directorio raíz
   	    File csv = new File("./TOKENS4");
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

          //Creamos archivos .csv con el recuento de las palabras de cada documento en CSV/
          PrintWriter writer = new PrintWriter("./TOKENS4/" + file + ".txt");
          //Creamos un analizador que se encarga de obtener los tokens quitando espacios,
          //signos de puntuación, etc.
          Analyzer an = new StandardAnalyzer();
          tokenizeString(an, str, writer);
          writer.close();
        }

    }
  }
