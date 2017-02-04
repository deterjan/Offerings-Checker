package checker;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class DocParser
{

    String courseLabel;

    public DocParser( String courseLabel)
    {
        this.courseLabel = courseLabel; // full course label in the format "PHYS 520-1"
    }

    public void setCourseLabel( String courseLabel) { this.courseLabel = courseLabel; }

    // bug if section names are not ordered
    public int getQuotaCell( Document doc)
    {
        Element quotaRow = doc.select("tr:has(td:contains(" + courseLabel + "))").first();

        int courseCellIndex = quotaRow
                .select("td:contains(" + courseLabel + ")")
                .first()
                .elementSiblingIndex();

        int quotaCellIndex = courseCellIndex + 13;

        int cellValue = Integer.parseInt( quotaRow
                .getElementsByIndexEquals( quotaCellIndex)
                .text());

        return cellValue;
    }
}
