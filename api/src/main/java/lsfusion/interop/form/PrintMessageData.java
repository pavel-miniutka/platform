package lsfusion.interop.form;

import java.util.List;

public class PrintMessageData {
    
    public final String message;
    public final List<String> titles;
    public final List<List<String>> rows;

    public PrintMessageData(String message, List<String> titles, List<List<String>> rows) {
        this.message = message;
        this.titles = titles;
        this.rows = rows;
    }
}
