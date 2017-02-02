package de.ovgu.softwareprojektapp.activities.send;


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
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojektapp.networking.NetworkClient;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * The LayoutParser class subclasses {@link LinearLayout}, and is designed to display button layouts
 * specified by the server. It can do so from a map of buttons to be displayed, and from an xml layout
 * file. This layout file should be a valid android xml layout resource file and contain only Buttons
 * and LinearLayouts.
 */
public class LayoutParser extends LinearLayout {

    /**
     * The network client organises all our communication with the server
     */
    private NetworkClient mNetworkClient = null;

    /**
     * Create a new LayoutParser instance using a direct super call
     */
    public LayoutParser(Context context) {
        super(context);
    }

    /**
     * Create a new LayoutParser instance using a direct super call
     */
    public LayoutParser(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Create a new LayoutParser instance using a direct super call
     */
    public LayoutParser(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Set the NetworkClient to be used for responding to button clicks
     */
    public void setNetworkClient(NetworkClient networkClient) {
        mNetworkClient = networkClient;
    }

    /**
     * parse Nodes from a given xml layout string. This layout string should be a valid android xml
     * layout resource file and contain only Buttons and LinearLayouts.
     *
     * @param xmlString given xml layout as String
     * @throws InvalidLayoutException if xml is invalid
     */
    public void createFromXML(String xmlString) throws InvalidLayoutException {
        // clear any previous views
        removeAllViews();

        try {
            // create a Document object from the xmlString
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream xmlStream = new ByteArrayInputStream(xmlString.getBytes("utf-8"));
            Document doc = parser.parse(xmlStream);


            // get and apply layout orientation for the this linearlayout
            Node orientationNode = doc.getDocumentElement().getAttributes().getNamedItem("android:orientation");

            // apply
            if (orientationNode != null)
                setOrientation(orientationNode.getNodeValue().equals("vertical") ? VERTICAL : HORIZONTAL);

            // parse the layout out of the root node
            createFromNode(doc.getDocumentElement(), this);
        } catch (SAXException | IOException e) {
            throw new InvalidLayoutException("Invalid XML encountered");
        } catch (ParserConfigurationException e) {
            throw new InvalidLayoutException("XML parser configuration error");
        }
    }

    /**
     * is called when a linear layout is nested in the xml layout
     *
     * @param xmlElement given nested node
     * @param parent     layout to create buttons in
     * @throws InvalidLayoutException if an error occurs while parsing the layout l
     */
    private void createFromNode(Element xmlElement, LinearLayout parent) throws InvalidLayoutException {
        xmlElement.normalize();

        NodeList nodes = xmlElement.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element item = (Element) nodes.item(i);
                String itemTag = item.getTagName();

                if (itemTag.equals("LinearLayout"))
                    createFromNode(item, createLinearLayout(item, parent));
                else if (itemTag.equals("Button"))
                    addButton(item, parent);
            }
        }

        // add the view to the parent if the current view is not the parent
        if (parent != this)
            addView(parent);

    }

    /**
     * Create a a new linear layout. Tries to find the orientation and weight of the layout.
     *
     * @param xmlLinearLayout linear layout xml element
     * @param parent          the parent linearlayout
     */
    private LinearLayout createLinearLayout(Element xmlLinearLayout, LinearLayout parent) {
        LinearLayout newLayout = new LinearLayout(parent.getContext());

        // extract weight information from xml
        Node weightNode = xmlLinearLayout.getAttributes().getNamedItem("android:layout_weight");


        // apply weight if given
        if (weightNode != null)
            newLayout.setLayoutParams(getLayoutParams(parent, Float.parseFloat(weightNode.getNodeValue())));

        // extract orientation from xml
        Node orientationNode = xmlLinearLayout.getAttributes().getNamedItem("android:orientation");
        // apply layout orientation
        if (orientationNode != null)
            newLayout.setOrientation(orientationNode.getNodeValue().equals("vertical") ? VERTICAL : HORIZONTAL);

        return newLayout;
    }

    /**
     * Get the correct layout params for view given a parent
     *
     * @param parent the parent of the view
     * @param weight weight of the element
     * @return layout params with appropriate parameters
     */
    private LinearLayout.LayoutParams getLayoutParams(LinearLayout parent, float weight) {
        if (parent.getOrientation() == VERTICAL)
            return new LinearLayout.LayoutParams(MATCH_PARENT, 0, weight);
        else
            return new LinearLayout.LayoutParams(0, MATCH_PARENT, weight);
    }

    /**
     * create button from parsed node
     *
     * @param node         parsed node
     * @param parentLayout layout to put button in
     * @throws InvalidLayoutException if button has no ID
     */
    private void addButton(Node node, LinearLayout parentLayout) throws InvalidLayoutException {
        //get node attributes
        Node weightNode = node.getAttributes().getNamedItem("android:layout_weight");
        Node textNode = node.getAttributes().getNamedItem("android:text");
        Node idNode = node.getAttributes().getNamedItem("android:id");

        if (idNode == null || idNode.getNodeValue() == null)
            throw new InvalidLayoutException("Button without ID");

        // parse if from idNode
        int buttonId = Integer.parseInt(idNode.getNodeValue());

        // choose fitting button text. prefer textNode value
        String buttonText;
        if (textNode == null)
            buttonText = "Button " + buttonId;
        else
            buttonText = textNode.getNodeValue();


        // create new button, set id and text
        Button newButton = new Button(parentLayout.getContext());
        newButton.setId(buttonId);
        newButton.setText(buttonText);

        // if a weight is specified, create and set layout params for the button
        if (weightNode != null) {
            newButton.setLayoutParams(getLayoutParams(parentLayout, Float.parseFloat(weightNode.getNodeValue())));
        }

        // add an onTouchListener that sends network commands when clicked
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

        // add the button to the layout
        parentLayout.addView(newButton);
    }

    /**
     * Create buttons in a given context from a button map
     *
     * @param context given Context
     * @param buttons a mapping from button ids to button texts
     */
    public void createFromMap(Context context, Map<Integer, String> buttons) {
        // remove any previous views
        removeAllViews();

        // default to vertical linear layout
        setOrientation(VERTICAL);

        // order button ids, so we have a deterministic order to the buttons
        TreeSet<Integer> orderedButtonIds = new TreeSet<>(buttons.keySet());

        for (Integer buttonId : orderedButtonIds) {
            // create a fitting layout param object
            LinearLayout.LayoutParams lp = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
            lp.weight = 1;

            // add the button to our layout using the previously created layout params
            Button newButton = new Button(context);
            addView(newButton, lp);

            // set the button information as required by the map
            newButton.setText(buttons.get(buttonId));
            newButton.setTag(buttonId);


            newButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        // if a up or down event was registered, send an appropriate button click
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_UP:
                            mNetworkClient.sendCommand(new ButtonClick(
                                    (Integer) view.getTag(),
                                    motionEvent.getAction() == MotionEvent.ACTION_DOWN));
                            return true;

                        // return false for unhandled cases
                        default:
                            return false;
                    }
                }
            });
        }
    }
}
