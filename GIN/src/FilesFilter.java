import java.util.logging.Filter;
import java.util.logging.LogRecord;


public class FilesFilter implements Filter{
	// All messages needs to contains the ID String.
	private String id;

	public FilesFilter(String id) {
		this.id = id;
	}

	@Override
	public boolean isLoggable(LogRecord record) {
		return record.getMessage().contains(id);
	}

}