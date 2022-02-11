import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.File;
import java.util.*;

import java.util.stream.Collectors;

public class JsonReader {

  //Atributos del documento JSON
  private Integer tamanio = null;
  private String titulo = null;
  private List<String> autores = null;
  private List<String> paises = null;
  private List<String> instituciones = null;
  private String absText = null;
  private String bodyText = null;
  private String nombreArchivo = null;

  //Declaración de constructor
  JsonReader(File f) {
    //Definimos un parser para el archivo JSON
    JSONParser parser = new JSONParser();
    //Extraemos toda la información del JSON
    try (Reader reader = new FileReader(f)) {
      JSONObject archivo = (JSONObject) parser.parse(reader);
      JSONObject metadataObject = (JSONObject) archivo.get("metadata");
      JSONArray authorsObject = (JSONArray) metadataObject.get("authors");
      JSONArray abstractObject = (JSONArray) archivo.get("abstract");
      JSONArray bodyTextObject = (JSONArray) archivo.get("body_text");
      titulo = (String) metadataObject.get("title");
      autores = extraeAutores(authorsObject);
      paises = extraePais(authorsObject);
      instituciones = extraeInstitucion(authorsObject);
      absText = extraeText(abstractObject);
      bodyText = extraeText(bodyTextObject);
      tamanio = (int) Math.round(f.length()/1024);
      nombreArchivo = f.getName();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  //Funciones para extraer los atributos de la clase
  public int getTamanio(){
    return tamanio;
  }

  public String getTitulo(){
    if(titulo.equals(""))
      titulo = "Unknown";
    return titulo;
  }

  public String getAbstract(){
    if(absText.equals(""))
      absText = "Unknown";
    return absText;
  }

  public String getTexto(){
    if(bodyText.equals(""))
      bodyText = "Unknown";
    return bodyText;
  }

  public String getArchivo(){
    return nombreArchivo;
  }

  //Devuelve las listas de String
  public List<String> getListaAutores(){
    return autores;
  }

  public List<String> getListaInstituciones(){
    return instituciones;
  }

  public List<String> getListaPaises(){
    return paises;
  }

  //Devuelve las listas como un solo String separado por ","
  public String getPaises(){
    String joined = String.join(", ", paises);
    if(joined.equals(""))
      joined = "Unknown";
    return joined;
  }

  public String getAutores(){
    String joined = String.join(", ", autores);
    if(joined.equals(""))
      joined = "Unknown";
    return joined;
  }

  public String getInstituciones(){
    String joined = String.join(", ", instituciones);
    if(joined.equals(""))
      joined = "Unknown";
    return joined;
  }

  //Funciones utilizadas para extraer los atributos del archivo JSON

  //Elimina palabras duplicadas en un String
  public List<String> removeDuplicates(String s) {
    String[] parts = s.split(", ");
    List<String> listado = Arrays.asList(parts);
    listado = listado.stream().distinct().collect(Collectors.toList());
    return listado;
  }

  //Extrae los autores en una lista de String
  public List<String> extraeAutores(JSONArray listaAutores){
    List<String> autores = new ArrayList<String>();
    Iterator<JSONObject> iterator = listaAutores.iterator();
    while (iterator.hasNext()) {
      String nombre = new String();
      JSONObject it = iterator.next();
      String primero = (String) it.get("first");
      String medio = new String();
      String ultimo = (String) it.get("last");
      JSONArray mid = (JSONArray) it.get("middle");
      Iterator<JSONObject> it_mid = mid.iterator();
      while(it_mid.hasNext())
        medio += it_mid.next();
      if(!primero.equals("")){
        if(!medio.equals("") || !ultimo.equals(""))
          nombre += primero + " ";
        else
          nombre += primero;
      }
      if(!medio.equals("")){
        if(!ultimo.equals(""))
          nombre += medio + " ";
        else
          nombre += medio;
      }
      if(!ultimo.equals(""))
        nombre += ultimo;
      autores.add(nombre);
    }
    return autores;
  }

  //Extrae los países en una lista de String
  public List<String> extraePais(JSONArray listaAutores){
    List<String> listaPaises = new ArrayList<String>();
    Iterator<JSONObject> iterator = listaAutores.iterator();
    while (iterator.hasNext()) {
      JSONObject it = iterator.next();
      JSONObject aff = (JSONObject) it.get("affiliation");
      JSONObject loc = (JSONObject) aff.get("location");
      if(loc != null){
        String pais = (String) loc.get("country");
        if(pais != null){
          List<String> lista = removeDuplicates(pais);
          for (String npais : lista)
            if(!npais.equals(""))
              listaPaises.add(npais);
        }
      }
    }
    //Elimina los países repetidos (comentar si se quieren aumentar sus pesos)
    //listaPaises = listaPaises.stream().distinct().collect(Collectors.toList());
    return listaPaises;
  }

  //Extrae las instituciones en una lista de String
  public List<String> extraeInstitucion(JSONArray listaAutores){
    List<String> listaInsts = new ArrayList<String>();
    Iterator<JSONObject> iterator = listaAutores.iterator();
    while (iterator.hasNext()) {
      JSONObject it = iterator.next();
      JSONObject aff = (JSONObject) it.get("affiliation");
      if(aff != null){
       String ins = (String) aff.get("institution");
       if(ins != null && !ins.equals(""))
        listaInsts.add(ins);
      }
    }
    //Elimina las instituciones repetidas (comentar si se quieren aumentar sus pesos)
    //listaInsts = listaInsts.stream().distinct().collect(Collectors.toList());
    return listaInsts;
  }

  //Extrae el texto como lista de String
  public List<String> extraeListText(JSONArray txt){
    List<String> listaTexto = new ArrayList<String>();
    Iterator<JSONObject> iterator = txt.iterator();
    while (iterator.hasNext()) {
      JSONObject it = iterator.next();
      String texto = (String) it.get("text");
      listaTexto.add(texto);
    }
    return listaTexto;
  }

  //Extrae el texto como un único String
  public String extraeText(JSONArray txt){
    String listaTexto = new String();
    Iterator<JSONObject> iterator = txt.iterator();
    while (iterator.hasNext()) {
      JSONObject it = iterator.next();
      String texto = (String) it.get("text");
      listaTexto += texto + " ";
    }
    return listaTexto;
  }
}
