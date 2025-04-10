package org.scribble.export.text;

import org.scribble.export.*;
import org.scribble.extensions.RegistryInfo;
import org.scribble.model.*;

/**
 * This class implements the text based export rule for the
 * ChannelList entity.
 */
@RegistryInfo(extension = ExportRule.class)
public class ChannelListTextExportRule implements ExportRule {

    /**
	 * This method determines whether the exporter rule is appropriate
	 * for the supplied model object and format.
	 * 
	 * @param modelObject The model object
	 * @param format The format
	 * @return Whether the rule is appropriate for the model object and
	 * 						format
	 */
    public boolean isSupported(ModelObject modelObject, Formatter format) {
        return (ChannelList.class.isAssignableFrom(modelObject.getClass()) && format instanceof TextFormatter);
    }

    /**
	 * This method exports the model object.
	 * 
	 * @param modelObject The model object
	 * @param context The context
	 * @throws IOException Failed to record export information
	 */
    public void export(ModelObject modelObject, ExporterContext context) throws java.io.IOException {
        ChannelList chlist = (ChannelList) modelObject;
        TextFormatter formatter = (TextFormatter) context.getFormatter();
        formatter.record("channel\t");
        for (int i = 0; i < chlist.getChannels().size(); i++) {
            if (i > 0) {
                formatter.record(", ");
            }
            context.export(chlist.getChannels().get(i));
        }
    }
}
