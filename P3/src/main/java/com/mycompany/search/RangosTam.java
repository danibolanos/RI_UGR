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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.File;
import java.util.*;

import java.util.stream.Collectors;

public class RangosTam {

  private List<String> lista = null;
  private List<Integer> maximos = null;

  //Declaración de constructor
  RangosTam(Integer top, Integer max) {
    List<String> aux = new ArrayList<String>();
    List<Integer> maxim = new ArrayList<Integer>();
    Integer intervalo = max/top;
    Integer inicio = 0;
    for(int i=0; i < top; i++){
      aux.add(Integer.toString(inicio) + "-" + Integer.toString(inicio+intervalo));
      inicio += intervalo;
      maxim.add(inicio);
    }
    lista = aux;
    maximos = maxim;
  }

  //Devuelve las listas de String
  public List<String> getListaIntervalos(){
    return lista;
  }

  public List<Integer> getListaMaximos(){
    return maximos;
  }

  public String getIndexIntervalos(Integer num){
    Integer index = 0;
    boolean para = false;
    for(int i=0; i < maximos.size() && !para; i++){
      if(maximos.get(i) > num){
          index = i;
          para = true;
      }
    }
    return lista.get(index);
  }


}
