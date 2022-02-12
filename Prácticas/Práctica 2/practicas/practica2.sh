#!/bin/bash

if [ $# != 1 ]
then 
	echo "El uso correcto es ./practica2.sh -opcion"
else
	if [ $1 -eq "-1" ]
	then
		javac -cp ./tika-app-1.23.jar:./lucene-8.6.2/core/lucene-core-8.6.2.jar:./lucene-8.6.2/analysis/common/lucene-analyzers-common-8.6.2.jar ./src/EJ1Lucene.java
		java -cp ./tika-app-1.23.jar:./lucene-8.6.2/core/lucene-core-8.6.2.jar:./src/:./lucene-8.6.2/analysis/common/lucene-analyzers-common-8.6.2.jar EJ1Lucene ./docs

	elif [ $1 -eq "-2" ]
	then
		javac -cp ./lucene-8.6.2/core/lucene-core-8.6.2.jar:./lucene-8.6.2/analysis/common/lucene-analyzers-common-8.6.2.jar ./src/EJ2Lucene.java
		java -cp ./lucene-8.6.2/core/lucene-core-8.6.2.jar:./src/:./lucene-8.6.2/analysis/common/lucene-analyzers-common-8.6.2.jar EJ2Lucene

	elif [ $1 -eq "-3" ]
	then
		javac -cp ./tika-app-1.23.jar:./lucene-8.6.2/core/lucene-core-8.6.2.jar:./lucene-8.6.2/analysis/common/lucene-analyzers-common-8.6.2.jar ./src/EJ3Lucene.java
		java -cp ./tika-app-1.23.jar:./src/:./lucene-8.6.2/core/lucene-core-8.6.2.jar:.:./lucene-8.6.2/analysis/common/lucene-analyzers-common-8.6.2.jar EJ3Lucene ./docs

	elif [ $1 -eq "-4" ]
	then
		javac -cp ./tika-app-1.23.jar:./lucene-8.6.2/core/lucene-core-8.6.2.jar:./lucene-8.6.2/analysis/common/lucene-analyzers-common-8.6.2.jar ./src/EJ4Lucene.java ./src/ultimas4Letras.java
		java -cp ./tika-app-1.23.jar:./lucene-8.6.2/core/lucene-core-8.6.2.jar:./src/:./lucene-8.6.2/analysis/common/lucene-analyzers-common-8.6.2.jar EJ4Lucene ./docs

	else
		echo "No es una opción válida, debe ser [-1,-2,-3,-4]."
	fi
fi
