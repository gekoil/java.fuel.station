import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        // dd/MM/yyyy HH:mm:ss
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        StringBuffer buffer = new StringBuffer(100);
        buffer.append(time.format(formatter));
        buffer.append(" " + record.getLevel() + " ");
        buffer.append(formatMessage(record) + Global.LINE_SEPARATOR);
        return buffer.toString();
    }
}
