package net.sourceforge.omov.core.tools.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.sourceforge.omov.core.BeanFactory;
import net.sourceforge.omov.core.BusinessException;
import net.sourceforge.omov.core.PreferencesDao;
import net.sourceforge.omov.core.bo.CoverFileType;
import net.sourceforge.omov.core.bo.Movie;
import net.sourceforge.omov.core.util.CloseableUtil;
import net.sourceforge.omov.core.util.FileUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author christoph_pickl@users.sourceforge.net
 */
public class ExporterHtml implements IExporterHtml {

    private static final Log LOG = LogFactory.getLog(ExporterHtml.class);

    /** used within html code; e.g.: "Monday, 11 February 2008 - 23:53:59" */
    private static final SimpleDateFormat CURRENT_DATE_FORMAT = new SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm:ss");

    private static final String COVERS_FOLDER_NAME = "covers";

    /**
     * @see {@link http://www.walterzorn.com/tooltip/tooltip_e.htm}
     */
    private static String wzTooltipJsContentCache = null;

    private final List<HtmlColumn> columns;

    private String targetFilePath = null;

    public static void main(String[] args) throws BusinessException {
        List<Movie> movies = BeanFactory.getInstance().getMovieDao().getMoviesSorted();
        try {
            List<HtmlColumn> columns = new ArrayList<HtmlColumn>();
            columns.add(HtmlColumn.COLUMN_ACTORS);
            columns.add(HtmlColumn.COLUMN_COVER);
            new ExporterHtml(columns).process(movies, new File("/OmovMoviesTarget/tut.html"));
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }

    public ExporterHtml(List<HtmlColumn> columns) {
        final List<HtmlColumn> list = new ArrayList<HtmlColumn>();
        list.add(HtmlColumn.COLUMN_ID);
        list.add(HtmlColumn.COLUMN_TITLE);
        for (HtmlColumn column : columns) {
            if (column == HtmlColumn.COLUMN_ID) {
                LOG.warn("Setting column ID is not necessary!");
                continue;
            }
            if (column == HtmlColumn.COLUMN_TITLE) {
                LOG.warn("Setting column Title is not necessary!");
                continue;
            }
            list.add(column);
        }
        this.columns = Collections.unmodifiableList(list);
        LOG.info("Exporting with columns: " + Arrays.toString(columns.toArray()));
    }

    /**
     * @return file[0]=new target file; file[1]=created directory
     */
    private static File[] copyCoverFiles(List<Movie> movies, File targetFile) throws BusinessException {
        final File targetDirectory = getAvailableTargetDirectory(targetFile);
        LOG.debug("Copying cover files to directory '" + targetDirectory.getAbsolutePath() + "'.");
        final File targetCoverDirectory = new File(targetDirectory, COVERS_FOLDER_NAME);
        if (targetCoverDirectory.mkdirs() == false) {
            throw new BusinessException("Could not create cover folder '" + targetCoverDirectory.getAbsolutePath() + "'!");
        }
        for (Movie movie : movies) {
            if (movie.isCoverFileSet()) {
                copyCoverFile(movie, targetCoverDirectory, true);
                copyCoverFile(movie, targetCoverDirectory, false);
            }
        }
        targetFile = new File(targetDirectory, targetFile.getName());
        return new File[] { targetFile, targetDirectory };
    }

    private static void copyCoverFile(Movie movie, File targetCoverDirectory, boolean isThumbNail) throws BusinessException {
        final CoverFileType coverType;
        final String newCoverFileName;
        if (isThumbNail) {
            coverType = CoverFileType.THUMBNAIL;
            newCoverFileName = "lil_" + movie.getOriginalCoverFile();
        } else {
            coverType = CoverFileType.NORMAL;
            newCoverFileName = "big_" + movie.getOriginalCoverFile();
        }
        final File originalCoverFile = new File(PreferencesDao.getInstance().getCoversFolder(), movie.getCoverFile(coverType));
        final File targetFile = new File(targetCoverDirectory, newCoverFileName);
        FileUtil.copyFile(originalCoverFile, targetFile);
    }

    private static File getAvailableTargetDirectory(final File targetFile) {
        String name = targetFile.getName();
        final String extension = FileUtil.extractExtension(targetFile);
        if (extension != null) {
            name = name.substring(0, name.length() - (extension.length() + 1));
        }
        final File parent = targetFile.getParentFile();
        File dir = new File(parent, name);
        int i = 1;
        while (dir.exists() == true) {
            dir = new File(parent, name + "-" + i);
            i++;
        }
        LOG.debug("Returning getAvailableTargetDirectory(targetFile=" + targetFile.getAbsolutePath() + "): '" + dir.getAbsolutePath() + "'");
        return dir;
    }

    public String getTargetFilePath() {
        return this.targetFilePath;
    }

    private static String getContentsOfWzTooltipJs() throws BusinessException {
        if (wzTooltipJsContentCache == null) {
            LOG.debug("initializing wzTooltipJsContentCache.");
            final StringBuilder sb = new StringBuilder(35043 + 50);
            sb.append("\n\n<!-- BEGIN OF wz_tooltip.js -->\n\n\n");
            sb.append("<script type='text/javascript'>\n");
            sb.append(FileUtil.getFileContentsFromJar("/wz_tooltip.js", 35043));
            sb.append("</script>\n");
            sb.append("\n\n<!-- END OF wz_tooltip.js -->\n\n\n");
            wzTooltipJsContentCache = sb.toString();
        }
        return wzTooltipJsContentCache;
    }

    public void process(List<Movie> movies, File target) throws BusinessException {
        LOG.info("Going to export " + movies.size() + " movies to target '" + target.getAbsolutePath() + "'.");
        boolean processFinishedSuccessfully = false;
        File createdDir = null;
        try {
            final boolean coversEnabled = columns.contains(HtmlColumn.COLUMN_COVER);
            if (coversEnabled) {
                final File[] targetFileAndTargetDir = copyCoverFiles(movies, target);
                LOG.info("Resetting target file to '" + target.getAbsolutePath() + "'.");
                target = targetFileAndTargetDir[0];
                createdDir = targetFileAndTargetDir[1];
            }
            this.targetFilePath = target.getAbsolutePath();
            final String currentDate = CURRENT_DATE_FORMAT.format(new Date());
            BufferedWriter writer = null;
            try {
                LOG.debug("Opening writer for file '" + target.getAbsolutePath() + "'.");
                writer = new BufferedWriter(new FileWriter(target));
                writer.write("<html>\n" + "<head>\n" + "<title>OurMovies - Movies from " + PreferencesDao.getInstance().getUsername() + "</title>\n");
                writer.write(getHeadContent());
                writer.write("</head>\n" + "\n" + "<body>\n");
                LOG.debug("Writing contents of wz_tooltip.js");
                writer.write(getContentsOfWzTooltipJs());
                writer.write("<h1>Movies from " + PreferencesDao.getInstance().getUsername() + "</h1>\n" + "<div id='date'>" + currentDate + "</div>\n" + "\n" + "<form id='data_form'>\n" + "\n" + "<table id='tbl_data' cellspacing='0' cellpadding='12'>\n" + "   <colgroup>\n");
                for (HtmlColumn column : this.columns) {
                    writer.write("       <col width='" + column.getWidth() + "' valign='middle' /> <!-- " + column.getLabel() + " -->\n");
                }
                writer.write("   </colgroup>\n" + "   <tr>\n");
                for (HtmlColumn column : this.columns) {
                    writer.write("       <th class='th'>" + column.getLabel() + "</th>\n");
                }
                writer.write("   </tr>\n");
                boolean isEven = true;
                LOG.debug("Wrting table rows for " + movies.size() + " movies.");
                for (final Movie movie : movies) {
                    writer.write(this.getHtmlCodeForMovie(movie, isEven));
                    isEven = !isEven;
                }
                writer.write("</table>" + "\n" + "<div id='outputIdsWrapper'>\n" + "   <textarea id='outputIds' readonly='readonly'></textarea>\n" + "   <div id='btnGenerate'><input type='submit' value='Generate ID Request' onclick='doGenerateIds();return false' /></div>\n" + "</div>\n" + "\n" + "</form>\n" + "\n" + "<p id='footer'>Created with OurMovies v" + BeanFactory.getInstance().getCurrentApplicationVersion().getVersionString() + ": <a id='footer_link' href='http://omov.sourceforge.net' target='_blank'>http://omov.sourceforge.net</a></p>" + "</body>\n" + "</html>\n");
            } catch (IOException e) {
                throw new BusinessException("Could not generate HTML report!", e);
            } finally {
                CloseableUtil.close(writer);
            }
            LOG.info("Exporting HTML finished.");
            processFinishedSuccessfully = true;
        } finally {
            if (processFinishedSuccessfully == false && createdDir != null) {
                LOG.info("Going to delete created directory '" + createdDir.getAbsolutePath() + "' because exporting html failed.");
                try {
                    FileUtil.deleteDirectoryRecursive(createdDir);
                } catch (BusinessException e) {
                    LOG.error("Could not delete created directory '" + createdDir.getAbsolutePath() + "'!", e);
                }
            }
        }
    }

    private String getHtmlCodeForMovie(Movie movie, boolean isEven) {
        final StringBuilder sb = new StringBuilder(100);
        final String trClassName = "tr_" + (isEven ? "even" : "odd");
        sb.append("   <tr class='").append(trClassName).append("' onmouseover=\"javascript:overTr(this);\" onmouseout=\"javascript:outTr(this, '").append(trClassName).append("');\">\n");
        for (HtmlColumn column : this.columns) {
            sb.append("      <td class='td_").append(column.getStyleClass()).append("'>").append(column.getValue(movie, this)).append("</td>\n");
        }
        sb.append("   </tr>\n");
        return sb.toString();
    }

    /**
     * @see HtmlFileConverter
     */
    private static String getHeadContent() {
        return "<script type='text/javascript'>\n" + "\n" + "function doGenerateIds() {\n" + "   form = document.getElementById('data_form');\n" + "   output = '';\n" + "   for(i=0; i < form.inpId.length; i++) {\n" + "       currentInpId = form.inpId[i];\n" + "       if(currentInpId.checked == true) {\n" + "           if(output != '') output += '|';\n" + "           output += currentInpId.value;\n" + "       }\n" + "   }\n" + "   if(output == '') {\n" + "       alert('Not any movie was selected.');\n" + "   } else {\n" + "       document.getElementById('outputIds').innerHTML = '[[' + output + ']]';\n" + "   }\n" + "}\n" + "\n" + "\n" + "function overTr(tr) {\n" + "// debug('over: ' + tr);\n" + "   tr.className = 'tr_over';\n" + "}\n" + "function outTr(tr, oldClassName) {\n" + "// debug('out: ' + tr);\n" + "   tr.className = oldClassName;\n" + "}\n" + "function clickTitle(tr, checkboxId) {\n" + "// debug('click: ' + checkboxId);\n" + "   chkBox = document.getElementById('inpCheckbox'+checkboxId);\n" + "   chkBox.checked = !chkBox.checked;\n" + "}\n" + "/*\n" + "document.write('<textarea id=\"debugField\"></textarea>');\n" + "function debug(str) {\n" + "   document.getElementById('debugField').innerHTML =  str + '\n" + "' + document.getElementById('debugField').innerHTML;\n" + "}\n" + "*/\n" + "</script>\n" + "\n" + "<style type='text/css'>\n" + "body {\n" + "   background-color:#EDEEEE;\n" + "   margin:10px 0px 0px 0px; /* top, right, bottom, left */\n" + "}\n" + "\n" + "body, h1, #date, .title_link, #footer {\n" + "   font-family: verdana, sans-serif;\n" + "}\n" + "body, h1, #date {\n" + "   color:#101010;\n" + "}\n" + "h1 {\n" + "   font-size:15pt;\n" + "   font-weight:bold;\n" + "   margin:10px 0px 0px 0px; /* top, right, bottom, left */\n" + "}\n" + "h1, #date, #footer {\n" + "   margin-left:15px;\n" + "   margin-right:15px;\n" + "}\n" + "#date {\n" + "   font-size:10pt;\n" + "   margin-bottom:0px;\n" + "}\n" + "#data_wrapper {\n" + "   margin-top:30px;\n" + "   margin-bottom:15px; /* ... button needs to be closer */\n" + "}\n" + "#data_header {\n" + "   border-bottom:2px solid black;\n" + "}\n" + ".tr_even {\n" + "   background-color:#FFFFFF;\n" + "}\n" + ".tr_odd {\n" + "   background-color:#EDEEEE;\n" + "}\n" + ".tr_over {\n" + "   background-color:#CCCDCD;\n" + "}\n" + ".tr_even, .tr_odd {\n" + "   height:20px;\n" + "}\n" + ".td_id, .td_cover, .td_title, .td_genre, .td_actors, .td_language, .td_rating,\n" + ".td_style, .td_director, .td_year, .td_quality, .td_file_size, .td_format, .td_duration, .td_resolution, .td_subtitles {\n" + "   border-bottom:1px solid #999999;\n" + "   empty-cells:show;\n" + "   padding:6px 0px 6px 0px;\n" + "   padding-left:6px;\n" + "   padding-right:6px;\n" + "}\n" + ".td_id {\n" + "   text-align:center;\n" + "}\n" + ".td_rating, .td_duration, .td_file_size, .td_resolution, .td_year {\n" + "   text-align:right;\n" + "}\n" + "#outputIds {\n" + "   width:450px;\n" + "   height:70px;\n" + "}\n" + "#outputIdsWrapper {\n" + "   text-align:center;\n" + "}\n" + "#btnGenerate {\n" + "   margin-top:10px;\n" + "}\n" + "#tbl_data {\n" + "   width:100%;\n" + "   margin-top:20px;\n" + "   margin-bottom:40px;\n" + "}\n" + ".th {\n" + "   border-bottom:1px solid #999999;\n" + "   padding:6px 10px 6px 10px;\n" + "   empty-cells:show;\n" + "}\n" + ".title_link {\n" + "   text-decoration:none;\n" + "   color:#060666\n" + "}\n" + ".title_link:hover {\n" + "   color:#020222\n" + "}\n" + "\n" + "#footer, #footer_link {\n" + "   font-size:10pt;\n" + "   color:#999999;\n" + "}\n" + "#footer_link {\n" + "   text-decoration:underline;\n" + "}\n" + "#footer_link:hover {\n" + "   color:#666666;\n" + "   text-decoration:none;\n" + "}\n" + ".ratingYes {\n" + "\n" + "}\n" + ".ratingNo {\n" + "   color:#BBBBBB;\n" + "}\n" + "#tbl_data {\n" + "   margin-top:0px;\n" + "}\n" + "</style>\n";
    }

    private File coversFolderCache = null;

    /**
     * Interface method IExporterHtml for HtmlColumn class.
     */
    public File getCoversFolder() {
        if (this.coversFolderCache == null) {
            this.coversFolderCache = new File(new File(this.targetFilePath).getParent(), COVERS_FOLDER_NAME);
            assert (this.coversFolderCache.exists());
        }
        return this.coversFolderCache;
    }
}
