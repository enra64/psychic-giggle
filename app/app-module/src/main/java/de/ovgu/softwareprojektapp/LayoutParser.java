package de.ovgu.softwareprojektapp;


import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojektapp.networking.NetworkClient;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.LinearLayout.LayoutParams.WRAP_CONTENT;

public class LayoutParser extends LinearLayout {
    private DocumentBuilder parser;
    private final NetworkClient mNetworkClient;

    public LayoutParser(Context context, NetworkClient networkClient) throws ParserConfigurationException {
        super(context);
        parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        mNetworkClient = networkClient;
    }

    public void createFromXML(String xmlString, LinearLayout linlay) throws InvalidLayoutException {
        try {
            InputStream xmlStream = new ByteArrayInputStream(xmlString.getBytes("utf-8"));
            Document doc = parser.parse(xmlStream);

            org.w3c.dom.Element root = doc.getDocumentElement();
            root.normalize();

            NodeList nList = root.getChildNodes();

            for (int i = 0 ; i < nList.getLength(); i++){
                Node node = nList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    org.w3c.dom.Element nodeElement = (Element) node;
                    if(nodeElement.getTagName().equals("LinearLayout")){
                        createFromXML(nodeElement.toString(), new LinearLayout(linlay.getContext()));
                    } else if (nodeElement.getTagName().equals("Button")){
                        addButtons(nodeElement, linlay);
                    }
                }
            }
        } catch (SAXException | IOException e) {
            throw new InvalidLayoutException("Invalid XML encountered");
        }
    }

    public void createFromMap(Context context, Map<Integer, String> buttons){
        removeAllViews();

        for (Map.Entry<Integer, String> button : buttons.entrySet()) {
            Button btn = new Button(context);
            btn.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
            addView(btn);
            btn.setText(button.getValue());
            btn.setTag(button.getKey());
            btn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        mNetworkClient.sendCommand(new ButtonClick((Integer) view.getTag(), true));
                    } else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                        mNetworkClient.sendCommand(new ButtonClick((Integer) view.getTag(), false));
                    return true;
                }
            });
        }
    }

    private void addButtons(Node node, LinearLayout linlay) throws InvalidLayoutException {
        Node weightNode = node.getAttributes().getNamedItem("layout_weight");
        Node textNode = node.getAttributes().getNamedItem("text");
        Node idNode = node.getAttributes().getNamedItem("id");
        Button newButton = new Button(linlay.getContext());
        if(idNode != null && idNode.getNodeValue() != null)
            throw new InvalidLayoutException("Button without ID");

        newButton.setId(Integer.parseInt(idNode.getNodeValue()));

        if(textNode == null)
            newButton.setText("Button " + Integer.parseInt(idNode.getNodeValue()));
        else
            newButton.setText(textNode.getNodeValue());

        if(weightNode != null){
            LayoutParams layoutParams  = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            layoutParams.weight = Float.parseFloat(weightNode.getNodeValue());
        }
        linlay.addView(newButton);
    }
}
