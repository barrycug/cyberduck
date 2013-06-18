package ch.cyberduck.core.filter;

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

import java.util.regex.Pattern;

/**
 * @version $Id$
 */
public class DownloadRegexFilter implements Filter<Path> {
    private static final Logger log = Logger.getLogger(DownloadRegexFilter.class);

    private final Pattern pattern
            = Pattern.compile(Preferences.instance().getProperty("queue.download.skip.regex"));

    @Override
    public boolean accept(final Path file) {
        if(file.attributes().isDuplicate()) {
            return false;
        }
        if(Preferences.instance().getBoolean("queue.download.skip.enable")) {
            if(pattern.matcher(file.getName()).matches()) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Skip %s excluded with regex", file.getAbsolute()));
                }
                return false;
            }
        }
        return true;
    }
}
