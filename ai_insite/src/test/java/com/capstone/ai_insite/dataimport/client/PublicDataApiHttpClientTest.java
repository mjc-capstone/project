package com.capstone.ai_insite.dataimport.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PublicDataApiHttpClientTest {

    @Test
    void buildsCommercialTransactionUriWithoutDoubleEncoding() {
        PublicDataApiHttpClient client = new PublicDataApiHttpClient(
            "https://apis.data.go.kr",
            "testKey"
        );
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("LAWD_CD", "11110");
        parameters.put("DEAL_YMD", "202601");
        parameters.put("numOfRows", "1000");
        parameters.put("pageNo", "1");

        assertEquals(
            "https://apis.data.go.kr/1613000/RTMSDataSvcNrgTrade/"
                + "getRTMSDataSvcNrgTrade?serviceKey=testKey&LAWD_CD=11110"
                + "&DEAL_YMD=202601&numOfRows=1000&pageNo=1",
            client.buildUri(
                "/1613000/RTMSDataSvcNrgTrade/getRTMSDataSvcNrgTrade",
                parameters
            ).toString()
        );
    }

    @Test
    void decodesResponseBodyAsUtf8WhenCharsetHeaderIsMissing() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/test", exchange -> {
            byte[] body = "<umdNm>개포동</umdNm>".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            PublicDataApiHttpClient client = new PublicDataApiHttpClient(
                "http://localhost:" + server.getAddress().getPort(),
                "testKey"
            );

            assertTrue(client.fetch("/test", Map.of()).contains("개포동"));
        } finally {
            server.stop(0);
        }
    }
}
