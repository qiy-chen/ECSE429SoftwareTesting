package com.testproject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.StringReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;

public class helper {
    public static HashMap<String, Object> jsonStringToMap(String jsonstring){
        Gson gson = new GsonBuilder()
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create();
        return gson.fromJson(jsonstring, new TypeToken<HashMap<String, Object>>() {}.getType());
    }
    public static String mapToJSONString(HashMap<String, Object> map){
        Gson gson = new GsonBuilder().create();
        return gson.toJson(map);
    }
    public static int getDifferencesSizeXMLStrings(String xml_string_1, String xml_string_2) throws SAXException, IOException, ParserConfigurationException{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder document_builder = dbf.newDocumentBuilder();
        Document xml_1 = document_builder.parse(new InputSource(new StringReader(xml_string_1)));
        Document xml_2 = document_builder.parse(new InputSource(new StringReader(xml_string_2)));

        // Check if there is differences between the two XML
        //creating Diff instance to compare two XML files
        XMLUnit.setIgnoreWhitespace(true);
        Diff xmlDiff = new Diff(xml_1, xml_2);
        //for getting detailed differences between two xml files
        DetailedDiff detailXmlDiff = new DetailedDiff(xmlDiff);
        @SuppressWarnings("rawtypes")
        List differences = detailXmlDiff.getAllDifferences();
        return differences.size();
    }
    @SuppressWarnings("rawtypes")
    public static Object getFirstInstanceFromListFromProp(HashMap<String, Object> map, String propName) {
        return ((List) map.get(propName)).get(0);
    }
    public static double getMeanListDouble(LinkedList<Double> list){
        Double total = 0.0;
        for (int i = 0; i < list.size(); i++){
            total += list.get(i);
        }
        return total/list.size();
    }
    public static long getMeanListLong(LinkedList<Long> list){
        Long total = 0L;
        for (int i = 0; i < list.size(); i++){
            total += list.get(i);
        }
        return total/list.size();
    }
    public static long getSumListLong(LinkedList<Long> list){
        Long total = 0L;
        for (int i = 0; i < list.size(); i++){
            total += list.get(i);
        }
        return total;
    }
}
