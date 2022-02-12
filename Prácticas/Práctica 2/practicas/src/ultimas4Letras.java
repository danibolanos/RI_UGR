

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


// Sobrecargamos la clase TokenFilter para crear nuestro propio filtro.
public class ultimas4Letras extends TokenFilter{

  private final CharTermAttribute cAtt = addAttribute(CharTermAttribute.class);

  public ultimas4Letras(TokenStream in){
    super(in);
  }

  /*Método accept que se encarga de decir que tokens devuelve incrementToken() y cuales no.*/
  public boolean accept() throws IOException{
    return cAtt.length() >= 4;
  }
  //Sobrecargamos incrementToken() para obtener los tokens deseados
  @Override
  public final boolean incrementToken() throws IOException{

    //Vamos recorriendo los tokens del TokenStream que nos han pasado en el constructor.
    while(input.incrementToken()){
      /*Si el token no es de longitud menor que cuatro modificamos el token y sus
        atributos para quedarnos como token las últimas 4 letras del mismo.*/
      if(accept()){
        char[] buffer = new char[4];
        char[] s = cAtt.buffer();

        for(int i=0; i < 4; ++i)
          buffer[i] = s[cAtt.length()-(4-i)];

        cAtt.setLength(4);
        cAtt.copyBuffer(buffer, 0, 4);

        return true;
      }


    }
    return false;
  }

}
