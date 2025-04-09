package net.metasimian.spelunk.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import net.metasimian.spelunk.analyzer.Analyzer;
import net.metasimian.spelunk.data.DataArtifact;
import net.metasimian.spelunk.filter.Filter;
import net.metasimian.spelunk.statistics.Statistics;

class FileSource extends AbstractDataSource implements DataSource {

    protected File file;

    FileSource(File file) {
        this.file = file;
    }

    public void process(Analyzer analyzer, Filter filter) throws Exception {
        Statistics.dataSourceCount++;
        String filename = file.getName();
        if (!filter.filter(filename)) {
            if (file.isDirectory()) {
                new DirectorySource(file).process(analyzer, filter);
            } else {
                InputStream is = new FileInputStream(file);
                if (ZipFileSource.isZipFile(filename)) {
                    new ZipFileSource(is).process(analyzer, filter);
                } else {
                    analyzer.analyze(new DataArtifact(IOUtils.toByteArray(new FileInputStream(file)), filename));
                }
                is.close();
            }
        }
    }
}
