import java.io.IOException;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.commongrams.CommonGramsFilter;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;


public class EJ2Lucene{

	public static void probarTokenFilter(TokenStream stream, String filter) throws IOException{
		System.out.println("\n-----------------------" + filter + "-----------------------\n");
		stream.reset();
		while(stream.incrementToken()){
			System.out.println(stream.getAttribute(CharTermAttribute.class));
		}
		stream.end();
		stream.close();
		System.out.println();
	}

	public static void main(String[] args) throws IOException{
		/*Probar el efecto de los siguientes tokenFilters: StandardFilter, LowerCaseFilter, StopFilter, SnowballFilter,
		ShingleFilter, EdgeNGramCommonFilter, NGramTokenFilter, CommonGramsFilter, SynonymFilter*/
		String cadena = "En un lugar de la Mancha, de cuyo nombre no quiero acordarme.";
		System.out.println("Texto original: " + cadena);
		//Usaremos el WhitespaceAnalyzer para extraer los tokens de la cadena de texto
		Analyzer whitean = new WhitespaceAnalyzer();
		//Definiremos el SimpleAnalyzer para observar diferentes comportamientos
		Analyzer stdan = new StandardAnalyzer();
		//Mostramos el resultado obtenido por defecto por el analizador WhitespaceAnalyzer y StandardAnalyzer
		probarTokenFilter(whitean.tokenStream(null, cadena), "WhitespaceAnalyzer");
		probarTokenFilter(stdan.tokenStream(null, cadena), "StandardAnalyzer");
		//Sobre la tokenización realizada por el analizador por defecto realizaremos el filtrado
		//StandardFilter no se incluye en la versión 8.6.2
		//Convierte los tokens a minúsculas
		probarTokenFilter(new LowerCaseFilter(whitean.tokenStream(null, cadena)), "WhitespaceAnalyzer+LowerCaseFilter");
		//Cargamos el conjunto de palabras vacías contenido en el SpanishAnalyzer
		CharArraySet stopSet = SpanishAnalyzer.getDefaultStopSet();
		Iterator iter = stopSet.iterator();
		//Elimina los tokens de la cadena que se encuentren en el conjunto de palabras vacías
		probarTokenFilter(new StopFilter(stdan.tokenStream(null, cadena), stopSet), "StandardAnalyzer+StopFilter");
		//Mostramos las 10 primeras palabras vacías del conjunto
		System.out.println("Primeras 20 StopWords del SpanishAnalyzer:");
		for(int i=0; i<20; i++){
			System.out.print((char []) iter.next());
			System.out.print(", ");
		}
		System.out.print("...\n\n");
		//Realiza según el idioma un stemmed de bola de nieve, reduciendo cada token a su raíz
		probarTokenFilter(new SnowballFilter(stdan.tokenStream(null, cadena), "Spanish"), "StandardAnalyzer+SnowballFilter");
		//Realiza combinaciones de tokens de una longitud dada como un único token
		probarTokenFilter(new ShingleFilter(stdan.tokenStream(null, cadena)), "StandardAnalyzer+ShingleFilter");
		//Reduce los tokens a la longitud dada, tomando las N primeras letras, si es de menor longitud, lo elimina
		probarTokenFilter(new EdgeNGramTokenFilter(stdan.tokenStream(null, cadena), 5), "StandardAnalyzer+EdgeNGramTokenFilter");
		//Reduce los tokens a una longitud dada como el filtro anterior, pero además se queda con combinaciones de N letras de cada token
		probarTokenFilter(new NGramTokenFilter(stdan.tokenStream(null, cadena), 4), "StandardAnalyzer+NGramTokenFilter");
		//Añadimos algunas common words necesarias para el funcionamiento del CommonGramsFilter
		String[] words = new String[] {"lugar", "mancha", "nombre"};
		CharArraySet commonWords = new CharArraySet(words.length, false);
		commonWords.addAll(Arrays.asList(words));
		//Realiza combinaciones de las palabras establecidas como commonWords junto las que aparecen próximas en un único token
		probarTokenFilter(new CommonGramsFilter(stdan.tokenStream(null, cadena), commonWords), "StandardAnalyzer+CommonGramsFilter");
		//Creamos un diccionario pequeño de sinónimos para algunas palabras del texto
		SynonymMap.Builder builder = new SynonymMap.Builder(true);
    builder.add(new CharsRef("lugar"), new CharsRef("sitio"), true);
		builder.add(new CharsRef("lugar"), new CharsRef("espacio"), true);
		builder.add(new CharsRef("nombre"), new CharsRef("apelativo"), true);
		builder.add(new CharsRef("mancha"), new CharsRef("Castilla La Mancha"), true);
		SynonymMap synonymMap = builder.build();
		//Crea tokens de los sinónimos de las palabras que se encuentren en el texto
		probarTokenFilter(new SynonymFilter(stdan.tokenStream(null, cadena), synonymMap, true), "StandardAnalyzer+SynonymFilter");
	}
}
