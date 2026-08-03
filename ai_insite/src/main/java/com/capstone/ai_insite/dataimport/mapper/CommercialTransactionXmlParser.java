package com.capstone.ai_insite.dataimport.mapper;

import com.capstone.ai_insite.dataimport.dto.publicdata.CommercialTransactionPage;
import com.capstone.ai_insite.dataimport.dto.publicdata.CommercialTransactionRow;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import tools.jackson.databind.ObjectMapper;

@Component
public class CommercialTransactionXmlParser {

    private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);

    private final ObjectMapper objectMapper;

    public CommercialTransactionXmlParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CommercialTransactionPage parse(String responseBody) {
        try {
            DocumentBuilderFactory factory = secureFactory();
            Element root = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(responseBody)))
                .getDocumentElement();
            String resultCode = text(root, "resultCode");
            if (!"000".equals(resultCode)) {
                throw new IllegalArgumentException(
                    "MOLIT API error: " + resultCode + " "
                        + text(root, "resultMsg") + " | root="
                        + root.getTagName() + " | response="
                        + responseBody.substring(
                            0,
                            Math.min(300, responseBody.length())
                        )
                );
            }
            List<CommercialTransactionRow> rows = new ArrayList<>();
            NodeList items = root.getElementsByTagName("item");
            for (int index = 0; index < items.getLength(); index++) {
                rows.add(toRow((Element) items.item(index)));
            }
            return new CommercialTransactionPage(
                integer(root, "pageNo"),
                integer(root, "numOfRows"),
                integer(root, "totalCount"),
                List.copyOf(rows)
            );
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException(
                "MOLIT commercial transaction XML parsing failed.",
                exception
            );
        }
    }

    private CommercialTransactionRow toRow(Element item) throws Exception {
        int year = integer(item, "dealYear");
        int month = integer(item, "dealMonth");
        int day = integer(item, "dealDay");
        String cancellationDay = blankToNull(text(item, "cdealDay"));
        return new CommercialTransactionRow(
            text(item, "sggCd").trim(),
            text(item, "sggNm").trim(),
            text(item, "umdNm").trim(),
            LocalDate.of(year, month, day),
            decimal(item, "dealAmount").multiply(TEN_THOUSAND),
            nullableDecimal(item, "buildingAr"),
            nullableDecimal(item, "plottageAr"),
            blankToNull(text(item, "buildingType")),
            blankToNull(text(item, "buildingUse")),
            blankToNull(text(item, "landUse")),
            nullableInteger(item, "floor"),
            nullableInteger(item, "buildYear"),
            blankToNull(text(item, "jibun")),
            cancellationDay != null
                || blankToNull(text(item, "cdealType")) != null,
            cancellationDay,
            blankToNull(text(item, "dealingGbn")),
            objectMapper.writeValueAsString(childValues(item))
        );
    }

    private static DocumentBuilderFactory secureFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(
            "http://apache.org/xml/features/disallow-doctype-decl",
            true
        );
        factory.setFeature(
            "http://xml.org/sax/features/external-general-entities",
            false
        );
        factory.setFeature(
            "http://xml.org/sax/features/external-parameter-entities",
            false
        );
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    private static Map<String, String> childValues(Element item) {
        Map<String, String> values = new LinkedHashMap<>();
        NodeList children = item.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node node = children.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                values.put(node.getNodeName(), node.getTextContent().trim());
            }
        }
        return values;
    }

    private static String text(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent();
    }

    private static int integer(Element parent, String tagName) {
        return Integer.parseInt(text(parent, tagName).trim());
    }

    private static Integer nullableInteger(Element parent, String tagName) {
        String value = blankToNull(text(parent, tagName));
        return value == null ? null : Integer.valueOf(value);
    }

    private static BigDecimal decimal(Element parent, String tagName) {
        return new BigDecimal(text(parent, tagName).replace(",", "").trim());
    }

    private static BigDecimal nullableDecimal(Element parent, String tagName) {
        String value = blankToNull(text(parent, tagName));
        return value == null
            ? null
            : new BigDecimal(value.replace(",", ""));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
