import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        // dd/MM/yyyy HH:mm:ss
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        StringBuffer buffer = new StringBuffer();
        //LocalDate date = LocalDate.now();
        buffer.append(formatter.format(LocalDate.now()));
        buffer.append(" " + record.getLevel() + " ");
        buffer.append(formatMessage(record));
        return buffer.toString();
    }
}
