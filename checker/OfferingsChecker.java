package checker;

import org.jsoup.nodes.Document;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import checkerinterface.*;

public class OfferingsChecker
{

    // properties of class
    String courseDept; // MATH, PHYS etc.
    int courseNum;     // 101, 102 etc.
    int section;       // 1, 10, 25 etc.
    int year;          // year of fall semester
    int semester;      // 1 for fall, 2 for spring, 3 for summer

    String courseLabel;  // full course label in the format "PHYS 520-1"

    // sub objects
    URLReader reader;
    DocParser parser;

    public OfferingsChecker( String courseDept, int courseNum, int section, int year, int semester)
    {
        this.courseDept = courseDept;
        this.courseNum = courseNum;
        this.section = section;
        this.year = year;
        this.semester = semester;

        courseLabel = constructCourseLabel();

        reader = new URLReader( courseDept, year, semester);
        parser = new DocParser( courseLabel);
    }

    private String constructCourseLabel()
    {
        return courseDept + " " + courseNum + "-" + section;
    }

    public String getCourseLabel()
    {
        return courseLabel;
    }

    public int checkQuota() throws Exception
    {
        Document doc = reader.getPageAsDocument();
        return parser.getQuotaCell( doc);
    }

    public int getCurrentQuota() throws Exception
    {
        return checkQuota();
    }

}
