package org.flume;

import com.typesafe.config.Config;
import org.kitesdk.morphline.api.Command;
import org.kitesdk.morphline.api.CommandBuilder;
import org.kitesdk.morphline.api.MorphlineContext;
import org.kitesdk.morphline.api.Record;
import org.kitesdk.morphline.base.AbstractCommand;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: keyki
 */
public class RegexFilterBuilder implements CommandBuilder {

    @Override
    public Collection<String> getNames() {
        return Collections.singleton("regexFilter");
    }

    @Override
    public Command build(Config config, Command parent, Command child, MorphlineContext context) {
        return new RegexFilter(this, config, parent, child, context);
    }

    private static final class RegexFilter extends AbstractCommand {

        private final String fieldName;
        private final Pattern pattern;
        private final boolean reversePattern;

        public RegexFilter(CommandBuilder builder, Config config, Command parent, Command child, MorphlineContext context) {
            super(builder, config, parent, child, context);
            this.fieldName = getConfigs().getString(config, "fieldName");
            this.pattern = Pattern.compile(getConfigs().getString(config, "pattern"));
            this.reversePattern = getConfigs().getBoolean(config, "reversePattern");
        }

        @Override
        protected boolean doProcess(Record record) {
            for (Object value : record.get(fieldName)) {
                Matcher matcher = pattern.matcher(value.toString());
                if (matcher.find() != reversePattern) {
                    return false;
                }
            }
            return super.doProcess(record);
        }
    }

}
