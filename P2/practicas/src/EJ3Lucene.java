import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.standard.UAX29URLEmailTokenizer;

import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.language.LanguageIdentifier;


public class EJ3Lucene{

	//Imprime los tokens extraídos por el analizador en un archivo haciendo uso de la clase PrintWriter
	public static void imprimirTokens(TokenStream stream, PrintWriter writer) throws IOException{
		OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
		stream.reset();
		while(stream.incrementToken()){
			writer.print(stream.getAttribute(CharTermAttribute.class) + " : [" + (offsetAtt.startOffset()) + "," + offsetAtt.endOffset() + "]\n");
		}
		stream.end();
		stream.close();
		writer.close();
	}

	//Sobrecargamos el comportamiento del analizador para obtener un constructor del Analyzer personalizado
	public static Analyzer buildAnalyzer(final String language, final CharArraySet stopwords, final int min){
		return new Analyzer(){
			@Override
			protected TokenStreamComponents createComponents(String fieldname){
				final Tokenizer source = new UAX29URLEmailTokenizer();
				TokenStream result = new LowerCaseFilter(source);
				result = new StopFilter(result, stopwords);
				result = new NumerosFilter(result);
				result = new SnowballFilter(result, language);
				result = new LengthFilter(result, min, 1000);
				return new TokenStreamComponents(source, result);
			}
		};
	}
	//Implementamos un filtro para eliminar los números decimales separados por ,. y aquellos que sean distintos de 3 y 4 cifras
	static class NumerosFilter extends FilteringTokenFilter{
		private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
		public NumerosFilter(TokenStream in){
			super(in);
		}
		@Override
		protected boolean accept() throws IOException{
			String token = new String(termAtt.buffer(), 0, termAtt.length());
			if(token.matches("[0-9]{1,2}") || token.matches("[0-9]{5,}") || token.matches("[0-9]+[.,][0-9]+")){
				return false;
			}
			return true;
		}
	}

	public static void main(String[] args) throws IOException{
		/*Diseñar un analizador propio sobrecargando la clase Analyzer */
		File directory = new File(args[0]);
		String[] ficheros = directory.list();

		Tika tika = new Tika();
    tika.setMaxStringLength(1000000000);
		Metadata metadata = new Metadata();
		System.out.println();

		//Si no existe, creamos un directorio llamado PARSER3 en el directorio raiz
		File p3 = new File("./TOKENS3");
		if (!p3.exists())
			p3.mkdir();

		//Tenemos un analizador diferente para cada idioma, donde cambian la lista de palabras vacías y las raíces del SnowballFilter
		Analyzer an_es = buildAnalyzer("Spanish", SpanishAnalyzer.getDefaultStopSet(), 4);
		Analyzer an_en = buildAnalyzer("English", EnglishAnalyzer.getDefaultStopSet(), 4);

		for(String file : ficheros){
				File f = new File("./" + args[0] + "/" + file);
				tika.parse(f, metadata);
				String contenido = new String();
				//Parseamos el contenido del archivo
				try{
					contenido = tika.parseToString(f);
		 			System.out.println("Archivo Parseado y Analizado: " + file);
				}catch (Exception e){
					System.out.println("Error al parsear el archivo.\n");
				}
				//Detectamos el idioma del archivo (inglés o español)
				LanguageIdentifier identifier = new LanguageIdentifier(contenido);
				String idioma = identifier.getLanguage();
				//Creamos archivos .txt con el resultado de aplicar nuestro Analyzer a cada documento
				PrintWriter writer = new PrintWriter("./TOKENS3/" + file + ".txt");
				//Utilizamos un analizador diferente dependiendo del idioma
				if(idioma.equals("es"))
					imprimirTokens(an_es.tokenStream(null, contenido), writer);
				else
					imprimirTokens(an_en.tokenStream(null, contenido), writer);
		}
	}
}
