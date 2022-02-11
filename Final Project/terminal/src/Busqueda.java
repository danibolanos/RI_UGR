import org.apache.lucene.document.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.*;
import java.nio.file.Paths;
import java.util.ArrayList;


import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.analysis.CharArraySet;


import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.LabelAndValue;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.QueryBuilder;



public class Busqueda{

  String INDEX_PATH = "./index";
	String FACET_PATH = "./facet";
  IndexSearcher searcher = null;
  FacetsCollector fc = null;
  FacetsConfig fconfig = null;
  int TOP = 5;
  int DOCUMENTOS = 10;
  TaxonomyReader taxoReader = null;
  DirectoryReader indexReader = null;
  TopDocs results = null;
  boolean mostrarTodo = true;

  public TopDocs ConsultaGenerica(String campo , Analyzer analyzer) throws IOException {
      //Creo la entrada y la redirijo hacia un buffer
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
      //El campo por defecto será texto y la consulta sera analizada con analyzer
      QueryParser parser = new QueryParser(campo, analyzer);
      String line;

      do{
        System.out.print("\nIntroduzca la consulta: ");
        line = in.readLine();
      }while(line == null || line.length() <= 0);

      line = line.trim();

      Query query = null ;
      TopDocs docs = null;

      try{
          query = parser.parse(line);
          docs = searcher.search(query, DOCUMENTOS);

      }catch (org.apache.lucene.queryparser.classic.ParseException e){
          System.out.println("Error al procesar consulta. ");
      }

      long totalHits = docs.totalHits.value;
      System.out.print("\nSe han encontrado " + totalHits + " documento/s en total.");

      System.out.print(" ¿Desea mostrar las facetas? y/n: ");
      String res =  in.readLine();
      if(res.equals("y"))
          MostrarFacetas(query);
      return docs;
  }

  public TopDocs ConsultaBooleana() throws IOException {
    TopDocs docs = null;
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    Query query = null;
    ArrayList<String> tipoInfo = new ArrayList<String>();
    ArrayList<String> campos = new ArrayList<String>();
    ArrayList<Query> consultas = new ArrayList<Query>();
    String line;

    tipoInfo.add("titulo");
    tipoInfo.add("autores");
    tipoInfo.add("paises");
    tipoInfo.add("instituciones");
    tipoInfo.add("abstract");
    tipoInfo.add("texto");

    System.out.print("\nTitulo: ");
    campos.add(in.readLine());

    System.out.print("Autores: ");
    campos.add(in.readLine());

    System.out.print("Paises: ");
    campos.add(in.readLine());

    System.out.print("Instituciones: ");
    campos.add(in.readLine());

    System.out.print("Abstract: ");
    campos.add(in.readLine());

    System.out.print("Texto: ");
    campos.add(in.readLine());

    Analyzer analyzer = null;
    QueryBuilder builder = null;

    if(campos.get(1).compareTo("") != 0){
      analyzer = new MiAnalizador(null, null, true, false, false);
      builder = new QueryBuilder(analyzer);
      consultas.add(builder.createPhraseQuery("autores", campos.get(1)));
    }

    if(campos.get(2).compareTo("") != 0){
      analyzer = new MiAnalizador(null, null, false, false, true);
      builder = new QueryBuilder(analyzer);
      consultas.add(builder.createPhraseQuery("paises", campos.get(2)));
    }

    if(campos.get(3).compareTo("") != 0){
      analyzer = new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", false, false, true);
      builder = new QueryBuilder(analyzer);
      consultas.add(builder.createPhraseQuery("instituciones", campos.get(3)));
    }

    if(campos.get(0).compareTo("") != 0){
      analyzer = new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", true, false, true);
      builder = new QueryBuilder(analyzer);
      consultas.add(builder.createPhraseQuery("titulo", campos.get(0)));
    }
    if(campos.get(4).compareTo("") != 0){
      analyzer = new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", true, false, true);
      builder = new QueryBuilder(analyzer);
      consultas.add(builder.createPhraseQuery("abstract", campos.get(4)));
      }
    if(campos.get(5).compareTo("") != 0){
      analyzer = new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", true, true, true);
      builder = new QueryBuilder(analyzer);
      consultas.add(builder.createPhraseQuery("texto", campos.get(5)));
    }
    BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();
    for(int i = 0; i < consultas.size(); ++i){
      bqbuilder.add(new BooleanClause(consultas.get(i), BooleanClause.Occur.MUST));
    }

    query = bqbuilder.build();
    //Almacenamos los documentos resultados de la búsqueda asociada a la consulta en un colector
    docs = searcher.search(query, DOCUMENTOS);

    long totalHits = docs.totalHits.value;
    System.out.print("\nSe han encontrado " + totalHits + " documento/s en total.");

    System.out.print(" ¿Desea mostrar las facetas? y/n: ");
    String res =  in.readLine();
    if(res.equals("y"))
        MostrarFacetas(query);
    return docs;
  }

  public void MostrarFacetas(Query query){
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in , StandardCharsets.UTF_8));
      String [] vector_facetas =  new String[4*TOP];
      Map<String, String> map_facetas = new HashMap<String, String>();

      int i=0;

      try{
          FacetsCollector fc1 = new FacetsCollector();
          TopDocs tdc = FacetsCollector.search(searcher, query, 10, fc1);

          Facets fcCount = new FastTaxonomyFacetCounts(taxoReader, fconfig, fc1);
          List<FacetResult> allDims = fcCount.getAllDims(100);

          System.out.println("\nCategorias totales " + allDims.size()+ " \nMostrando las " + TOP + " (máx) más relevantes de cada una...");

          //Para cada categoria mostramos el valor de la etiqueta y su numero de ocurrencias
          for( FacetResult fr : allDims ){
              System.out.println("\nCategoria: " + fr.dim);
              int cont=0;
              //Almacenamos cada etiqueta en un vector de 3*TOP casillas para guardar todas las que mostramos
              for(LabelAndValue lv : fr.labelValues){
                  if(cont < TOP){
                      vector_facetas[i]=new String(fr.dim+ " (#n)-> "+ lv.label + "");
                      map_facetas.put(lv.label, fr.dim);
                      System.out.println(lv.label + " (#n)-> "+ lv.value);
                  }else
                      break;
                  cont++;
                  i++;
              }
          }

        System.out.print("\n¿Quieres filtrar por facetas? y/n: ");
          String res =  in.readLine();

          if(res.equals("y")){
              tdc = FiltrarPorFacetas(query, tdc, vector_facetas, map_facetas);
              mostrarDocs(tdc);
              mostrarTodo = false;
          }

      }catch(IOException e){
        System.out.println("Error al mostrar facetas. ");
      }


  }

  public TopDocs FiltrarPorFacetas(Query query, TopDocs td2, String [] vector_facetas, Map<String, String> map_facetas){
      //inicializamos el DrillDownQuery con la consulta realizada
      DrillDownQuery ddq = new DrillDownQuery(fconfig, query);
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in , StandardCharsets.UTF_8));

      try{

          System.out.println("\n\nFiltramos query( " + ddq.toString()+ " ) a la que aplicaremos DrillDownQuery");
          System.out.println("Total hits = "+ td2.totalHits);
          System.out.println("\nFiltrar por: ");

          for(int i=0 ; i < TOP*4; i++){
            if(vector_facetas[i]!= null)
              System.out.println("\n(" + i +")" + " " + vector_facetas[i]);
          }

          System.out.print("\nIntroduzca los filtros: ");
          String entrada_teclado =  in.readLine();
          String[] filtros = entrada_teclado.split(" ");
          int[] faceta_n = new int[filtros.length];

          for(int i=0; i < faceta_n.length; i++){
              faceta_n[i]= Integer.parseInt(filtros[i]);
              //Busca la primera ocurrencia de ">"
              int ultpos = vector_facetas[faceta_n[i]].indexOf(">");
              String faceta = vector_facetas[faceta_n[i]].substring(ultpos+1, vector_facetas[faceta_n[i]].length());
              faceta = faceta.trim();
              //Realizamos operación AND entre cada dimensión
              ddq.add(map_facetas.get(faceta), faceta);
          }

          System.out.println("\n\nNueva búsqueda  ( " + ddq.toString() + " )");

          //volvemos a hacer el search con el nuevo ddq que contiene las facetas.
           FacetsCollector fc1 = new FacetsCollector();
           td2 = FacetsCollector.search(searcher, ddq, 10, fc1);

           System.out.println("\nCoincidencias totales = " + td2.totalHits);

       }catch(IOException e){
          System.out.println("Error al filtrar facetas. ");
       }

       return td2;
   }

  public TopDocs ConsultaTamanio(boolean rango) throws IOException{
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in , StandardCharsets.UTF_8));
      String line1, line2;
      Integer tam1, tam2;
      tam1 = 0;
      tam2 = 0;

      if(rango){
        do{
          System.out.print("\nIntroduzca el mín del rango: ");
          line1 = in.readLine();
        }while(line1 == null || line1.length() <= 0);

        do{
          System.out.print("\nIntroduzca el máx del rango: ");
          line2 = in.readLine();
        }while(line2 == null || line2.length() <= 0);
      }

      else{
        do{
          System.out.print("\nIntroduzca el tamaño exacto: ");
          line1 = in.readLine();
        }while(line1 == null || line1.length() <= 0);
        line2 = line1;
      }

      line1 = line1.trim();
      line2 = line2.trim();

      Query query = null ;
      TopDocs docs = null;

      try{
        tam1 = Integer.parseInt(line1);
        tam2 = Integer.parseInt(line2);
        query = IntPoint.newRangeQuery("tamanio", tam1, tam2) ;
        docs = searcher.search(query, DOCUMENTOS);
      }catch(IOException e){
        System.out.println("Error al procesar consulta. ");
      }

      long totalHits = docs.totalHits.value;
      System.out.print("\nSe han encontrado " + totalHits + " documento/s en total.");

      System.out.print(" ¿Desea mostrar las facetas? y/n: ");
      String res =  in.readLine();
      if(res.equals("y"))
          MostrarFacetas(query);
      return docs;
  }


  public void mostrarDocs(TopDocs results) throws IOException{
      ScoreDoc[] hits = results.scoreDocs;

      for(int j=0; j < hits.length; j++){
          Document doc = searcher.doc(hits[j].doc);
          String id = doc.get("id");
          Integer tam = doc.getField("tamanio").numericValue().intValue();
          String titulo = doc.get("titulo");
          String resumen  = doc.get("abstract");
          String pais = doc.get("paises");
          String autor = doc.get("autores");
          String institucion = doc.get("instituciones");

          if(resumen.length()>170){
            resumen = resumen.substring(0, 170);
            resumen += " [...]";
          }
          System.out.println("---------------------");
          System.out.println("Nombre fichero: " + id);
          System.out.println("Tamaño: " + tam);
          System.out.println("Título: " + titulo);
          System.out.println("Autores: " + autor);
          System.out.println("Paises: " + pais);
          System.out.println("Instituciones: " + institucion);
          System.out.println("Abstract: " + resumen);
          System.out.println("---------------------\n");
      }
  }

  public static void main (String[] args) throws Exception{
    Similarity similarity = new ClassicSimilarity();

    Busqueda b = new Busqueda();

    b.indexSearch(similarity);
  }

  public void indexSearch(Similarity similarity)throws IOException{
    try{
      //Asignamos al IndexReader el directorio donde se encuentra el índice.
      indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_PATH)));
      //Asignamos el directorio donde se encuentran las facetas.
      taxoReader = new DirectoryTaxonomyReader(FSDirectory.open(Paths.get(FACET_PATH)));
      //Creamos el IndexSearcher a partir del IndexReader
      searcher = new IndexSearcher(indexReader);
      fconfig = new FacetsConfig();
      fc = new FacetsCollector();
      //Asignamos como se calcula la similitud entre documentos
      searcher.setSimilarity(similarity);

      while(true){
        //Creo la entrada y la redirijo hacia un buffer
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        String opcion;
        Query query = null;

        do{
          System.out.println("\nElija la opcion que prefiera: ");
          System.out.println("\n1: Consulta generica.");
          System.out.println("2: Consulta por formulario.");
          System.out.println("3: Salir.");
          System.out.print("\nOpcion: ");

          opcion = in.readLine();
        } while(opcion.compareTo("1")!=0 && opcion.compareTo("2")!=0 && opcion.compareTo("3")!=0);

        if(opcion.compareTo("1") == 0){
          do{
            System.out.println("\n¿Cómo desea realizar la consulta genérica?");
            System.out.println("\n1: Sobre el texto.");
            System.out.println("2: Especificar campo/s.");
            System.out.println("3: Salir.");
            System.out.print("\nOpcion: ");
            opcion = in.readLine();
          } while(opcion.compareTo("1")!=0 && opcion.compareTo("2")!=0 && opcion.compareTo("3")!=0);

          //Consulta genérica sobre todos los campos
          if(opcion.compareTo("1") == 0)
            results = ConsultaGenerica("texto", new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", true, true, true));

          //Consulta genérica sobre campo específico
          else if(opcion.compareTo("2") == 0){
            String line =  new String();
            System.out.println("Campos disponibles: ( titulo / autores / paises / instituciones / tamanio )");
            System.out.print("\nIntroduzca el campo: ");
            opcion = in.readLine();

            if(opcion.equals("titulo") )
              results = ConsultaGenerica("titulo", new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", true, false, true));

            else if(opcion.equals("autores"))
              results = ConsultaGenerica("autores", new MiAnalizador(null, null, true, false, false));

            else if(opcion.equals("paises"))
              results = ConsultaGenerica("paises", new MiAnalizador(null, null, false, false, true));

            else if(opcion.equals("instituciones"))
              results = ConsultaGenerica("instituciones", new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", false, false, true));

            else if(opcion.equals("tamanio")){
              do{
                System.out.println("\n¿Cómo desea realizar la consulta?");
                System.out.println("\n1: Tamaño exacto.");
                System.out.println("2: Rango de tamaños.");
                System.out.println("3: Salir.");
                System.out.print("\nOpcion: ");
                opcion = in.readLine();
              } while(opcion.compareTo("1")!=0 && opcion.compareTo("2")!=0 && opcion.compareTo("3")!=0);

		          if(opcion.compareTo("1") == 0){
		              results = ConsultaTamanio(false);
              }
              else if(opcion.compareTo("2") == 0){
                  results = ConsultaTamanio(true);
              }
              else if(opcion.compareTo("3") == 0)
                break;
            }

            else{
              System.out.println("El campo especificado no está en la lista.");
              break;
            }
          }

          else if(opcion.compareTo("3") == 0)
            break;

        }

        else if(opcion.compareTo("2") == 0){
          results = ConsultaBooleana();

        }
        else if(opcion.compareTo("3") == 0)
          break;

        long totalHits = results.totalHits.value;

        //Mostramos los resultados
        if(mostrarTodo){
          System.out.print("\n¿Desea mostrar los " + totalHits + " resultado/s obtenidos? y/n:  ");
          String res =  in.readLine();
          if(res.equals("y"))
            mostrarDocs(results);
        }

      }
      indexReader.close();

    }catch(IOException e){
      try{
        indexReader.close();
      } catch (IOException e1){
        e1.printStackTrace();
      }
      e.printStackTrace();
    }

  }

}
