package dev.nasuta.tmc.lab3;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.*;

class OSMParser extends DefaultHandler {
    private Map<Long, NodeData> nodesMap = new HashMap<>();
    private List<WayData> waysList = new ArrayList<>();
    
    private long currentWayId = 0;
    private List<Long> currentWayNodes = new ArrayList<>();
    private Map<String, String> currentWayTags = new HashMap<>();
    
    private long currentNodeId = 0;
    private double currentNodeLat = 0;
    private double currentNodeLon = 0;
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("node".equals(qName)) {
            currentNodeId = Long.parseLong(attributes.getValue("id"));
            currentNodeLat = Double.parseDouble(attributes.getValue("lat"));
            currentNodeLon = Double.parseDouble(attributes.getValue("lon"));
        } else if ("way".equals(qName)) {
            currentWayId = Long.parseLong(attributes.getValue("id"));
            currentWayNodes.clear();
            currentWayTags.clear();
        } else if ("nd".equals(qName)) {
            currentWayNodes.add(Long.parseLong(attributes.getValue("ref")));
        } else if ("tag".equals(qName)) {
            String k = attributes.getValue("k");
            String v = attributes.getValue("v");
            currentWayTags.put(k, v);
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("node".equals(qName)) {
            nodesMap.put(currentNodeId, new NodeData(currentNodeId, currentNodeLat, currentNodeLon));
        } else if ("way".equals(qName)) {
            waysList.add(new WayData(currentWayId, new ArrayList<>(currentWayNodes), new HashMap<>(currentWayTags)));
        }
    }
    
    public void parse(InputStream inputStream) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(inputStream, this);
    }
    
    public List<NodeData> getNodes() {
        return new ArrayList<>(nodesMap.values());
    }
    
    public List<WayData> getWays() {
        return waysList;
    }
    
    public Map<Long, NodeData> getNodesMap() {
        return nodesMap;
    }
}