import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.standard.UAX29URLEmailTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;


public class MiAnalizador extends Analyzer {

  private CharArraySet stopwords = null;
  private String language = null;
  private boolean flagAnios = false;
  private boolean flagURL = false;
  private boolean flagSynonym = false;

  //stop: Conjunto de palabras vacías (si es null no se aplica)
  //lang: Lenguaje para realizar stemming (si es null no se aplica)
  //f1: boolean para aplicar o no el filtro de años
  //f2: boolean para aplicar o no el tokenizer de URLs
  //f3: boolean para aplicar o no los sinónimos
  MiAnalizador(CharArraySet stop, String lang, boolean f1, boolean f2, boolean f3){
    super();
    stopwords = stop;
    language = lang;
    flagAnios = f1;
    flagURL = f2;
    flagSynonym = f3;
  }

  //Construir el diccionario de sinónimos manualmente
  private SynonymMap buildSynonymMap(){
  SynonymMap.Builder builder = new SynonymMap.Builder(true);
  builder.add(new CharsRef("eeuu"), new CharsRef("usa"), true);
  builder.add(new CharsRef("usa"), new CharsRef("eeuu"), true);
  builder.add(new CharsRef("uk"), new CharsRef("united kingdom"), true);
  builder.add(new CharsRef("pr china"), new CharsRef("china"), true);
  builder.add(new CharsRef("china"), new CharsRef("pr china"), true);
  builder.add(new CharsRef("covid"), new CharsRef("covid-19"), true);
  builder.add(new CharsRef("covid"), new CharsRef("covid 19"), true);
  SynonymMap map = null;
  try {
    map = builder.build();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return map;
  }

  protected TokenStreamComponents createComponents(String fieldname){
    Tokenizer source = new StandardTokenizer();
    if(flagURL)
      source = new UAX29URLEmailTokenizer();
    TokenStream result = new LowerCaseFilter(source);
    if(flagAnios)
      result = new AniosFilter(result);
    if(flagSynonym)
      result = new SynonymFilter(result, buildSynonymMap(), true);
    if(stopwords != null)
      result = new StopFilter(result, stopwords);
    if(language != null)
      result = new SnowballFilter(result, language);
    return new TokenStreamComponents(source, result);
  }

}
