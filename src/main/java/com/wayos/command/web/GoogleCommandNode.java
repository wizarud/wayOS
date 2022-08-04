package com.wayos.command.web;

import com.wayos.Hook;
import com.wayos.MessageObject;
import com.wayos.NodeEvent;
import com.wayos.Session;
import com.wayos.command.CommandNode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by eossth on 7/31/2017 AD.
 */
public class GoogleCommandNode extends CommandNode {

    public static class GoogleURLBuilder {

        private static final Map<String, String> googleMap = new TreeMap<>();

        static {
            googleMap.put("Albania", "https://www.google.al");
            googleMap.put("Algeria", "https://www.google.dz");
            googleMap.put("Argentina", "https://www.google.com.ar");
            googleMap.put("Armenia", "https://www.google.am");
            googleMap.put("Australia", "https://www.google.com.au");
            googleMap.put("Austria", "https://www.google.at");
            googleMap.put("Azerbaijan", "https://www.google.az");
            googleMap.put("Bahrain", "https://www.google.com.bh");
            googleMap.put("Bangladesh", "https://www.google.com.bd");
            googleMap.put("Belarus", "https://www.google.by");
            googleMap.put("Belgium", "https://www.google.be");
            googleMap.put("Bolivia", "https://www.google.com.bo");
            googleMap.put("Bosnia and Herzegovina", "https://www.google.ba");
            googleMap.put("Brazil", "https://www.google.com.br");
            googleMap.put("Bulgaria", "https://www.google.bg");
            googleMap.put("Cambodia", "https://www.google.com.kh");
            googleMap.put("Canada", "https://www.google.ca");
            googleMap.put("Chile", "https://www.google.cl");
            googleMap.put("China", "https://www.google.cn");
            googleMap.put("Colombia", "https://www.google.com.co");
            googleMap.put("Costa Rica", "https://www.google.co.cr");
            googleMap.put("Croatia", "https://www.google.hr");
            googleMap.put("Cuba", "https://www.google.com.cu");
            googleMap.put("Cyprus", "https://www.google.com.cy");
            googleMap.put("Czech Republic", "https://www.google.cz");
            googleMap.put("Denmark", "https://www.google.dk");
            googleMap.put("Dominican Republic", "https://www.google.com.do");
            googleMap.put("Ecuador", "https://www.google.com.ec");
            googleMap.put("Egypt", "https://www.google.com.eg");
            googleMap.put("El Salvador", "https://www.google.com.sv");
            googleMap.put("Estonia", "https://www.google.ee");
            googleMap.put("Ethiopia", "https://www.google.com.et");
            googleMap.put("Finland", "https://www.google.fi");
            googleMap.put("France", "https://www.google.fr");
            googleMap.put("Georgia", "https://www.google.ge");
            googleMap.put("Germany", "https://www.google.de");
            googleMap.put("Ghana", "https://www.google.com.gh");
            googleMap.put("Greece", "https://www.google.gr");
            googleMap.put("Guatemala", "https://www.google.com.gt");
            googleMap.put("Honduras", "https://www.google.hn");
            googleMap.put("Hong Kong", "https://www.google.com.hk");
            googleMap.put("Hungary", "https://www.google.co.hu");
            googleMap.put("Iceland", "https://www.google.is");
            googleMap.put("India", "https://www.google.co.in");
            googleMap.put("Indonesia", "https://www.google.co.id");
            googleMap.put("Iraq", "https://www.google.com.iq");
            googleMap.put("Ireland", "https://www.google.ie");
            googleMap.put("Israel", "https://www.google.co.il");
            googleMap.put("Italy", "https://www.google.it");
            googleMap.put("Japan", "https://www.google.co.jp");
            googleMap.put("Jordan", "https://www.google.jo");
            googleMap.put("Kenya", "https://www.google.co.ke");
            googleMap.put("Kuwait", "https://www.google.com.kw");
            googleMap.put("Laos", "https://www.google.la");
            googleMap.put("Latvia", "https://www.google.lv");
            googleMap.put("Lebanon", "https://www.google.com.lb");
            googleMap.put("Libya", "https://www.google.com.ly");
            googleMap.put("Lithuania", "https://www.google.lt");
            googleMap.put("Luxembourg", "https://www.google.lu");
            googleMap.put("Macedonia", "https://www.google.mk");
            googleMap.put("Malaysia", "https://www.google.com.my");
            googleMap.put("Malta", "https://www.google.com.mt");
            googleMap.put("Mexico", "https://www.google.com.mx");
            googleMap.put("Montenegro", "https://www.google.me");
            googleMap.put("Morocco", "https://www.google.co.ma");
            googleMap.put("Nepal", "https://www.google.com.np");
            googleMap.put("Netherlands", "https://www.google.nl");
            googleMap.put("New Zealand", "https://www.google.co.nz");
            googleMap.put("Nicaragua", "https://www.google.com.ni");
            googleMap.put("Nigeria", "https://www.google.com.ng");
            googleMap.put("Norway", "https://www.google.no");
            googleMap.put("Oman", "https://www.google.com.om");
            googleMap.put("Pakistan", "https://www.google.com.pk");
            googleMap.put("Panama", "https://www.google.com.pa");
            googleMap.put("Paraguay", "https://www.google.com.py");
            googleMap.put("Peru", "https://www.google.com.pe");
            googleMap.put("Philippines", "https://www.google.com.ph");
            googleMap.put("Poland", "https://www.google.pl");
            googleMap.put("Portugal", "https://www.google.pt");
            googleMap.put("Puerto Rico", "https://www.google.com.pr");
            googleMap.put("Qatar", "https://www.google.com.qa");
            googleMap.put("Romania", "https://www.google.ro");
            googleMap.put("Russia", "https://www.google.ru");
            googleMap.put("Saudi Arabia", "https://www.google.com.sa");
            googleMap.put("Serbia", "https://www.google.rs");
            googleMap.put("Singapore", "https://www.google.com.sg");
            googleMap.put("Slovakia", "https://www.google.sk");
            googleMap.put("Slovenia", "https://www.google.si");
            googleMap.put("South Africa", "https://www.google.co.za");
            googleMap.put("South Korea", "https://www.google.co.kr");
            googleMap.put("Spain", "https://www.google.es");
            googleMap.put("Sri Lanka", "https://www.google.lk");
            googleMap.put("Srilanka", "https://www.google.lk");
            googleMap.put("State of Palestine", "https://www.google.ps");
            googleMap.put("Sweden", "https://www.google.se");
            googleMap.put("Switzerland", "https://www.google.ch");
            googleMap.put("Taiwan", "https://www.google.com.tw");
            googleMap.put("Tanzania", "https://www.google.co.tz");
            googleMap.put("Thailand", "https://www.google.co.th");
            googleMap.put("Tunisia", "https://www.google.tn");
            googleMap.put("Turkey", "https://www.google.com.tr");
            googleMap.put("Ukraine", "https://www.google.com.ua");
            googleMap.put("United Arab Emirates", "https://www.google.ae");
            googleMap.put("United Kingdom", "https://www.google.co.uk");
            googleMap.put("United States", "https://www.google.com");
            googleMap.put("Uruguay", "https://www.google.com.uy");
            googleMap.put("Venezuela", "https://www.google.co.ve");
            googleMap.put("Vietnam", "https://www.google.com.vn");
        }

        public static String build(Locale locale) {

            String url = googleMap.get(locale.getDisplayCountry(Locale.US));

            if (url!=null) return url;

            return "https://www.google.com";
        }
    }

    public final String googleURL;

    public final int limit;

    public GoogleCommandNode(Session session, String [] hooks, int limit) {
        this(session, hooks, GoogleURLBuilder.build(Locale.getDefault()), limit);
    }

    public GoogleCommandNode(Session session, String [] hooks, String googleURL, int limit) {
        super(session, hooks, Hook.Match.Head);
        this.googleURL = googleURL;
        this.limit = limit;
    }

    @Override
    public String execute(MessageObject messageObject) {

        String params = cleanHooksFrom(messageObject.toString());

        String encodedParams;

        try {
            encodedParams = URLEncoder.encode(params,"UTF-8");
        } catch (UnsupportedEncodingException e1) {
            encodedParams = params;
        }

        try {

            String url = googleURL + "/search?num=" + limit + "&q=";
            Document doc = Jsoup.connect(url+encodedParams).userAgent(
                    "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36").get();

            /* For Floating WIKI
            Elements elements = doc.select("b:contains(" + pack(params) + ")");
            if (elements.size()>0) {
                Element element = elements.get(elements.size()-1);
                Element parent = element.parent();
                return parent.text();
            }
            */
            StringBuilder resultText = new StringBuilder();

            resultText.append(doc.select("span.st").text().trim());

            Elements results = doc.select("h3.r > a");

            for (Element result : results) {
                String linkHref = result.attr("href");
                if (linkHref.indexOf("/url?q=")!=-1) {
                    linkHref = URLDecoder.decode(linkHref.substring(linkHref.indexOf("/url?q=")+"/url?q=".length(), linkHref.indexOf("&sa=")), "UTF-8");
                } else {
                    linkHref = googleURL + linkHref;
                }
                if (!linkHref.isEmpty())
                    resultText.append(System.lineSeparator() + linkHref);

                /*
                String linkText = result.text().trim();
                if (!linkText.isEmpty())
                    resultText.append(" " + linkText);
                   */
            }

            String result = resultText.toString().trim();
            if (!result.isEmpty()) {
                if (session.sessionListener !=null) {
                    session.sessionListener.callback(new NodeEvent(this, MessageObject.build(messageObject, result), NodeEvent.Event.LateReply));
                    return "";
                }
                return result;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return failMsg();
    }

    private String pack(String texts) {
        String [] textArray = texts.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String text:textArray) {
            sb.append(text);
            sb.append(" ");
        }
        return sb.toString().trim();
    }

}
