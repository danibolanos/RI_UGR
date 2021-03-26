import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.FilteringTokenFilter;


//Implementamos un filtro para eliminar los números decimales separados por ,. y aquellos que sean distintos de 3 y 4 cifras
public class AniosFilter extends FilteringTokenFilter{
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  public AniosFilter(TokenStream in){
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
