/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.search;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.QueryBuilder;

import javax.swing.DefaultListModel;

/**
 *
 * @author fernando
 */
public class MostrarResultados extends javax.swing.JFrame {

    /**
     * Creates new form MostrarResultados
     */
    public MostrarResultados(String consulta, String campo) throws IOException {
        initComponents();
        ArrayList<javax.swing.JComboBox<String>> desplegables= new ArrayList<javax.swing.JComboBox<String>>();
        Similarity similarity = new ClassicSimilarity();
        ArrayList<String> categorias;
        b = new busqueda();
        b.indexSearch(similarity);

        if(campo.equals("Título") )
              docsBusqueda = b.ConsultaGenerica("titulo", consulta, new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", true, false, true));

            else if(campo.equals("Autores"))
              docsBusqueda = b.ConsultaGenerica("autores", consulta, new MiAnalizador(null, null, true, false, false));

            else if(campo.equals("Paises"))
              docsBusqueda = b.ConsultaGenerica("paises", consulta, new MiAnalizador(null, null, false, false, true));

            else if(campo.equals("Instituciones"))
              docsBusqueda = b.ConsultaGenerica("instituciones", consulta, new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", false, false, true));
            
            else if(campo.equals("Texto"))
              docsBusqueda = b.ConsultaGenerica("texto", consulta, new MiAnalizador(EnglishAnalyzer.getDefaultStopSet(), "English", true, true, true));
            
            else if(campo.equals("Tamaño")){
                String[] rango = consulta.split("-");
                docsBusqueda = b.ConsultaTamanio(rango[0], rango[1]);
            }
        
        q = b.getGeneralQuery();
        System.out.println("Numero de docs" + docsBusqueda.totalHits);
        
        imprimirResultados(docsBusqueda);
        facetas = b.MostrarFacetas(q);
        categorias = b.getCategorias();
        
        if(categorias.size()>0){
            this.jLabel2.setText(categorias.get(0));
            this.jLabel3.setText(categorias.get(1));
            this.jLabel4.setText(categorias.get(2));
            this.jLabel5.setText(categorias.get(3));
        }
        
        desplegables.add(this.jComboBox1);
        desplegables.add(this.jComboBox2);
        desplegables.add(this.jComboBox3);
        desplegables.add(this.jComboBox4);
        
        
        for(int i = 0; i < desplegables.size(); ++i){
            desplegables.get(i).removeAllItems();
            desplegables.get(i).addItem("Todos");
            if(facetas.size()>0)
                for(int j = 0; j < facetas.get(i).size(); ++j){
                    desplegables.get(i).addItem(facetas.get(i).get(j));
                }
        }
        
        
        

        
            
    }
    
    public MostrarResultados(ArrayList<String> l, boolean restrictivo) throws IOException {
        initComponents();
        ArrayList<String> salida;
        DefaultListModel model = new DefaultListModel<>();
        ArrayList<javax.swing.JComboBox<String>> desplegables= new ArrayList<javax.swing.JComboBox<String>>();
        Similarity similarity = new ClassicSimilarity();
        ArrayList<String> categorias;
        b = new busqueda();
        b.indexSearch(similarity);
        
        q = b.queryBooleana(l, restrictivo);
        
        docsBusqueda = b.ConsultaBooleana(q);
        
        imprimirResultados(docsBusqueda);
        
        facetas = b.MostrarFacetas(q);
        categorias = b.getCategorias();
        
        if(categorias.size()>0){
            this.jLabel2.setText(categorias.get(0));
            this.jLabel3.setText(categorias.get(1));
            this.jLabel4.setText(categorias.get(2));
            this.jLabel5.setText(categorias.get(3));
        }
        
        
        desplegables.add(this.jComboBox1);
        desplegables.add(this.jComboBox2);
        desplegables.add(this.jComboBox3);
        desplegables.add(this.jComboBox4);
        
        
        for(int i = 0; i < desplegables.size(); ++i){
            desplegables.get(i).removeAllItems();
            desplegables.get(i).addItem("Todos");
            if(facetas.size()>0)
                for(int j = 0; j < facetas.get(i).size(); ++j){
                    desplegables.get(i).addItem(facetas.get(i).get(j));
                }
        }
        
        
        
        
     
     
    }
    
    public void imprimirResultados(TopDocs docs) throws IOException{
        ArrayList<String> salida = new ArrayList<String>();
        salida = b.mostrarDocs(docs);
        String total = "";
         for(int i = 0 ; i < salida.size();++i){
             total += salida.get(i);
         }
         
        this.jLabel1.setText("Resultados: "+ docs.totalHits);
        this.jTextArea1.setText(total);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox3 = new javax.swing.JComboBox<>();
        jComboBox4 = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Resultados:");

        jLabel2.setText("Autor");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox3ActionPerformed(evt);
            }
        });

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox4ActionPerformed(evt);
            }
        });

        jButton1.setText("Finalizar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel3.setText("Institución");

        jLabel4.setText("Tamaño");

        jLabel5.setText("País");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jButton2.setText("Nueva búqueda");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel2)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jComboBox4, 0, 213, Short.MAX_VALUE)
                        .addComponent(jComboBox3, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel5)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 87, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 791, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(85, 85, 85))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(109, 109, 109)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 576, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton2)
                            .addComponent(jButton1)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(105, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        try {
            // TODO add your handling code here:
            actualizarBusqueda();
        } catch (IOException ex) {
            Logger.getLogger(MostrarResultados.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_jComboBox1ActionPerformed
    
    public void actualizarBusqueda() throws IOException{
        String seleccion = recogerSeleccion();
        TopDocs docs;
        
        if(seleccion.equals("")){
            imprimirResultados(docsBusqueda);
        }
        else{
            docs = b.FiltrarPorFacetas(q, docsBusqueda, seleccion);
            imprimirResultados(docs);
        }
        
        
    }
    
    public String recogerSeleccion(){
        ArrayList<javax.swing.JComboBox<String>> desplegables= new ArrayList<javax.swing.JComboBox<String>>();
        ArrayList<Integer> seleccion = new ArrayList<Integer>();
        String salida= "";
        desplegables.add(this.jComboBox1);
        desplegables.add(this.jComboBox2);
        desplegables.add(this.jComboBox3);
        desplegables.add(this.jComboBox4);
        
        int sum = 0;
        if(facetas.size()>0)
            for(int i = 0; i < desplegables.size(); ++i){
                boolean encontrado = false;

                String s = (String)desplegables.get(i).getSelectedItem();
                for(int j = 0; j < facetas.get(i).size() && !encontrado; ++j){
                    if(facetas.get(i).get(j).equals(s)){
                       seleccion.add(sum + j);
                       encontrado = true;
                    }
                }
                sum += facetas.get(i).size();
            }
        
        for(int i = 0; i <seleccion.size(); ++i){
            salida += seleccion.get(i).toString() + " ";
        }

        return salida;
    }
    
    
    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
        try {
            // TODO add your handling code here:
            actualizarBusqueda();
        } catch (IOException ex) {
            Logger.getLogger(MostrarResultados.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jComboBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox3ActionPerformed
        // TODO add your handling code here:
        try {
            // TODO add your handling code here:
            actualizarBusqueda();
        } catch (IOException ex) {
            Logger.getLogger(MostrarResultados.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jComboBox3ActionPerformed

    private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
        // TODO add your handling code here:
        try {
            // TODO add your handling code here:
            actualizarBusqueda();
        } catch (IOException ex) {
            Logger.getLogger(MostrarResultados.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jComboBox4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            // TODO add your handling code here:
            b.closeindexReader();
        } catch (IOException ex) {
            Logger.getLogger(MostrarResultados.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        Pventana v = new Pventana();
        v.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    public void setBusquedaGeneral(String s){
        busquedaG = s;
    }
    
    public void setBusquedaEspecifica(ArrayList<String> l){
        busquedaE = l;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

    String busquedaG = null;
    ArrayList<String> busquedaE = null;
    busqueda b;
    ArrayList<ArrayList<String>> facetas;
    Query q = null;
    TopDocs docsBusqueda = null;
    
    

}
