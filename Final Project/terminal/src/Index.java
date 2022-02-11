import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.*;
import java.nio.file.Paths;


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
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.search.IndexSearcher;




public class Index {
	//Directorio de los archivos json completo
	String JSON_FILE_PATH;
	String INDEX_PATH = "./index";
	String FACET_PATH = "./facet";
	IndexWriter writer;
	DirectoryTaxonomyWriter taxoWriter;
	FacetsConfig fconfig;


	Index(String jsonFile){

		JSON_FILE_PATH = jsonFile;

	}

	public static void main (String[] args) throws Exception{
		EnglishAnalyzer english = new EnglishAnalyzer();
		CharArraySet englishWords = english.getDefaultStopSet();
		if(englishWords.contains("-")){
			System.out.println("EL GUION ESTA");
			System.exit(0);
		}
		Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
		//analyzerPerField.put("ID", new WhitespaceAnalyzer());
		analyzerPerField.put("titulo", new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", true, false, true));
		analyzerPerField.put("autores", new MiAnalizador(null, null, true, false, false));
		analyzerPerField.put("paises", new MiAnalizador(null, null, false, false, true));
		analyzerPerField.put("instituciones", new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", false, false, true));
		analyzerPerField.put("abstract", new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", true, false, true));
		analyzerPerField.put("texto", new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", true, true, true));


		PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(), analyzerPerField);


		//Medida de Similitud (modelo de recuperación) por defecto BM25
		Similarity similarity = new ClassicSimilarity();

		//Llamamos al constructor con los parámetros
		Index index = new Index("./pdf_json/");


		try{
			index.configurarIndice(analyzer, similarity);
		} catch (IOException e){
			System.out.println("Error configuring the index");
		}


		try{
			index.indexarDocumentos();
		} catch (IOException e){
			System.out.println("Error indexing documents");
		}

		index.close();

	}

	public void configurarIndice(Analyzer analyzer, Similarity similarity) throws IOException{
		FSDirectory dir = FSDirectory.open(Paths.get(INDEX_PATH));

		//Creamos la configuración del IndexWriter
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		config.setSimilarity(similarity);
		FSDirectory taxoDir = FSDirectory.open(Paths.get(FACET_PATH));

		fconfig = new FacetsConfig();
		writer = new IndexWriter(dir, config);
		taxoWriter = new DirectoryTaxonomyWriter(taxoDir);
	}

	public void indexarDocumentos()throws IOException{
		File directorio = new File(JSON_FILE_PATH);
    String[] archivos = directorio.list();
		RangosTam rangos = new RangosTam(5, 4000);

		Integer TOTAL = archivos.length;
		Integer articulos = 0;

    for(String file : archivos){
      File f = new File(JSON_FILE_PATH + file);
      JsonReader j = new JsonReader(f);

			articulos+=1;

			//Porcentaje de indexación
			if(articulos==1){
				System.out.println("Creando índice... 0%");
			}if(articulos==TOTAL/4){
				System.out.println("Creando índice... 25%");
			}if(articulos==TOTAL/2){
				System.out.println("Creando índice... 50%");
			}if(articulos==3*TOTAL/4){
				System.out.println("Creando índice... 75%");
			}if(articulos>=TOTAL){
				System.out.println("Creando índice... 100%");
			}

			fconfig.setMultiValued("autores", true);
			fconfig.setMultiValued("paises", true);
			fconfig.setMultiValued("instituciones", true);

			//Incluimos los campos de indexación
      			Document doc = new Document();
			doc.add(new StringField("id", j.getArchivo(), Field.Store.YES));
			doc.add(new IntPoint("tamanio", j.getTamanio()));
			doc.add(new StoredField("tamanio", j.getTamanio()));
			doc.add(new TextField("titulo", j.getTitulo(), Field.Store.YES));
			doc.add(new TextField("autores", j.getAutores(), Field.Store.YES));
			doc.add(new TextField("paises", j.getPaises(), Field.Store.YES));
			doc.add(new TextField("instituciones", j.getInstituciones(), Field.Store.YES));
			doc.add(new TextField("abstract", j.getAbstract(), Field.Store.YES));
			doc.add(new TextField("texto", j.getTexto(), Field.Store.NO));

			//Incluimos las facetas
			for (String autor :  j.getListaAutores())
				doc.add(new FacetField("autores", autor));

			for (String insti :  j.getListaInstituciones())
				doc.add(new FacetField("instituciones", insti));

			for (String pais :  j.getListaPaises())
				doc.add(new FacetField("paises", pais));

			//Incluimos las facetas para los rangos de tamaño
			doc.add(new FacetField("tamanio", rangos.getIndexIntervalos(j.getTamanio())));

			//writer.addDocument(doc);
			writer.addDocument(fconfig.build(taxoWriter,doc));
    }

	}


	public void close(){
		try{
			writer.commit();
			writer.close();
			taxoWriter.close();
		} catch (IOException e){
			System.out.println("Error closing the index");
		}
	}


}
