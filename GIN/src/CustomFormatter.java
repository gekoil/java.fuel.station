import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        // dd/MM/yyyy HH:mm:ss
    	LocalDateTime time = LocalDateTime.now();
    	DateTimeFormatter formater = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        StringBuffer buffer = new StringBuffer(100);
        buffer.append(time.format(formater));
        buffer.append(" " + record.getLevel() + " ");
        buffer.append(formatMessage(record) + '\n');
        return buffer.toString();
    }
}
