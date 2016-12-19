package de.ovgu.softwareprojektapp;


import android.content.Context;
import android.util.AttributeSet;
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

public class LayoutParser extends LinearLayout {

    /**
     * The network client organises all our communication with the server
     */
    private NetworkClient mNetworkClient = null;


    public LayoutParser(Context context) {
        super(context);
    }

    public LayoutParser(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LayoutParser(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * initialise NetworkClient
     *
     * @param networkClient
     */
    public void setNetworkClient(NetworkClient networkClient) {
        mNetworkClient = networkClient;
    }

    /**
     * parse Nodes from a given XMLlayoutString
     *
     * @param xmlString given XMLlayout as String
     * @param linlay    linear layout to put the buttons in
     * @throws InvalidLayoutException triggered if xml is invalid
     */
    public void createFromXML(String xmlString, LinearLayout linlay) throws InvalidLayoutException {
        removeAllViews();
        try {
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream xmlStream = new ByteArrayInputStream(xmlString.getBytes("utf-8"));
            Document doc = parser.parse(xmlStream);
            org.w3c.dom.Element root = doc.getDocumentElement();
            root.normalize();

            NodeList nList = root.getChildNodes();

            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    org.w3c.dom.Element nodeElement = (Element) node;
                    if (nodeElement.getTagName().equals("LinearLayout")) {
                        createFromXML(nodeElement, new LinearLayout(linlay.getContext()));
                    } else if (nodeElement.getTagName().equals("Button")) {
                        addButtons(nodeElement, linlay);
                    }
                }
            }
        } catch (SAXException | IOException e) {
            throw new InvalidLayoutException("Invalid XML encountered");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * create Buttons in a given Context from a buttonsmap
     *
     * @param context given Context
     * @param buttons map with Buttons and IDs
     */
    public void createFromMap(Context context, Map<Integer, String> buttons) {
        removeAllViews();

        for (Map.Entry<Integer, String> button : buttons.entrySet()) {
            Button btn = new Button(context);
            LinearLayout.LayoutParams lp = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
            lp.weight = 1;
            btn.setLayoutParams(lp);
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

    /**
     * create button from parsed node
     *
     * @param node   parsed node
     * @param linlay layout to put button in
     * @throws InvalidLayoutException if button has no ID
     */
    private void addButtons(Node node, LinearLayout linlay) throws InvalidLayoutException {
        //get node attributes
        Node weightNode = node.getAttributes().getNamedItem("android:layout_weight");
        Node textNode = node.getAttributes().getNamedItem("android:text");
        Node idNode = node.getAttributes().getNamedItem("android:id");

        Button newButton = new Button(linlay.getContext());

        newButton.setOnTouchListener(new OnTouchListener() { //set actions on touch
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    mNetworkClient.sendCommand(new ButtonClick(view.getId(), true));
                    return true;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    mNetworkClient.sendCommand(new ButtonClick(view.getId(), false));
                    return true;
                }
                return false;
            }
        });

        if (idNode == null || idNode.getNodeValue() == null)
            throw new InvalidLayoutException("Button without ID");

        newButton.setId(Integer.parseInt(idNode.getNodeValue()));

        if (textNode == null)
            newButton.setText("Button " + Integer.parseInt(idNode.getNodeValue()));
        else
            newButton.setText(textNode.getNodeValue());

        if (weightNode != null) {
            LayoutParams layoutParams = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
            layoutParams.weight = Float.parseFloat(weightNode.getNodeValue());
            newButton.setLayoutParams(layoutParams);
        }
        linlay.addView(newButton);
    }

    /**
     * is called when a linear layout is nested in the xmllayoutfile
     *
     * @param node   given nested node
     * @param linlay layout to create buttons in
     * @throws InvalidLayoutException
     */
    private void createFromXML(Node node, LinearLayout linlay) throws InvalidLayoutException {
        org.w3c.dom.Element root = (Element) node;
        root.normalize();

        NodeList nList = root.getChildNodes();

        for (int i = 0; i < nList.getLength(); i++) {
            Node item = nList.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element nodeElement = (Element) item;
                if (nodeElement.getTagName().equals("LinearLayout")) {
                    createFromXML(nodeElement, new LinearLayout(linlay.getContext()));
                } else if (nodeElement.getTagName().equals("Button")) {
                    addButtons(nodeElement, linlay);
                }
            }
        }
        addView(linlay);
    }
}
