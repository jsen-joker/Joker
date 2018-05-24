package com.jsen.joker.core.plugin.entry.server.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>
 * </p>
 *
 * @author jsen
 * @since 2018/5/17
 */
public class XmlToJson {

    private static String number = "\\d+";
    private static Pattern isNumber = Pattern.compile(number);

    public static JsonObject xml2JSON(InputStream inputStream) throws JDOMException, IOException {
        JsonObject json = new JsonObject();
        SAXBuilder sb = new SAXBuilder();
        org.jdom2.Document doc = sb.build(inputStream);
        Element root = doc.getRootElement();
        json.put(root.getName(), iterateElement(root));
        return json;
    }

    private static JsonObject iterateElement(Element element) {
        List node = element.getChildren();
        Element et;
        JsonObject obj = new JsonObject();
        JsonArray list;
        for (int i = 0; i < node.size(); i++) {
            list = new JsonArray();
            et = (Element) node.get(i);
            if ("".equals(et.getTextTrim())) {
                if (et.getChildren().size() == 0) {
                    continue;
                }
                if (obj.containsKey(et.getName())) {
                    list = obj.getJsonArray(et.getName());
                }
                list.add(iterateElement(et));
                obj.put(et.getName(), list);
            } else {
                if (obj.containsKey(et.getName())) {
                    list = obj.getJsonArray(et.getName());
                }
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("value", et.getText());
                List<Attribute> attributes = et.getAttributes();
                JsonObject attrs = new JsonObject();
                for (Attribute attribute: attributes) {
                    if (isNumber.matcher(attribute.getValue()).matches()) {
                        try {
                            attrs.put(attribute.getName(), attribute.getIntValue());
                        } catch (DataConversionException e) {
                            e.printStackTrace();
                        }
                    } else {
                        attrs.put(attribute.getName(), attribute.getValue());
                    }
                }
                jsonObject.put("attrs", attrs);
                list.add(jsonObject);
                obj.put(et.getName(), list);
            }
        }
        return obj;
    }


}
