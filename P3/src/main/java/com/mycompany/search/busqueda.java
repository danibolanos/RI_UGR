/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.search;

/**
 *
 * @author fernando
 */
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



public class busqueda{

  String INDEX_PATH = "./terminal/index";
  String FACET_PATH = "./terminal/facet";
  IndexSearcher searcher = null;
  FacetsCollector fc = null;
  FacetsConfig fconfig = null;
  int TOP = 5;
  int DOCUMENTOS = 10;
  TaxonomyReader taxoReader = null;
  DirectoryReader indexReader = null;
  TopDocs results = null;
  boolean mostrarTodo = true;
  String [] vector_facetas =  new String[4*TOP];
  Map<String, String> map_facetas = new HashMap<String, String>();
  Query gquery = null;
  long totalHits = 0;
  ArrayList<String> categorias = null;

  public TopDocs ConsultaGenerica(String campo, String consulta , Analyzer analyzer) throws IOException {
      //Creo la entrada y la redirijo hacia un buffer
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
      //El campo por defecto será texto y la consulta sera analizada con analyzer
      QueryParser parser = new QueryParser(campo, analyzer);
      Query query = null ;
      TopDocs docs = null;
      String line = consulta;

      line = line.trim();

      try{
          query = parser.parse(line);
          docs = searcher.search(query, DOCUMENTOS);

      }catch (org.apache.lucene.queryparser.classic.ParseException e){
          System.out.println("Error al procesar consulta. ");
      }

      totalHits = docs.totalHits.value;
      gquery = query;
      
      return docs;
  }
  
  public Query getGeneralQuery(){
      return gquery;
  }
  
  public Query queryBooleana(ArrayList<String> campos, boolean restrictivo){
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    Query query = null;
    ArrayList<String> tipoInfo = new ArrayList<String>();
    ArrayList<Query> consultas = new ArrayList<Query>();
    
    

    tipoInfo.add("titulo");
    tipoInfo.add("autores");
    tipoInfo.add("paises");
    tipoInfo.add("instituciones");
    tipoInfo.add("abstract");
    tipoInfo.add("texto");
    
    
    
    

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
    BooleanClause.Occur r = BooleanClause.Occur.MUST;
    if(!restrictivo)
        r = BooleanClause.Occur.SHOULD;
    
    for(int i = 0; i < consultas.size(); ++i){
      bqbuilder.add(new BooleanClause(consultas.get(i), r));
    }

    query = bqbuilder.build();
    
    return query;
  }

  public TopDocs ConsultaBooleana(Query query) throws IOException {
    TopDocs docs = null;
    
    //Almacenamos los documentos resultados de la búsqueda asociada a la consulta en un colector
    docs = searcher.search(query, DOCUMENTOS);
    
    totalHits = docs.totalHits.value;

    return docs;
  }

  public ArrayList<ArrayList<String>> MostrarFacetas(Query query){
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in , StandardCharsets.UTF_8));
      vector_facetas =  new String[4*TOP];
      map_facetas = new HashMap<String, String>();
      ArrayList<ArrayList<String>> l = new ArrayList<ArrayList<String>>();
      DrillDownQuery ddq = new DrillDownQuery(fconfig, query);
      categorias = new ArrayList<String>();
      
      
      int i=0;

      try{
          FacetsCollector fc1 = new FacetsCollector();
          TopDocs tdc = FacetsCollector.search(searcher, ddq, 10, fc1);          
          Facets fcCount = new FastTaxonomyFacetCounts(taxoReader, fconfig, fc1);        
          List<FacetResult> allDims = fcCount.getAllDims(100);


          //Para cada categoria mostramos el valor de la etiqueta y su numero de ocurrencias
          for( FacetResult fr : allDims ){
              l.add(new ArrayList<String>());
              categorias.add(fr.dim);
              int cont=0;
              //Almacenamos cada etiqueta en un vector de 3*TOP casillas para guardar todas las que mostramos
              for(LabelAndValue lv : fr.labelValues){
                  if(cont < TOP){
                      vector_facetas[i]=new String(fr.dim+ " (#n)-> "+ lv.label + "");
                      map_facetas.put(lv.label, fr.dim);
                      l.get(l.size()-1).add(lv.label + " ("+ lv.value + ")");
                  }else
                      break;
                  cont++;
                  i++;
              }
          }
      }catch(IOException e){
        System.out.println("Error al mostrar facetas. ");
      }

      return l;
  }
  
  public ArrayList<String> getCategorias(){
      return categorias;
  }

  public TopDocs FiltrarPorFacetas(Query query, TopDocs td2, String s){
      //inicializamos el DrillDownQuery con la consulta realizada
      DrillDownQuery ddq = new DrillDownQuery(fconfig, query);
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in , StandardCharsets.UTF_8));

      try{
          String entrada_teclado =  s;
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

          //volvemos a hacer el search con el nuevo ddq que contiene las facetas.
           FacetsCollector fc1 = new FacetsCollector();
           td2 = FacetsCollector.search(searcher, ddq, 10, fc1);

           totalHits = td2.totalHits.value;

       }catch(IOException e){
          System.out.println("Error al filtrar facetas. ");
       }

       return td2;
   }

  public TopDocs ConsultaTamanio(String line1, String line2) throws IOException{
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in , StandardCharsets.UTF_8));
      Integer tam1, tam2;
      tam1 = 0;
      tam2 = 0;

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

      totalHits = docs.totalHits.value;

      gquery = query;
      return docs;
  }


  public ArrayList<String> mostrarDocs(TopDocs results) throws IOException{
      ScoreDoc[] hits = results.scoreDocs;
      ArrayList<String> l = new ArrayList<String>();
      String s = "";

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
          s = "";
          s += "\n---------------------\n";
          s += "\nNombre fichero: " + id;
          s += "\nTamaño: " + tam;
          s += "\nTítulo: " + titulo;
          s += "\nAutores: " + autor;
          s += "\nPaises: " + pais;
          s += "\nInstituciones: " + institucion;
          s += "\nAbstract: " + resumen;
          s += "\n---------------------\n";
          
          l.add(s);
      }
      return l;
  }

  public long numResultados(){
      return totalHits;
  }
  
  public void closeindexReader() throws IOException{
      indexReader.close();
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
