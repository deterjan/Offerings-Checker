package checker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class URLReader {

    String url;

    public URLReader( String courseDept, int year, int semester)
    {
        this.url = constructURL( courseDept, year, semester);
    }

    private String constructURL( String courseDept, int year, int semester)
    {
        return "https://stars.bilkent.edu.tr/homepage/ajax/plainOfferings.php?COURSE_CODE=" + courseDept + "&SEMESTER=" + year + semester + "&";
    }

    public String getPageSource() throws Exception
    {
        return Jsoup.connect( url).get().html();
    }

    public Document getPageAsDocument() throws Exception
    {
        return Jsoup.connect( url).get();
    }

}