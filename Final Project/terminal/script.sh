#!/bin/bash

if [ $# != 1 ]
then 
	echo "El uso correcto es ./practica2.sh opcion [donde las opciones son (-i, -b, --help)]"
else

	if [ $1 != "--help" ]
	then

	javac -cp ./library/json-simple-1.1.1.jar:./library/lucene-8.6.2/core/lucene-core-8.6.2.jar:./library/lucene-8.6.2/analysis/common/lucene-analyzers-common-8.6.2.jar:./library/lucene-8.6.2/facet/lucene-facet-8.6.2.jar:./library/lucene-8.6.2/queryparser/lucene-queryparser-8.6.2.jar:./library/hppc-0.8.2.jar ./src/JsonReader.java ./src/Index.java ./src/Busqueda.java ./src/AniosFilter.java ./src/MiAnalizador.java ./src/RangosTam.java
	
	fi
	
	
	if [ $1 == "-i" ]
	then

	java -cp ./library/json-simple-1.1.1.jar:./library/lucene-8.6.2/core/lucene-core-8.6.2.jar:./src/:./library/lucene-8.6.2/analysis/common/lucene-analyzers-common-8.6.2.jar:./library/lucene-8.6.2/facet/lucene-facet-8.6.2.jar Index
	
	elif [ $1 == "-b" ]
	then
	
	java -cp ./library/json-simple-1.1.1.jar:./library/lucene-8.6.2/core/lucene-core-8.6.2.jar:./src/:./library/lucene-8.6.2/analysis/common/lucene-analyzers-common-8.6.2.jar:./library/lucene-8.6.2/facet/lucene-facet-8.6.2.jar:./library/lucene-8.6.2/queryparser/lucene-queryparser-8.6.2.jar:./library/hppc-0.8.2.jar Busqueda

	elif [ $1 == "--help" ]
	then

	echo "Para crear el índice use la opción -i."
	echo "Para realizar una búsqueda use la opción -b."
	echo "Los documentos a indexar deberán añadirse al directorio ./pdf_json"
	
	fi
fi
	
	

