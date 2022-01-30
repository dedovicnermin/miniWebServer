package tech.nermindedovic.webserver;

import org.assertj.core.internal.Integers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.PATH;

class RequestTest {

    @Test
    void test1() {
        String requestLine = "GET / HTTP/1.1";

        final String[] elems = requestLine.split(" ");

        assertThat(elems.length).isEqualTo(3);
        assertThat(elems[0]).isEqualTo("GET");
        assertThat(elems[1]).isEqualTo("/");
        assertThat(elems[2]).isEqualTo("HTTP/1.1");
    }


    @Test
    void test2() {
        String respLine1 = "HTTP/1.1 200 OK";
        String respLine2 = "HTTP/1.1 404 NOT FOUND";
        final String property = System.getProperty("line.separator");
        System.out.println(property);
        final String x = System.lineSeparator();
        System.out.println(x);

        System.out.println("PROPERTY : " + property);
        System.out.println("LINE SEP : " + x);

    }

    @Test
    void test3() {
        StringBuilder builder = new StringBuilder();
        builder.append("GET /abc HTTP/1.1").append(System.lineSeparator());
        builder.append("Server: Apache / 1.3.27").append(System.lineSeparator());
        builder.append("Content-Type: text/html").append(System.lineSeparator());
        builder.append(System.lineSeparator());

        final String toString = builder.toString();
        System.out.println(toString);

        final String[] split = toString.split(System.lineSeparator());
        assertThat(split).hasSize(3);
    }

    @Test
    void response() {
        final StringBuilder builder = new StringBuilder();
        builder.append("HTTP/1.1 200 OK").append(System.lineSeparator());
        builder.append("Connection: close").append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append("DATA DATA DATA DATA DATA").append(System.lineSeparator());

        final String s = builder.toString();
        System.out.println(s);
    }

    @Test
    void testtttt() {
        String actual = "GET /abc?name=nermin&num1=1&num2=1 HTTP/1.1";
        final String[] s = actual.split(" ");
        final String s1 = s[1];
        System.out.println("About to split " + s1);


        final String replace = s1.replace('?', ' ');
        System.out.println("REPLACE: " + replace);

        final String[] s2 = replace.split(" ");
        System.out.println(s2.toString());

        final String s3 = s2[1];
        System.out.println(s3);

        final String[] split = s3.split("&");
        System.out.println(Arrays.toString(split));

    }


    @Test
    void testFormDatamethod() {
        final SumFormData sumFormData = new SumFormData();
        sumFormData.mapToObject("name=nermin");
        sumFormData.mapToObject("num1=34343");
        sumFormData.mapToObject("num2=324");

        assertThat(sumFormData.getName()).isEqualTo("nermin");
        assertThat(sumFormData.getNum1()).isEqualTo("34343");
        assertThat(sumFormData.getNum2()).isEqualTo("324");

        int i = 324 + 34343;
        assertThat(sumFormData.getSum()).isEqualTo(Integer.toString(i));
    }


}
